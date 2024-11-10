package io.fabric8.crd.generator.victools.schema;

import io.fabric8.crd.generator.victools.CustomResourceInfo;
import io.fabric8.crd.generator.victools.SelectableFieldProvider;
import io.fabric8.crd.generator.victools.annotation.AdditionalSelectableField;
import io.fabric8.crd.generator.victools.model.SelectableFieldInfo;

public class AdditionalSelectableFieldProvider extends
    SelectableFieldProvider.TopLevelAnnotationSelectableFieldProvider<AdditionalSelectableField> {

  public AdditionalSelectableFieldProvider(CustomResourceInfo crInfo) {
    super(crInfo, AdditionalSelectableField.class);
  }

  @Override
  protected SelectableFieldInfo map(AdditionalSelectableField annotation) {
    return new SelectableFieldInfo(annotation.jsonPath());
  }
}
