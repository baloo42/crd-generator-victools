package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.FieldScope;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaKeyword;
import com.github.victools.jsonschema.generator.TypeScope;
import io.fabric8.crd.generator.annotation.PrinterColumn;
import io.fabric8.crd.generator.victools.CustomResourceContext;
import io.fabric8.crd.generator.victools.PrinterColumnInfo;
import io.fabric8.kubernetes.model.annotation.LabelSelector;
import io.fabric8.kubernetes.model.annotation.SpecReplicas;
import io.fabric8.kubernetes.model.annotation.StatusReplicas;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.emptyToNull;
import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotation;
import static java.util.Optional.ofNullable;

/**
 * This module collects metadata and saves it to the {@link CustomResourceContext}, so that
 * it can be used in the post-processing phase. To be able to correlate the metadata
 * to a field, the metadata is associated with a generated ID and the ID is
 * added to the attributes of the field using {@link SchemaKeyword#TAG_ID}.
 * This ID should be removed in the post-processing phase as it has no further meaning.
 *
 * @see io.fabric8.crd.generator.victools.v1.PathAwareSchemaPropsVisitor
 * @see io.fabric8.crd.generator.victools.CustomResourceContext
 */
@Slf4j
@RequiredArgsConstructor
public class MetadataModule implements Module {

  @NonNull
  private final CustomResourceContext customResourceContext;

  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.forFields().withInstanceAttributeOverride(this::onInstanceAttributes);
    builder.forTypesInGeneral().withTypeAttributeOverride(this::onType);
  }

  private void onType(ObjectNode attributes, TypeScope scope, SchemaGenerationContext context) {
    customResourceContext.collectDependentClasses(scope.getType().getErasedType());
  }

  private void onInstanceAttributes(ObjectNode attributes, FieldScope scope, SchemaGenerationContext context) {
    var id = UUID.randomUUID().toString();
    var isIdRequired = new AtomicBoolean(false);

    findAnnotation(scope, PrinterColumn.class)
        .map(annotation -> mapPrinterColumn(attributes, annotation))
        .ifPresent(info -> {
          customResourceContext.setPrinterColumnInfo(id, info);
          isIdRequired.set(true);
        });

    findAnnotation(scope, SpecReplicas.class)
        .ifPresent(annotation -> {
          customResourceContext.setSpecReplicasPath(id, true);
          isIdRequired.set(true);
        });

    findAnnotation(scope, StatusReplicas.class)
        .ifPresent(annotation -> {
          customResourceContext.setStatusReplicasPath(id, true);
          isIdRequired.set(true);
        });

    findAnnotation(scope, LabelSelector.class)
        .ifPresent(annotation -> {
          customResourceContext.setLabelSelectorPath(id, true);
          isIdRequired.set(true);
        });

    if (isIdRequired.get()) {
      attributes.put("id", id);
    }
  }

  private PrinterColumnInfo mapPrinterColumn(ObjectNode attributes, PrinterColumn annotation) {
    var name = emptyToNull(annotation.name());
    var format = ofNullable(emptyToNull(annotation.format()))
        .orElseGet(() -> ofNullable(attributes.get("format"))
            .map(JsonNode::asText)
            .orElse(null));
    var priority = annotation.priority();
    return new PrinterColumnInfo(name, format, priority);
  }
}
