package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.CustomDefinition;
import com.github.victools.jsonschema.generator.CustomDefinitionProviderV2;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaKeyword;
import com.github.victools.jsonschema.generator.impl.AttributeCollector;
import com.github.victools.jsonschema.generator.impl.module.EnumModule;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * Module to treat enum types as plain strings, similar to
 * {@link EnumModule#asStringsFromName()}, but with the following additions:
 * <ul>
 * <li>Enum values, which are annotated with {@link JsonIgnore}, are ignored.</li>
 * <li>The name of an enum value can be overridden, if the value is annotated with {@link JsonProperty}.</li>
 * </ul>
 */
public class JacksonEnumModule implements Module {

  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.forTypesInGeneral()
        .withCustomDefinitionProvider(new EnumAsStringDefinitionProvider());
  }

  /**
   * Implementation of the {@link CustomDefinitionProviderV2} interface for treating enum types as
   * plain strings.
   */
  private static class EnumAsStringDefinitionProvider implements CustomDefinitionProviderV2 {

    @Override
    public CustomDefinition provideCustomSchemaDefinition(ResolvedType javaType,
        SchemaGenerationContext context) {
      if (javaType.isInstanceOf(Enum.class)) {
        ObjectNode customNode = context.getGeneratorConfig().createObjectNode()
            .put(context.getKeyword(SchemaKeyword.TAG_TYPE),
                context.getKeyword(SchemaKeyword.TAG_TYPE_STRING));
        var enumValues = extractEnumValues(javaType);
        new AttributeCollector(context.getGeneratorConfig().getObjectMapper())
            .setEnum(customNode, enumValues, context);
        return new CustomDefinition(customNode);
      }
      return null;
    }

    /**
     * Look-up the given enum type's constant values.
     *
     * @param enumType targeted enum type
     * @return collection containing constant enum values
     */
    private List<String> extractEnumValues(
        ResolvedType enumType) {
      Class<?> erasedType = enumType.getErasedType();
      if (erasedType.getEnumConstants() == null) {
        return null; // NOSONAR
      }

      return Arrays.stream(erasedType.getFields())
          .filter(Field::isEnumConstant)
          .filter(field -> field.getAnnotation(JsonIgnore.class) == null)
          .map(this::getEnumValue)
          .sorted()
          .toList();
    }

    private String getEnumValue(Field field) {
      var jsonPropertyAnnotation = field.getAnnotation(JsonProperty.class);
      if (jsonPropertyAnnotation != null) {
        return jsonPropertyAnnotation.value();
      } else {
        return field.getName();
      }
    }

  }
}
