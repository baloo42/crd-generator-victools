package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.TypeAttributeOverrideV2;
import com.github.victools.jsonschema.generator.TypeScope;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.runtime.RawExtension;

import java.util.Set;
import java.util.stream.Collectors;

import static io.fabric8.crd.generator.victools.spi.KubernetesSchemaKeyword.KUBERNETES_EMBEDDED_RESOURCE;

public class EmbeddedResourceModule implements Module {

  private static final Set<Class<?>> IMPLICIT_CLASSES = Set.of(
      RawExtension.class,
      GenericKubernetesResource.class,
      HasMetadata.class);

  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.forTypesInGeneral()
        .withTypeAttributeOverride(new ImplicitEmbeddedResourceCustomDefinitionProvider(IMPLICIT_CLASSES));
  }

  private static class ImplicitEmbeddedResourceCustomDefinitionProvider
      implements TypeAttributeOverrideV2 {

    private final Set<Class<?>> classes;
    private final Set<Class<?>> interfaces;

    public ImplicitEmbeddedResourceCustomDefinitionProvider(Set<Class<?>> classes) {
      this.classes = classes;
      this.interfaces = classes.stream()
          .filter(Class::isInterface)
          .collect(Collectors.toSet());
    }

    private boolean matches(Class<?> clazz) {
      if (classes.contains(clazz)) {
        return true;
      }

      for (var i : interfaces) {
        if (i.isAssignableFrom(clazz)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public void overrideTypeAttributes(
        ObjectNode attributes,
        TypeScope scope,
        SchemaGenerationContext context) {

      if (!matches(scope.getType().getErasedType())) {
        return;
      }
      attributes.put(KUBERNETES_EMBEDDED_RESOURCE.getValue(), true);
    }
  }
}
