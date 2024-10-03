package io.fabric8.crd.generator.victools.approvaltests.schemafrom;

import io.fabric8.crd.generator.annotation.SchemaFrom;
import lombok.Data;

@Data
public class SchemaFromSpec {

  @SchemaFrom(type = FooExtractor.class)
  public Foo foo;

}
