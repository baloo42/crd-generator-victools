package io.fabric8.crd.generator.victools.schema.fkc;

import com.github.victools.jsonschema.generator.FieldScope;
import io.fabric8.crd.generator.victools.schema.MetadataModule;
import io.fabric8.kubernetes.model.annotation.LabelSelector;
import io.fabric8.kubernetes.model.annotation.SpecReplicas;
import io.fabric8.kubernetes.model.annotation.StatusReplicas;

import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotation;

public class FkcScaleSubresourceProvider implements MetadataModule.MetadataProvider {

  @Override
  public boolean isSpecReplicasField(FieldScope scope) {
    return findAnnotation(scope, SpecReplicas.class).isPresent();
  }

  @Override
  public boolean isStatusReplicasField(FieldScope scope) {
    return findAnnotation(scope, StatusReplicas.class).isPresent();
  }

  @Override
  public boolean isLabelSelectorField(FieldScope scope) {
    return findAnnotation(scope, LabelSelector.class).isPresent();
  }

}
