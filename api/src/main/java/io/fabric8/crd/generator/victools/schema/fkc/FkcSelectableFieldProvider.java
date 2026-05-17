package io.fabric8.crd.generator.victools.schema.fkc;

import com.github.victools.jsonschema.generator.MemberScope;
import io.fabric8.crd.generator.annotation.SelectableField;
import io.fabric8.crd.generator.victools.schema.MetadataModule;

import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotation;

public class FkcSelectableFieldProvider implements MetadataModule.MetadataProvider {
  @Override
  public boolean isSelectableField(MemberScope<?, ?> scope) {
    return findAnnotation(scope, SelectableField.class).isPresent();
  }
}
