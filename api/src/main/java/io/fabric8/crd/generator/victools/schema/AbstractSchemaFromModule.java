package io.fabric8.crd.generator.victools.schema;

import com.github.victools.jsonschema.generator.CustomPropertyDefinition;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.Function;

@RequiredArgsConstructor
public abstract class AbstractSchemaFromModule<T extends Annotation> implements Module {

  private final Class<T> annotationClass;
  private final Function<T, Type> typeMapper;

  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.forFields().withCustomDefinitionProvider((scope, context) -> {
      var schemaFromAnnotation = scope.getAnnotationConsideringFieldAndGetter(annotationClass);
      if (schemaFromAnnotation == null) {
        return null;
      }
      var resolvedType = context.getTypeContext().resolve(typeMapper.apply(schemaFromAnnotation));
      return new CustomPropertyDefinition(context.createDefinition(resolvedType));
    });
  }
}
