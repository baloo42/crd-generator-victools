package io.fabric8.crd.generator.victools.schema;

import io.fabric8.crd.generator.victools.annotation.MapType;

public class KubernetesMapTypeModule extends AbstractKubernetesMapTypeModule<MapType> {

  public KubernetesMapTypeModule() {
    super(MapType.class, annotation -> annotation.value().name().toLowerCase());
  }
}
