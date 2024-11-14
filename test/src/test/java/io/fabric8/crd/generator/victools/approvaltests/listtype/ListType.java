package io.fabric8.crd.generator.victools.approvaltests.listtype;

import io.fabric8.crd.generator.victools.approvaltests.DummyStatus;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1alpha1")
@Group("samples.fabric8.io")
public class ListType extends CustomResource<ListTypeSpec, DummyStatus> {
}
