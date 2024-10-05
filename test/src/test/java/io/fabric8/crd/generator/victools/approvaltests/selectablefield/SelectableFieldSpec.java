package io.fabric8.crd.generator.victools.approvaltests.selectablefield;

import io.fabric8.crd.generator.victools.annotation.SelectableField;
import lombok.Data;

@Data
public class SelectableFieldSpec {

  @SelectableField
  private String id;

  private DeepLevel1 deepLevel1;

  @Data
  static class DeepLevel1 {
    // targeted from @AdditionalSelectableField
    private String name;

    @SelectableField
    private Integer fromLevel1;

    private DeepLevel2 deepLevel2;
  }

  @Data
  static class DeepLevel2 {
    @SelectableField
    private Boolean fromLevel2;
  }

}
