package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.TypeScope;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotation;
import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotationOnFieldAndGetter;
import static io.fabric8.crd.generator.victools.spi.KubernetesSchemaKeyword.KUBERNETES_MAP_TYPE;

@RequiredArgsConstructor
public abstract class AbstractKubernetesMapTypeModule<T extends Annotation> implements Module {

  private final Class<T> annotationClass;
  private final Function<T, String> mapper;

  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.forFields().withInstanceAttributeOverride(this::overrideInstanceAttributes);
    builder.forMethods().withInstanceAttributeOverride(this::overrideInstanceAttributes);
    builder.forTypesInGeneral().withTypeAttributeOverride(this::overrideTypeAttributes);
  }

  private void overrideInstanceAttributes(
      ObjectNode attributes,
      MemberScope<?, ?> scope,
      SchemaGenerationContext schemaGenerationContext) {
    if (scope.isFakeContainerItemScope()) {
      return;
    }

    processMapTypeAnnotation(attributes, () -> findMapTypeAnnotation(scope));
  }

  private void overrideTypeAttributes(
      ObjectNode attributes,
      TypeScope scope,
      SchemaGenerationContext schemaGenerationContext) {

    processMapTypeAnnotation(attributes, () -> findMapTypeAnnotation(scope));
  }

  private void processMapTypeAnnotation(
      ObjectNode attributes,
      Supplier<Optional<T>> supplier) {

    supplier.get()
        .ifPresent(annotation -> attributes.put(KUBERNETES_MAP_TYPE.getValue(), mapper.apply(annotation)));
  }

  private Optional<T> findMapTypeAnnotation(TypeScope scope) {
    return findAnnotation(scope, annotationClass);
  }

  private Optional<T> findMapTypeAnnotation(MemberScope<?, ?> scope) {
    return findAnnotationOnFieldAndGetter(scope, annotationClass);
  }
}
