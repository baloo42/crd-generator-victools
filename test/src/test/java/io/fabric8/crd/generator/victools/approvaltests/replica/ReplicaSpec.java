package io.fabric8.crd.generator.victools.approvaltests.replica;

import io.fabric8.kubernetes.model.annotation.SpecReplicas;
import lombok.Data;

@Data
public class ReplicaSpec {

  @SpecReplicas
  private int replicas;
}
