package io.fabric8.crd.generator.victools.schema;

import com.github.victools.jsonschema.generator.CustomPropertyDefinition;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import io.fabric8.crd.generator.annotation.SchemaFrom;

public class SchemaFromModule implements Module {
  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.forFields().withCustomDefinitionProvider((scope, context) -> {
      var schemaFromAnnotation = scope.getAnnotationConsideringFieldAndGetter(SchemaFrom.class);
      if (schemaFromAnnotation == null) {
        return null;
      }
      var resolvedType = context.getTypeContext().resolve(schemaFromAnnotation.type());
      return new CustomPropertyDefinition(context.createDefinition(resolvedType));
    });
  }
}
