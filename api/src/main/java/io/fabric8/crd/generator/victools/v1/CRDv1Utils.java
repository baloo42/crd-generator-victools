package io.fabric8.crd.generator.victools.v1;

import io.fabric8.crd.generator.victools.CustomResourceInfo;
import io.fabric8.kubernetes.api.model.apiextensions.v1.ValidationRule;
import io.fabric8.kubernetes.api.model.apiextensions.v1.ValidationRuleBuilder;
import lombok.experimental.UtilityClass;

import java.util.Collection;

import static io.fabric8.crd.generator.victools.CRDUtils.findRepeatingAnnotations;
import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.emptyToNull;

@UtilityClass
class CRDv1Utils {

  static Collection<ValidationRule> findTopLevelValidationRules(CustomResourceInfo crInfo) {
    return findRepeatingAnnotations(
        crInfo.definition(),
        io.fabric8.generator.annotation.ValidationRule.class).stream()
        .map(CRDv1Utils::createValidationRule)
        .toList();
  }

  static ValidationRule createValidationRule(
      io.fabric8.generator.annotation.ValidationRule annotation) {
    return new ValidationRuleBuilder()
        .withRule(annotation.value())
        .withFieldPath(emptyToNull(annotation.fieldPath()))
        .withMessage(emptyToNull(annotation.message()))
        .withMessageExpression(emptyToNull(annotation.messageExpression()))
        .withOptionalOldSelf(annotation.optionalOldSelf() ? true : null)
        .withReason(emptyToNull(annotation.reason()))
        .build();
  }

}
