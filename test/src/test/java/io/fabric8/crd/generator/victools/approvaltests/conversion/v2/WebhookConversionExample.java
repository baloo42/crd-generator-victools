package io.fabric8.crd.generator.victools.approvaltests.conversion.v2;

import io.fabric8.crd.generator.victools.annotation.WebhookConversion;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("samples.fabric8.io")
@Version("v2")
@Kind("WebhookConversion")
@WebhookConversion(versions = { "v2", "v1" }, serviceName = "conversion-webhook", serviceNamespace = "my-namespace")
public class WebhookConversionExample extends CustomResource<WebhookConversionSpec, Void> {
}
