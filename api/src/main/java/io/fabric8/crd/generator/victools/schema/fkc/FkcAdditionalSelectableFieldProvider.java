package io.fabric8.crd.generator.victools.schema.fkc;

import io.fabric8.crd.generator.annotation.AdditionalSelectableField;
import io.fabric8.crd.generator.victools.CustomResourceInfo;
import io.fabric8.crd.generator.victools.SelectableFieldProvider;
import io.fabric8.crd.generator.victools.model.SelectableFieldInfo;

public class FkcAdditionalSelectableFieldProvider extends
    SelectableFieldProvider.TopLevelAnnotationSelectableFieldProvider<AdditionalSelectableField> {

  public FkcAdditionalSelectableFieldProvider(CustomResourceInfo crInfo) {
    super(crInfo, AdditionalSelectableField.class);
  }

  @Override
  protected SelectableFieldInfo map(AdditionalSelectableField annotation) {
    return new SelectableFieldInfo(annotation.jsonPath());
  }
}
