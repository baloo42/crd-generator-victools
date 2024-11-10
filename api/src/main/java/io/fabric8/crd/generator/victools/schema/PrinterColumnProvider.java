package io.fabric8.crd.generator.victools.schema;

import com.github.victools.jsonschema.generator.FieldScope;
import io.fabric8.crd.generator.victools.annotation.PrinterColumn;
import io.fabric8.crd.generator.victools.model.PrinterColumnInfo;

import java.util.Optional;

import static io.fabric8.crd.generator.victools.CRDUtils.emptyToNull;
import static io.fabric8.crd.generator.victools.CRDUtils.zeroToNull;
import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotation;

public class PrinterColumnProvider implements MetadataModule.MetadataProvider {

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
