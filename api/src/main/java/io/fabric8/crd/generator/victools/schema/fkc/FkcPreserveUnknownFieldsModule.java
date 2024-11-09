package io.fabric8.crd.generator.victools.schema.fkc;

import io.fabric8.crd.generator.annotation.PreserveUnknownFields;
import io.fabric8.crd.generator.victools.CRDGeneratorContextInternal;
import io.fabric8.crd.generator.victools.schema.AbstractPreserveUnknownFieldsModule;

public class FkcPreserveUnknownFieldsModule extends
  AbstractPreserveUnknownFieldsModule<PreserveUnknownFields> {

  public FkcPreserveUnknownFieldsModule(CRDGeneratorContextInternal context) {
    super(PreserveUnknownFields.class, context);
  }

}
