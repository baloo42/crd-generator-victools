package io.fabric8.crd.generator.victools.schema;

import io.fabric8.crd.generator.victools.CustomResourceInfo;
import io.fabric8.crd.generator.victools.KubernetesValidationRuleProvider;
import io.fabric8.crd.generator.victools.annotation.ValidationRule;
import io.fabric8.crd.generator.victools.model.ValidationRuleInfo;

import static io.fabric8.crd.generator.victools.CRDUtils.emptyToNull;

public class AdditionalKubernetesValidationRuleProvider
    extends KubernetesValidationRuleProvider.TopLevelAnnotationKubernetesValidationRuleProvider<ValidationRule> {

  public AdditionalKubernetesValidationRuleProvider(CustomResourceInfo crInfo) {
    super(crInfo, ValidationRule.class);
  }

  @Override
  protected ValidationRuleInfo map(ValidationRule annotation) {
    return ValidationRuleInfo.builder()
        .rule(emptyToNull(annotation.value()))
        .fieldPath(emptyToNull(annotation.fieldPath()))
        .reason(emptyToNull(annotation.reason()))
        .message(emptyToNull(annotation.message()))
        .messageExpression(emptyToNull(annotation.messageExpression()))
        .optionalOldSelf(annotation.optionalOldSelf())
        .build();
  }
}
