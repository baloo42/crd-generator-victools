package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaKeyword;
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
    builder.forFields().withInstanceAttributeOverride((attributes, member, context) -> {
      var id = UUID.randomUUID().toString();
      var isIdRequired = new AtomicBoolean(false);

      findAnnotation(member, PrinterColumn.class)
          .map(annotation -> mapPrinterColumn(attributes, annotation))
          .ifPresent(info -> {
            customResourceContext.setPrinterColumnInfo(id, info);
            isIdRequired.set(true);
          });

      findAnnotation(member, SpecReplicas.class)
          .ifPresent(annotation -> {
            customResourceContext.setSpecReplicasPath(id, true);
            isIdRequired.set(true);
          });

      findAnnotation(member, StatusReplicas.class)
          .ifPresent(annotation -> {
            customResourceContext.setStatusReplicasPath(id, true);
            isIdRequired.set(true);
          });

      findAnnotation(member, LabelSelector.class)
          .ifPresent(annotation -> {
            customResourceContext.setLabelSelectorPath(id, true);
            isIdRequired.set(true);
          });

      if(isIdRequired.get()){
        attributes.put("id", id);
      }
    });
  }

  private PrinterColumnInfo mapPrinterColumn(ObjectNode attributes, PrinterColumn annotation) {
    String name = emptyToNull(annotation.name());
    String format = ofNullable(emptyToNull(annotation.format()))
        .orElseGet(() -> ofNullable(attributes.get("format"))
            .map(JsonNode::asText)
            .orElse(null));
    return new PrinterColumnInfo(name, format, annotation.priority());
  }
}
