package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaKeyword;
import com.github.victools.jsonschema.generator.TypeScope;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import lombok.extern.slf4j.Slf4j;

/**
 * Workaround to transform constants to enums in {@code allOf} definitions, which can occur after
 * using {@link JacksonModule}.
 */
@Slf4j
public class ConstToEnumInAllOfModule implements Module {

  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.forTypesInGeneral()
        .withTypeAttributeOverride(this::overrideTypeAttributes);
  }

  private void overrideTypeAttributes(
      ObjectNode attributes,
      TypeScope scope,
      SchemaGenerationContext context) {

    var allOf = attributes.get(context.getKeyword(SchemaKeyword.TAG_ALLOF));
    if (allOf != null && allOf.isArray()) {
      for (var t : allOf) {
        var properties = t.get("properties");
        if (properties != null) {
          for (var p : properties) {
            if (p.isObject() && p.has(context.getKeyword(SchemaKeyword.TAG_CONST))) {
              var objectNode = (ObjectNode) p;
              var value = objectNode.get(context.getKeyword(SchemaKeyword.TAG_CONST));
              objectNode.remove(context.getKeyword(SchemaKeyword.TAG_CONST));
              objectNode.putArray(context.getKeyword(SchemaKeyword.TAG_ENUM)).add(value);
            }
          }
        }
      }
    }
  }
}
