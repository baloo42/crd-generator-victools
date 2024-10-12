package io.fabric8.crd.generator.victools.approvaltests.conversion.v1;

import io.fabric8.crd.generator.victools.approvaltests.DummyStatus;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("samples.fabric8.io")
@Version(value = "v1", storage = false)
@Kind("WebhookConversion")
public class WebhookConversionExample extends CustomResource<WebhookConversionSpec, DummyStatus> {
}
