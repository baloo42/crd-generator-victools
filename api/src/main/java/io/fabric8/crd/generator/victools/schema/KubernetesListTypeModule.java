package io.fabric8.crd.generator.victools.schema;

import io.fabric8.crd.generator.victools.annotation.ListMapKey;
import io.fabric8.crd.generator.victools.annotation.ListType;

import java.util.Arrays;
import java.util.List;

public class KubernetesListTypeModule extends AbstractKubernetesListTypeModule<ListType, ListMapKey> {

  public KubernetesListTypeModule() {
    super(ListType.class, ListMapKey.class);
  }

  @Override
  protected String getListType(ListType annotation) {
    return annotation.value().name();
  }

  @Override
  protected List<String> getListMapKeys(ListType annotation) {
    return Arrays.asList(annotation.mapKeys());
  }
}
