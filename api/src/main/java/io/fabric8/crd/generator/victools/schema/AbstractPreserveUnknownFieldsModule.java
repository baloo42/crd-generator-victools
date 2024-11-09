package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.CustomDefinition;
import com.github.victools.jsonschema.generator.CustomPropertyDefinition;
import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import io.fabric8.crd.generator.victools.CRDGeneratorContextInternal;
import io.fabric8.crd.generator.victools.CRDGeneratorSchemaOption;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.util.Set;

import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotationOnFieldAndGetter;
import static io.fabric8.crd.generator.victools.spi.KubernetesSchemaKeyword.KUBERNETES_PRESERVE_UNKNOWN_FIELDS;

@RequiredArgsConstructor
public class AbstractPreserveUnknownFieldsModule<T extends Annotation> implements Module {
  private static final Set<Class<?>> IMPLICIT_CLASSES = Set.of(JsonNode.class);

  private final Class<T> annotationClass;
  private final CRDGeneratorContextInternal context;

  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.forFields().withInstanceAttributeOverride(this::overrideInstanceAttributes);
    builder.forMethods().withInstanceAttributeOverride(this::overrideInstanceAttributes);
    builder.forTypesInGeneral().withCustomDefinitionProvider(this::provideCustomSchemaDefinition);
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

  private CustomDefinition provideCustomSchemaDefinition(
    ResolvedType javaType,
    SchemaGenerationContext context) {

    if (!IMPLICIT_CLASSES.contains(javaType.getErasedType())) {
      return null;
    }

    var objectNode = context.getGeneratorConfig().createObjectNode();
    objectNode.put(KUBERNETES_PRESERVE_UNKNOWN_FIELDS.getValue(), true);
    return new CustomPropertyDefinition(objectNode);
  }

  private boolean hasPreserveUnknownFieldsAnnotation(MemberScope<?, ?> scope) {
    return findAnnotationOnFieldAndGetter(scope, annotationClass)
      .isPresent();
  }

  private boolean hasImplicitPreserveUnknownFields(MemberScope<?, ?> scope) {
    if (!context.isEnabled(CRDGeneratorSchemaOption.IMPLICIT_PRESERVE_UNKNOWN_FIELDS)) {
      return false;
    }

    var beanDescription = context.introspect(scope.getType().getErasedType());
    return beanDescription.findAnyGetter() != null
           || beanDescription.findAnySetterAccessor() != null;
  }
}
