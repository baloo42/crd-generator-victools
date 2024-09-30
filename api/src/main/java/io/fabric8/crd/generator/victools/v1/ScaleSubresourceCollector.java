package io.fabric8.crd.generator.victools.v1;

import io.fabric8.crd.generator.victools.CustomResourceContext;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceSubresourceScale;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceSubresourceScaleBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.JSONSchemaProps;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Collects scale subresource details.
 * Note that the first path found is used (closest to the root).
 */
@RequiredArgsConstructor
class ScaleSubresourceCollector
    implements PathAwareSchemaPropsVisitor.IdentifiedPropertyVisitor {

  private final CustomResourceContext customResourceContext;

  private final CustomResourceSubresourceScaleBuilder builder
      = new CustomResourceSubresourceScaleBuilder();

  @Override
  public void visit(String id, String path, JSONSchemaProps schema) {
    if (customResourceContext.isSpecReplicasPath(id) && !builder.hasSpecReplicasPath()) {
      builder.withSpecReplicasPath(path);
    }
    if (customResourceContext.isStatusReplicasPath(id) && !builder.hasStatusReplicasPath()) {
      builder.withStatusReplicasPath(path);
    }
    if (customResourceContext.isLabelSelectorPath(id) && !builder.hasLabelSelectorPath()) {
      builder.withLabelSelectorPath(path);
    }
  }

  public Optional<CustomResourceSubresourceScale> findScaleSubresource() {
    if(builder.hasSpecReplicasPath()
       || builder.hasStatusReplicasPath()
       || builder.hasLabelSelectorPath()) {

      return Optional.of(builder.build());
    }
    return Optional.empty();
  }
}
