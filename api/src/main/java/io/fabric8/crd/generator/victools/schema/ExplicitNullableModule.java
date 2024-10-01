package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaKeyword;
import com.github.victools.jsonschema.generator.TypeScope;
import io.fabric8.crd.generator.victools.spi.KubernetesSchemaKeyword;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExplicitNullableModule implements Module {

  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.forTypesInGeneral().withTypeAttributeOverride(this::overrideTypeAttributes);
  }

  private void overrideTypeAttributes(
      ObjectNode attributes,
      TypeScope typeScope,
      SchemaGenerationContext schemaGenerationContext) {
    reduceTypeArray(attributes, schemaGenerationContext);
  }

  private void reduceTypeArray(ObjectNode attributes, SchemaGenerationContext ctx) {
    var type = attributes.get(ctx.getKeyword(SchemaKeyword.TAG_TYPE));
    if (type != null && type.isArray()) {
      String typeString = ctx.getKeyword(SchemaKeyword.TAG_TYPE_OBJECT);
      boolean isNullable = false;
      for (var n : type) {
        if (n.isTextual()) {
          if (n.asText().equals(ctx.getKeyword(SchemaKeyword.TAG_TYPE_NULL))) {
            isNullable = true;
          } else {
            typeString = n.asText();
          }
        }
      }
      attributes.put(ctx.getKeyword(SchemaKeyword.TAG_TYPE), typeString);
      if (isNullable) {
        attributes.put(KubernetesSchemaKeyword.NULLABLE.getValue(), true);
      }
    }
  }
}
