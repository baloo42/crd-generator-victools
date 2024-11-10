package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.TypeScope;
import io.fabric8.crd.generator.victools.CustomResourceContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class DependentClassesModule implements Module {

  @NonNull
  private final CustomResourceContext customResourceContext;

  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.forTypesInGeneral().withTypeAttributeOverride(this::onType);
  }

  private void onType(ObjectNode attributes, TypeScope scope, SchemaGenerationContext context) {
    collectDependentClassesFromHierarchy(scope.getType().getErasedType());
  }

  public void collectDependentClassesFromHierarchy(Class<?> rawClass) {
    if (rawClass != null && !rawClass.getName().startsWith("java.")
        && customResourceContext.addDependentClass(rawClass.getName())) {

      Stream.of(rawClass.getInterfaces()).forEach(this::collectDependentClassesFromHierarchy);
      collectDependentClassesFromHierarchy(rawClass.getSuperclass());
    }
  }

}
