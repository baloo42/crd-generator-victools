package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.TypeScope;
import io.fabric8.crd.generator.victools.annotation.ExternalDocs;
import io.fabric8.crd.generator.victools.spi.KubernetesSchemaKeyword;

import java.util.Optional;
import java.util.function.Supplier;

import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.emptyToNull;
import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotation;
import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotationOnFieldAndGetter;

public class ExternalDocsModule implements Module {

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
      Supplier<Optional<ExternalDocs>> supplier) {

    supplier.get()
        .map(ExternalDocsInfo::from)
        .filter(ExternalDocsInfo::isNotEmpty)
        .ifPresent(info -> {
          var externalDocs = attributes.putObject(KubernetesSchemaKeyword.EXTERNAL_DOCS.getValue());
          info.getDescription().ifPresent(s -> externalDocs.put("description", s));
          info.getUrl().ifPresent(s -> externalDocs.put("url", s));
        });
  }

  private Optional<ExternalDocs> findExternalDocsAnnotation(TypeScope scope) {
    return findAnnotation(scope, ExternalDocs.class);
  }

  private Optional<ExternalDocs> findExternalDocsAnnotation(MemberScope<?, ?> scope) {
    return findAnnotationOnFieldAndGetter(scope, ExternalDocs.class);
  }

  private record ExternalDocsInfo(String description, String url) {
    boolean isNotEmpty() {
      return description != null || url != null;
    }

    Optional<String> getDescription() {
      return Optional.ofNullable(description);
    }

    Optional<String> getUrl() {
      return Optional.ofNullable(url);
    }

    static ExternalDocsInfo from(ExternalDocs annotation) {
      return new ExternalDocsInfo(
          emptyToNull(annotation.description()),
          emptyToNull(annotation.url()));
    }
  }
}
