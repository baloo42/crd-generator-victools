package io.fabric8.crd.generator.victools.schema;

import io.fabric8.crd.generator.victools.AdditionalPrinterColumnProvider;
import io.fabric8.crd.generator.victools.CustomResourceInfo;
import io.fabric8.crd.generator.victools.annotation.AdditionalPrinterColumn;
import io.fabric8.crd.generator.victools.model.PrinterColumnInfo;

public class PrinterColumnProvider
    extends AdditionalPrinterColumnProvider.TopLevelAnnotationPrinterColumnProvider<AdditionalPrinterColumn> {

  public PrinterColumnProvider(CustomResourceInfo crInfo) {
    super(crInfo, AdditionalPrinterColumn.class, column -> PrinterColumnInfo.builder()
        .name(column.name())
        .type(column.type().getValue())
        .description(column.description())
        .jsonPath(column.jsonPath())
        .format(column.format().getValue())
        .priority(column.priority())
        .build());
  }
}
