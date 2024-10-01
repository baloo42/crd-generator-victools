package io.fabric8.crd.generator.victools.v1;

import io.fabric8.crd.generator.victools.annotation.AdditionalPrinterColumn;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceColumnDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceColumnDefinitionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.ValidationRule;
import io.fabric8.kubernetes.api.model.apiextensions.v1.ValidationRuleBuilder;
import lombok.experimental.UtilityClass;

import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.emptyToNull;

@UtilityClass
class CRDv1Utils {

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

  static CustomResourceColumnDefinition createColumnDefinition(AdditionalPrinterColumn annotation) {
    return new CustomResourceColumnDefinitionBuilder()
        .withName(annotation.name())
        .withJsonPath(annotation.jsonPath())
        .withPriority(annotation.priority())
        .build();
  }

}
