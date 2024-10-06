package io.fabric8.crd.generator.victools.approvaltests.replica;

import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@Version("v1alpha1")
@Group("samples.fabric8.io")
@EqualsAndHashCode(callSuper = true)
public class Replica extends CustomResource<ReplicaSpec, ReplicaStatus> {

  private ReplicaSpec spec;
  private ReplicaStatus status;
}
