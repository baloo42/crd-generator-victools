package io.fabric8.crd.generator.victools.approvaltests.replica;

import io.fabric8.kubernetes.model.annotation.SpecReplicas;
import lombok.Data;

@Data
public class ReplicaSpec {

  private String name;

  private int port;

  @SpecReplicas
  private int replicas;
}
