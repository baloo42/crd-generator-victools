package io.fabric8.crd.generator.victools.schema;

import com.github.victools.jsonschema.generator.MemberScope;
import io.fabric8.crd.generator.victools.annotation.LabelSelector;
import io.fabric8.crd.generator.victools.annotation.SpecReplicas;
import io.fabric8.crd.generator.victools.annotation.StatusReplicas;

import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotation;

public class ScaleSubresourceProvider implements MetadataModule.MetadataProvider {

  @Override
  public boolean isSpecReplicasField(MemberScope<?, ?> scope) {
    return findAnnotation(scope, SpecReplicas.class).isPresent();
  }

  @Override
  public boolean isStatusReplicasField(MemberScope<?, ?> scope) {
    return findAnnotation(scope, StatusReplicas.class).isPresent();
  }

  @Override
  public boolean isLabelSelectorField(MemberScope<?, ?> scope) {
    return findAnnotation(scope, LabelSelector.class).isPresent();
  }

}
