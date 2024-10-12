package io.fabric8.crd.generator.victools.approvaltests.replica;

import io.fabric8.kubernetes.model.annotation.LabelSelector;
import io.fabric8.kubernetes.model.annotation.StatusReplicas;
import lombok.Data;

@Data
public class ReplicaStatus {

  @StatusReplicas
  int replicas;
  @LabelSelector
  String labelSelector;
}
