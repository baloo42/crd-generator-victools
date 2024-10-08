package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaKeyword;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.ToIntBiFunction;

public abstract class AbstractValidationModule implements Module {
  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    // fields
    builder.forFields().withDefaultResolver(this::resolveDefault);
    builder.forFields().withNullableCheck(this::checkNullable);
    builder.forFields().withRequiredCheck(this::checkRequired);

    builder.forFields().withNumberInclusiveMinimumResolver(this::resolveNumberInclusiveMinimum);
    builder.forFields().withNumberExclusiveMinimumResolver(this::resolveNumberExclusiveMinimum);
    builder.forFields().withNumberInclusiveMaximumResolver(this::resolveNumberInclusiveMaximum);
    builder.forFields().withNumberExclusiveMaximumResolver(this::resolveNumberExclusiveMaximum);

    builder.forFields().withStringPatternResolver(this::resolvePattern);
    builder.forMethods().withStringMinLengthResolver(this::resolveStringMinLength);
    builder.forMethods().withStringMaxLengthResolver(this::resolveStringMaxLength);

    builder.forFields().withArrayMinItemsResolver(this::resolveArrayMinItems);
    builder.forFields().withArrayMaxItemsResolver(this::resolveArrayMaxItems);

    builder.forFields().withInstanceAttributeOverride(this::overrideInstanceAttributes);

    // methods
    builder.forMethods().withDefaultResolver(this::resolveDefault);
    builder.forMethods().withNullableCheck(this::checkNullable);
    builder.forMethods().withRequiredCheck(this::checkRequired);

    builder.forMethods().withNumberInclusiveMinimumResolver(this::resolveNumberInclusiveMinimum);
    builder.forMethods().withNumberExclusiveMinimumResolver(this::resolveNumberExclusiveMinimum);
    builder.forMethods().withNumberInclusiveMaximumResolver(this::resolveNumberInclusiveMaximum);
    builder.forMethods().withNumberExclusiveMaximumResolver(this::resolveNumberExclusiveMaximum);

    builder.forMethods().withStringPatternResolver(this::resolvePattern);
    builder.forMethods().withStringMinLengthResolver(this::resolveStringMinLength);
    builder.forMethods().withStringMaxLengthResolver(this::resolveStringMaxLength);

    builder.forMethods().withArrayMinItemsResolver(this::resolveArrayMinItems);
    builder.forMethods().withArrayMaxItemsResolver(this::resolveArrayMaxItems);

    builder.forMethods().withInstanceAttributeOverride(this::overrideInstanceAttributes);
  }

  protected abstract Object resolveDefault(MemberScope<?, ?> member);

  protected abstract Boolean checkNullable(MemberScope<?, ?> member);

  protected abstract Boolean checkRequired(MemberScope<?, ?> member);

  protected abstract BigDecimal resolveNumberInclusiveMinimum(MemberScope<?, ?> member);

  protected abstract BigDecimal resolveNumberExclusiveMinimum(MemberScope<?, ?> member);

  protected abstract BigDecimal resolveNumberInclusiveMaximum(MemberScope<?, ?> member);

  protected abstract BigDecimal resolveNumberExclusiveMaximum(MemberScope<?, ?> member);

  protected abstract String resolvePattern(MemberScope<?, ?> member);

  protected abstract Integer resolveArrayMinItems(MemberScope<?, ?> member);

  protected abstract Integer resolveArrayMaxItems(MemberScope<?, ?> member);

  protected abstract Integer resolveStringMinLength(MemberScope<?, ?> member);

  protected abstract Integer resolveStringMaxLength(MemberScope<?, ?> member);

  protected abstract Integer resolveMapMinEntries(MemberScope<?, ?> member);

  protected abstract Integer resolveMapMaxEntries(MemberScope<?, ?> member);

  private void overrideInstanceAttributes(
      ObjectNode attributes,
      MemberScope<?, ?> member,
      SchemaGenerationContext context) {

    if (member.getType().isInstanceOf(Map.class)) {
      overridePropertyCountAttribute(attributes,
          context.getKeyword(SchemaKeyword.TAG_PROPERTIES_MIN),
          resolveMapMinEntries(member), Math::min);
      overridePropertyCountAttribute(attributes,
          context.getKeyword(SchemaKeyword.TAG_PROPERTIES_MAX),
          resolveMapMaxEntries(member), Math::max);
    }
  }

  private void overridePropertyCountAttribute(
      ObjectNode memberAttributes,
      String attribute,
      Integer newValue,
      ToIntBiFunction<Integer, Integer> getStricterValue) {

    if (newValue == null) {
      return;
    }
    JsonNode existingValue = memberAttributes.get(attribute);
    boolean shouldSetNewValue = existingValue == null
        || !existingValue.isNumber()
        || newValue == getStricterValue.applyAsInt(newValue,
            existingValue.asInt());
    if (shouldSetNewValue) {
      memberAttributes.put(attribute, newValue);
    }
  }
}
