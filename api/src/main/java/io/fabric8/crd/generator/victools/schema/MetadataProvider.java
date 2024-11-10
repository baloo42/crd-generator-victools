package io.fabric8.crd.generator.victools.schema;

import com.github.victools.jsonschema.generator.FieldScope;
import io.fabric8.crd.generator.victools.annotation.LabelSelector;
import io.fabric8.crd.generator.victools.annotation.PrinterColumn;
import io.fabric8.crd.generator.victools.annotation.SelectableField;
import io.fabric8.crd.generator.victools.annotation.SpecReplicas;
import io.fabric8.crd.generator.victools.annotation.StatusReplicas;
import io.fabric8.crd.generator.victools.model.PrinterColumnInfo;

import java.util.Optional;

import static io.fabric8.crd.generator.victools.CRDUtils.emptyToNull;
import static io.fabric8.crd.generator.victools.CRDUtils.zeroToNull;
import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotation;

public class MetadataProvider implements MetadataModule.MetadataProvider {

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
    return findAnnotation(scope, SelectableField.class).isPresent();
  }

  @Override
  public Optional<PrinterColumnInfo> findPrinterColumn(FieldScope scope) {
    return findAnnotation(scope, PrinterColumn.class)
        .map(annotation -> PrinterColumnInfo.builder()
            .name(emptyToNull(annotation.name()))
            .format(annotation.format().getValue())
            .priority(zeroToNull(annotation.priority()))
            .build());
  }

}
