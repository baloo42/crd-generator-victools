package io.fabric8.crd.generator.victools.approvaltests.selectablefield;

import io.fabric8.crd.generator.victools.annotation.AdditionalSelectableField;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1alpha1")
@Group("samples.fabric8.io")
@AdditionalSelectableField(jsonPath = ".spec.deepLevel1.name")
public class SelectableField extends CustomResource<SelectableFieldSpec, Void> {
}
