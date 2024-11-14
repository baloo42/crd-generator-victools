package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.classmate.members.RawMember;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotationOnFieldAndGetter;
import static io.fabric8.crd.generator.victools.spi.KubernetesSchemaKeyword.KUBERNETES_LIST_MAP_KEYS;
import static io.fabric8.crd.generator.victools.spi.KubernetesSchemaKeyword.KUBERNETES_LIST_TYPE;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractKubernetesListTypeModule<T extends Annotation, K extends Annotation> implements Module {

  private final Class<T> annotationClass;
  private final Class<K> keyAnnotationClass;

  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.forFields().withInstanceAttributeOverride(this::overrideInstanceAttributes);
    builder.forMethods().withInstanceAttributeOverride(this::overrideInstanceAttributes);
  }

  protected abstract String getListType(T annotation);

  protected List<String> getListMapKeys(T annotation) {
    return Collections.emptyList();
  }

  private void overrideInstanceAttributes(
      ObjectNode attributes,
      MemberScope<?, ?> scope,
      SchemaGenerationContext schemaGenerationContext) {

    if (!scope.isContainerType()) {
      return;
    }

    findListTypeAnnotation(scope)
        .ifPresent(t -> onFieldWithAnnotation(attributes, scope, t));
  }

  private void onFieldWithAnnotation(ObjectNode attributes, MemberScope<?, ?> scope, T annotation) {
    var value = getListType(annotation).toLowerCase();
    attributes.put(KUBERNETES_LIST_TYPE.getValue(), value);

    if ("map".equals(value)) {
      var listMapKeysFromAnnotation = getListMapKeys(annotation);
      var listMapKeys = !listMapKeysFromAnnotation.isEmpty()
          ? listMapKeysFromAnnotation.stream().map(TextNode::new).toList()
          : scope.getContainerItemType().getMemberFields().stream()
              .filter(rawField -> Arrays.stream(rawField.getAnnotations()).anyMatch(this::isListMapKey))
              .map(RawMember::getName)
              .map(TextNode::new)
              .toList();

      if (!listMapKeys.isEmpty()) {
        attributes.withArray(KUBERNETES_LIST_MAP_KEYS.getValue()).addAll(listMapKeys);
      }
    }
  }

  private Optional<T> findListTypeAnnotation(MemberScope<?, ?> scope) {
    return findAnnotationOnFieldAndGetter(scope, annotationClass);
  }

  private boolean isListMapKey(Annotation annotation) {
    return isListMapKey(annotation.annotationType());
  }

  private boolean isListMapKey(Class<? extends Annotation> clazz) {
    return keyAnnotationClass.equals(clazz);
  }
}
