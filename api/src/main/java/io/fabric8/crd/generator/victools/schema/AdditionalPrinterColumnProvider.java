package io.fabric8.crd.generator.victools.schema;

import io.fabric8.crd.generator.victools.CustomResourceInfo;
import io.fabric8.crd.generator.victools.PrinterColumnProvider;
import io.fabric8.crd.generator.victools.annotation.AdditionalPrinterColumn;
import io.fabric8.crd.generator.victools.model.PrinterColumnInfo;

import static io.fabric8.crd.generator.victools.CRDUtils.emptyToNull;
import static io.fabric8.crd.generator.victools.CRDUtils.zeroToNull;

public class AdditionalPrinterColumnProvider
    extends PrinterColumnProvider.TopLevelAnnotationPrinterColumnProvider<AdditionalPrinterColumn> {

  public AdditionalPrinterColumnProvider(CustomResourceInfo crInfo) {
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
