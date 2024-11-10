package io.fabric8.crd.generator.victools.schema;

import io.fabric8.crd.generator.victools.AdditionalPrinterColumnProvider;
import io.fabric8.crd.generator.victools.CustomResourceInfo;
import io.fabric8.crd.generator.victools.annotation.AdditionalPrinterColumn;
import io.fabric8.crd.generator.victools.model.PrinterColumnInfo;

import static io.fabric8.crd.generator.victools.CRDUtils.emptyToNull;
import static io.fabric8.crd.generator.victools.CRDUtils.zeroToNull;

public class PrinterColumnProvider
    extends AdditionalPrinterColumnProvider.TopLevelAnnotationPrinterColumnProvider<AdditionalPrinterColumn> {

  public PrinterColumnProvider(CustomResourceInfo crInfo) {
    super(crInfo, AdditionalPrinterColumn.class);
  }

  @Override
  protected PrinterColumnInfo map(AdditionalPrinterColumn annotation) {
    return PrinterColumnInfo.builder()
        .name(emptyToNull(annotation.name()))
        .type(annotation.type().getValue())
        .description(emptyToNull(annotation.description()))
        .jsonPath(annotation.jsonPath())
        .format(annotation.format().getValue())
        .priority(zeroToNull(annotation.priority()))
        .build();
  }
}
