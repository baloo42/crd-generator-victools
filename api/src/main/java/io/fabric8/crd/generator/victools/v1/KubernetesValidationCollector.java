package io.fabric8.crd.generator.victools.v1;

import io.fabric8.crd.generator.victools.CustomResourceContext;
import io.fabric8.crd.generator.victools.KubernetesValidationRuleProvider;
import io.fabric8.crd.generator.victools.model.ValidationRuleInfo;
import io.fabric8.kubernetes.api.model.apiextensions.v1.JSONSchemaProps;
import io.fabric8.kubernetes.api.model.apiextensions.v1.ValidationRule;
import io.fabric8.kubernetes.api.model.apiextensions.v1.ValidationRuleBuilder;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static io.fabric8.crd.generator.victools.CRDUtils.distinctByKey;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
public class KubernetesValidationCollector
    implements PathAwareSchemaPropsVisitor.IdentifiedPropertyVisitor {

  private final CustomResourceContext context;
  private final List<KubernetesValidationRuleProvider> providers;

  @Override
  public void visit(String id, String path, JSONSchemaProps schema) {
    var mergedRules = Stream.concat(
        ofNullable(schema.getXKubernetesValidations()).orElseGet(Collections::emptyList).stream(),
        context.getValidationRules(id).stream().map(this::map))
        .filter(distinctByKey(ValidationRule::getRule))
        .toList();

    schema.setXKubernetesValidations(mergedRules);
  }

  public List<ValidationRule> getTopLevelValidationRules() {
    return providers.stream()
        .map(KubernetesValidationRuleProvider::getValidationRules)
        .flatMap(Collection::stream)
        .map(this::map)
        .filter(distinctByKey(ValidationRule::getRule))
        .toList();
  }

  private ValidationRule map(ValidationRuleInfo rule) {
    return new ValidationRuleBuilder()
        .withRule(rule.getRule())
        .withFieldPath(rule.getFieldPath())
        .withMessage(rule.getMessage())
        .withMessageExpression(rule.getMessageExpression())
        .withOptionalOldSelf(rule.getOptionalOldSelf() ? true : null)
        .withReason(rule.getReason())
        .build();
  }

}
