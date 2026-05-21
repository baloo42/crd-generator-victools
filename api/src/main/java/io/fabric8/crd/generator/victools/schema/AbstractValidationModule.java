package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaKeyword;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.ToIntBiFunction;

@Slf4j
public abstract class AbstractValidationModule implements Module {
  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    // fields
    builder.forFields().withDefaultResolver(target -> resolveDefault(target, builder.getObjectMapper()));
    builder.forFields().withNullableCheck(this::checkNullable);
    builder.forFields().withRequiredCheck(this::checkRequired);

    builder.forFields().withNumberInclusiveMinimumResolver(this::resolveNumberInclusiveMinimum);
    builder.forFields().withNumberExclusiveMinimumResolver(this::resolveNumberExclusiveMinimum);
    builder.forFields().withNumberInclusiveMaximumResolver(this::resolveNumberInclusiveMaximum);
    builder.forFields().withNumberExclusiveMaximumResolver(this::resolveNumberExclusiveMaximum);

    builder.forFields().withStringPatternResolver(this::resolvePattern);
    builder.forFields().withStringMinLengthResolver(this::resolveStringMinLength);
    builder.forFields().withStringMaxLengthResolver(this::resolveStringMaxLength);

    builder.forFields().withArrayMinItemsResolver(this::resolveArrayMinItems);
    builder.forFields().withArrayMaxItemsResolver(this::resolveArrayMaxItems);

    builder.forFields().withInstanceAttributeOverride(this::overrideInstanceAttributes);

    // methods
    builder.forMethods().withDefaultResolver(target -> resolveDefault(target, builder.getObjectMapper()));
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

  protected abstract Object resolveDefault(MemberScope<?, ?> member, ObjectMapper objectMapper);

  /**
   * Parses the string payload of a {@code @Default} annotation into a JSON node whose Jackson type
   * matches the annotated member's declared Java type, so the emitted CRD {@code default:} value is
   * not always a quoted string. String-like and enum members keep the literal value as a
   * {@code TextNode}; all other types are parsed as JSON (e.g. {@code "5"} → {@code IntNode},
   * {@code "true"} → {@code BooleanNode}, {@code "[1,2]"} → {@code ArrayNode}). Invalid JSON on a
   * non-string member is logged and treated as no default.
   */
  protected static JsonNode parseDefaultValue(MemberScope<?, ?> member, String value, ObjectMapper objectMapper) {
    if (value == null) {
      return null;
    }
    if (member.getType().isInstanceOf(CharSequence.class) || member.getType().isInstanceOf(Enum.class)) {
      return TextNode.valueOf(value);
    }
    try {
      return objectMapper.readTree(value);
    } catch (JsonProcessingException e) {
      log.warn("Ignoring @Default value '{}' on {} — not valid JSON for type {}",
          value, member.getName(), member.getType());
      return null;
    }
  }

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

    // Kubernetes structural schemas (apiextensions.k8s.io/v1) follow JSON Schema
    // draft-4: exclusiveMinimum/exclusiveMaximum are booleans modifying
    // minimum/maximum. victools emits draft-6 numeric values, so translate them.
    rewriteExclusiveBoundToKubernetes(attributes,
        context.getKeyword(SchemaKeyword.TAG_MINIMUM_EXCLUSIVE),
        context.getKeyword(SchemaKeyword.TAG_MINIMUM));
    rewriteExclusiveBoundToKubernetes(attributes,
        context.getKeyword(SchemaKeyword.TAG_MAXIMUM_EXCLUSIVE),
        context.getKeyword(SchemaKeyword.TAG_MAXIMUM));
  }

  private void rewriteExclusiveBoundToKubernetes(
      ObjectNode attributes,
      String exclusiveAttribute,
      String inclusiveAttribute) {

    JsonNode exclusiveValue = attributes.get(exclusiveAttribute);
    if (exclusiveValue != null && exclusiveValue.isNumber()) {
      attributes.set(inclusiveAttribute, exclusiveValue);
      attributes.put(exclusiveAttribute, true);
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
