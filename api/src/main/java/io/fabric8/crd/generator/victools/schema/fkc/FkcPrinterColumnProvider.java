package io.fabric8.crd.generator.victools.schema.fkc;

import com.github.victools.jsonschema.generator.MemberScope;
import io.fabric8.crd.generator.annotation.PrinterColumn;
import io.fabric8.crd.generator.victools.model.PrinterColumnInfo;
import io.fabric8.crd.generator.victools.schema.MetadataModule;

import java.util.Optional;

import static io.fabric8.crd.generator.victools.CRDUtils.emptyToNull;
import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotation;

public class FkcPrinterColumnProvider implements MetadataModule.MetadataProvider {

  @Override
  public Optional<PrinterColumnInfo> findPrinterColumn(MemberScope<?, ?> scope) {
    return findAnnotation(scope, PrinterColumn.class)
        .map(this::mapPrinterColumn);
  }

  private PrinterColumnInfo mapPrinterColumn(PrinterColumn annotation) {
    return PrinterColumnInfo.builder()
        .name(emptyToNull(annotation.name()))
        .format(emptyToNull(annotation.format()))
        .priority(annotation.priority()) // TODO: omit zero
        .build();
  }
}
