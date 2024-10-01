package io.fabric8.crd.generator.victools.approvaltests.printercolum;

import io.fabric8.crd.generator.annotation.PrinterColumn;
import lombok.Data;

@Data
public class PrinterColumnSpec {

  @PrinterColumn
  private String id;

  private DeepLevel1 deepLevel1;

  @Data
  static class DeepLevel1 {
    // targeted from @AdditionalPrinterColumn
    private String name;

    @PrinterColumn
    private Integer fromLevel1;

    private DeepLevel2 deepLevel2;
  }

  @Data
  static class DeepLevel2 {
    @PrinterColumn
    private Boolean fromLevel2;
  }

}
