package io.fabric8.crd.generator.victools.schema;

import com.github.victools.jsonschema.generator.FieldScope;
import io.fabric8.crd.generator.victools.annotation.SelectableField;

import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotation;

public class SelectableFieldProvider implements MetadataModule.MetadataProvider {
  @Override
  public boolean isSelectableField(FieldScope scope) {
    return findAnnotation(scope, SelectableField.class).isPresent();
  }
}
