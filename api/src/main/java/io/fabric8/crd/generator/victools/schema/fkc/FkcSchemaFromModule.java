package io.fabric8.crd.generator.victools.schema.fkc;

import io.fabric8.crd.generator.annotation.SchemaFrom;
import io.fabric8.crd.generator.victools.schema.AbstractSchemaFromModule;

public class FkcSchemaFromModule extends AbstractSchemaFromModule<SchemaFrom> {

  public FkcSchemaFromModule() {
    super(SchemaFrom.class, SchemaFrom::type);
  }

}
