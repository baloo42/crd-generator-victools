package io.fabric8.crd.generator.victools.schema.fkc;

import io.fabric8.crd.generator.victools.model.ValidationRuleInfo;
import io.fabric8.crd.generator.victools.schema.AbstractKubernetesValidationRuleProvider;
import io.fabric8.generator.annotation.ValidationRule;
import io.fabric8.generator.annotation.ValidationRules;

import static io.fabric8.crd.generator.victools.CRDUtils.emptyToNull;

public class FkcKubernetesValidationRuleProvider
    extends AbstractKubernetesValidationRuleProvider<ValidationRule, ValidationRules> {

  public FkcKubernetesValidationRuleProvider() {
    super(ValidationRule.class, ValidationRules.class);
  }

  @Override
  protected ValidationRule[] extractRepeatedAnnotation(ValidationRules annotation) {
    return annotation.value();
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
