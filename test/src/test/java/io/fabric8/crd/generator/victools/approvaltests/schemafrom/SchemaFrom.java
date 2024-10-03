package io.fabric8.crd.generator.victools.approvaltests.schemafrom;

import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1alpha1")
@Group("samples.fabric8.io")
public class SchemaFrom extends CustomResource<SchemaFromSpec, Void> {
}
