package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.TypeScope;
import io.fabric8.crd.generator.victools.annotation.MapType;
import io.fabric8.crd.generator.victools.spi.KubernetesSchemaKeyword;

import java.util.Optional;
import java.util.function.Supplier;

import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotation;
import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotationConsideringFieldAndGetter;

public class KubernetesMapTypeModule implements Module {

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
      Supplier<Optional<MapType>> supplier) {

    supplier.get()
        .ifPresent(info -> attributes.put(KubernetesSchemaKeyword.KUBERNETES_MAP_TYPE.getValue(),
            info.value().name().toLowerCase()));
  }

  private Optional<MapType> findMapTypeAnnotation(TypeScope scope) {
    return findAnnotation(scope, MapType.class);
  }

  private Optional<MapType> findMapTypeAnnotation(MemberScope<?, ?> scope) {
    return findAnnotationConsideringFieldAndGetter(scope, MapType.class);
  }
}
