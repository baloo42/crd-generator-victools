package io.fabric8.crd.generator.victools.approvaltests.printercolum;

import io.fabric8.crd.generator.annotation.AdditionalPrinterColumn;
import io.fabric8.crd.generator.victools.approvaltests.DummyStatus;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1alpha1")
@Group("samples.fabric8.io")
@AdditionalPrinterColumn(jsonPath = ".spec.deepLevel1.name")
public class FkcPrinterColumn extends CustomResource<FkcPrinterColumnSpec, DummyStatus> {
}
