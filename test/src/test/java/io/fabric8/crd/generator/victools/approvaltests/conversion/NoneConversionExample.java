package io.fabric8.crd.generator.victools.approvaltests.conversion;

import io.fabric8.crd.generator.victools.annotation.NoneConversion;
import io.fabric8.crd.generator.victools.approvaltests.DummySpec;
import io.fabric8.crd.generator.victools.approvaltests.DummyStatus;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;

@NoneConversion
@Version("v1")
@Kind("NoneConversion")
@Group("samples.fabric8.io")
public class NoneConversionExample extends CustomResource<DummySpec, DummyStatus> {
}
