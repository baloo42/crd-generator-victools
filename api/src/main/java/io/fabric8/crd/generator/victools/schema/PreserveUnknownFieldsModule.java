package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.CustomDefinition;
import com.github.victools.jsonschema.generator.CustomDefinitionProviderV2;
import com.github.victools.jsonschema.generator.CustomPropertyDefinition;
import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import io.fabric8.crd.generator.annotation.PreserveUnknownFields;
import io.fabric8.crd.generator.victools.CRDGeneratorContextInternal;
import io.fabric8.crd.generator.victools.CRDGeneratorSchemaOption;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotationConsideringFieldAndGetter;
import static io.fabric8.crd.generator.victools.spi.KubernetesSchemaKeyword.KUBERNETES_PRESERVE_UNKNOWN_FIELDS;

public class PreserveUnknownFieldsModule extends AbstractCRDGeneratorModule {

  private static final Set<Class<?>> IMPLICIT_CLASSES = Set.of(JsonNode.class);

  public PreserveUnknownFieldsModule(CRDGeneratorContextInternal context) {
    super(context);
  }

  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.forFields().withInstanceAttributeOverride(this::overrideInstanceAttributes);
    builder.forMethods().withInstanceAttributeOverride(this::overrideInstanceAttributes);
    builder.forTypesInGeneral().withCustomDefinitionProvider(
        new ImplicitPreserveUnknownFieldsCustomDefinitionProvider(IMPLICIT_CLASSES));
  }

  private void overrideInstanceAttributes(
      ObjectNode attributes,
      MemberScope<?, ?> scope,
      SchemaGenerationContext schemaGenerationContext) {

    if (scope.isFakeContainerItemScope()) {
      return;
    }

    if (scope.getType().isPrimitive()) {
      return;
    }

    if (hasPreserveUnknownFieldsAnnotation(scope)
        || hasImplicitPreserveUnknownFields(scope)) {
      attributes.put(KUBERNETES_PRESERVE_UNKNOWN_FIELDS.getValue(), true);
    }
  }

  private boolean hasPreserveUnknownFieldsAnnotation(MemberScope<?, ?> scope) {
    return findAnnotationConsideringFieldAndGetter(scope, PreserveUnknownFields.class)
        .isPresent();
  }

  private boolean hasImplicitPreserveUnknownFields(MemberScope<?, ?> scope) {
    if (!getContext().isEnabled(CRDGeneratorSchemaOption.IMPLICIT_PRESERVE_UNKNOWN_FIELDS)) {
      return false;
    }

    var beanDescription = getContext().introspect(scope.getType().getErasedType());
    return beanDescription.findAnyGetter() != null
        || beanDescription.findAnySetterAccessor() != null;
  }

  @RequiredArgsConstructor
  private static class ImplicitPreserveUnknownFieldsCustomDefinitionProvider
      implements CustomDefinitionProviderV2 {

    private final Set<Class<?>> classes;

    @Override
    public CustomDefinition provideCustomSchemaDefinition(
        ResolvedType javaType,
        SchemaGenerationContext context) {

      if (!classes.contains(javaType.getErasedType())) {
        return null;
      }

      var objectNode = context.getGeneratorConfig().createObjectNode();
      objectNode.put(KUBERNETES_PRESERVE_UNKNOWN_FIELDS.getValue(), true);
      return new CustomPropertyDefinition(objectNode);
    }
  }
}
