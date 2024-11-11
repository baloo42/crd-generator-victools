package io.fabric8.crd.generator.victools.schema;

import io.fabric8.crd.generator.victools.annotation.ValidationRule;
import io.fabric8.crd.generator.victools.model.ValidationRuleInfo;

import static io.fabric8.crd.generator.victools.CRDUtils.emptyToNull;

public class KubernetesValidationRuleProvider
    extends AbstractKubernetesValidationRuleProvider<ValidationRule, ValidationRule.List> {

  public KubernetesValidationRuleProvider() {
    super(ValidationRule.class, ValidationRule.List.class);
  }

  @Override
  protected ValidationRule[] extractRepeatedAnnotation(ValidationRule.List annotation) {
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
