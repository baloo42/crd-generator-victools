package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaKeyword;
import io.fabric8.crd.generator.victools.CustomResourceContext;
import io.fabric8.crd.generator.victools.model.PrinterColumnInfo;
import io.fabric8.crd.generator.victools.model.ValidationRuleInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

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
  @NonNull
  private final List<MetadataProvider> metadataProviders;

  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.forFields().withInstanceAttributeOverride(this::overrideInstanceAttributes);
    builder.forMethods().withInstanceAttributeOverride(this::overrideInstanceAttributes);
  }

  private void overrideInstanceAttributes(ObjectNode attributes, MemberScope<?, ?> scope, SchemaGenerationContext context) {
    var id = UUID.randomUUID().toString();
    var isIdRequired = new AtomicBoolean(false);

    for (MetadataProvider provider : metadataProviders) {
      if (provider.isSpecReplicasField(scope)) {
        customResourceContext.setSpecReplicasPath(id, true);
        isIdRequired.set(true);
      }
      if (provider.isStatusReplicasField(scope)) {
        customResourceContext.setStatusReplicasPath(id, true);
        isIdRequired.set(true);
      }
      if (provider.isLabelSelectorField(scope)) {
        customResourceContext.setLabelSelectorPath(id, true);
        isIdRequired.set(true);
      }
      if (provider.isSelectableField(scope)) {
        customResourceContext.setSelectableFieldPath(id, true);
        isIdRequired.set(true);
      }

      var printerColumnOptional = provider.findPrinterColumn(scope);
      if (printerColumnOptional.isPresent()) {
        customResourceContext.setPrinterColumnInfo(id, printerColumnOptional.get());
        isIdRequired.set(true);
      }

      var validationRules = provider.getValidationRules(scope);
      if (!validationRules.isEmpty()) {
        validationRules.forEach(rule -> customResourceContext.addValidationRule(id, rule));
        isIdRequired.set(true);
      }
    }

    if (isIdRequired.get()) {
      attributes.put("id", id);
    }
  }

  public interface MetadataProvider {

    default boolean isSpecReplicasField(MemberScope<?, ?> scope) {
      return false;
    }

    default boolean isStatusReplicasField(MemberScope<?, ?> scope) {
      return false;
    }

    default boolean isLabelSelectorField(MemberScope<?, ?> scope) {
      return false;
    }

    default boolean isSelectableField(MemberScope<?, ?> scope) {
      return false;
    }

    default List<ValidationRuleInfo> getValidationRules(MemberScope<?, ?> scope) {
      return Collections.emptyList();
    }

    default Optional<PrinterColumnInfo> findPrinterColumn(MemberScope<?, ?> scope) {
      return Optional.empty();
    }
  }
}
