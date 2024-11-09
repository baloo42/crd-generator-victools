package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.TypeScope;
import io.fabric8.crd.generator.victools.spi.KubernetesSchemaKeyword;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotation;
import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotationOnFieldAndGetter;

@RequiredArgsConstructor
public abstract class AbstractExternalDocsModule<T extends Annotation> implements Module {

  private final Class<T> annotationClass;
  private final Function<T, ExternalDocsInfo> mapper;

  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.forFields().withInstanceAttributeOverride(this::overrideInstanceAttributes);
    builder.forMethods().withInstanceAttributeOverride(this::overrideInstanceAttributes);
    builder.forTypesInGeneral().withTypeAttributeOverride(this::overrideTypeAttributes);
  }

  private void overrideTypeAttributes(
      ObjectNode attributes,
      TypeScope scope,
      SchemaGenerationContext schemaGenerationContext) {

    processExternalDocsAnnotation(attributes, () -> findExternalDocsAnnotation(scope));
  }

  private void overrideInstanceAttributes(
      ObjectNode attributes,
      MemberScope<?, ?> scope,
      SchemaGenerationContext schemaGenerationContext) {

    processExternalDocsAnnotation(attributes, () -> findExternalDocsAnnotation(scope));
  }

  private void processExternalDocsAnnotation(
      ObjectNode attributes,
      Supplier<Optional<T>> supplier) {

    supplier.get()
        .map(mapper)
        .filter(ExternalDocsInfo::isNotEmpty)
        .ifPresent(info -> {
          var externalDocs = attributes.putObject(KubernetesSchemaKeyword.EXTERNAL_DOCS.getValue());
          info.getDescription().ifPresent(s -> externalDocs.put("description", s));
          info.getUrl().ifPresent(s -> externalDocs.put("url", s));
        });
  }

  private Optional<T> findExternalDocsAnnotation(TypeScope scope) {
    return findAnnotation(scope, annotationClass);
  }

  private Optional<T> findExternalDocsAnnotation(MemberScope<?, ?> scope) {
    return findAnnotationOnFieldAndGetter(scope, annotationClass);
  }

  public record ExternalDocsInfo(String description, String url) {

    boolean isNotEmpty() {
      return description != null || url != null;
    }

    Optional<String> getDescription() {
      return Optional.ofNullable(description);
    }

    Optional<String> getUrl() {
      return Optional.ofNullable(url);
    }
  }
}
