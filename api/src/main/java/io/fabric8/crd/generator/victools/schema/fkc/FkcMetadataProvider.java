package io.fabric8.crd.generator.victools.schema.fkc;

import com.github.victools.jsonschema.generator.FieldScope;
import io.fabric8.crd.generator.annotation.PrinterColumn;
import io.fabric8.crd.generator.victools.model.PrinterColumnInfo;
import io.fabric8.crd.generator.victools.schema.MetadataModule;
import io.fabric8.kubernetes.model.annotation.LabelSelector;
import io.fabric8.kubernetes.model.annotation.SpecReplicas;
import io.fabric8.kubernetes.model.annotation.StatusReplicas;

import java.util.Optional;

import static io.fabric8.crd.generator.victools.CRDUtils.emptyToNull;
import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotation;
import static java.util.Optional.ofNullable;

public class FkcMetadataProvider implements MetadataModule.MetadataProvider {

  @Override
  public boolean isSpecReplicasField(FieldScope scope) {
    return findAnnotation(scope, SpecReplicas.class).isPresent();
  }

  @Override
  public boolean isStatusReplicasField(FieldScope scope) {
    return findAnnotation(scope, StatusReplicas.class).isPresent();
  }

  @Override
  public boolean isLabelSelectorField(FieldScope scope) {
    return findAnnotation(scope, LabelSelector.class).isPresent();
  }

  @Override
  public boolean isSelectableField(FieldScope scope) {
    // TODO: Add support once updated to fabric8/kubernetes-client v7
    return MetadataModule.MetadataProvider.super.isSelectableField(scope);
  }

  @Override
  public Optional<PrinterColumnInfo> findPrinterColumn(FieldScope scope, @Deprecated String format) {
    return findAnnotation(scope, PrinterColumn.class)
        .map(a -> mapPrinterColumn(a, format));
  }

  private PrinterColumnInfo mapPrinterColumn(PrinterColumn annotation, String formatFrom) {
    var format = ofNullable(emptyToNull(annotation.format()))
        .orElse(formatFrom);

    return PrinterColumnInfo.builder()
        .name(emptyToNull(annotation.name()))
        .format(format)
        .priority(annotation.priority())
        .build();
  }
}
