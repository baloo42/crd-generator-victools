package io.fabric8.crd.generator.victools.v1;

import io.fabric8.crd.generator.victools.CustomResourceInfo;
import io.fabric8.crd.generator.victools.annotation.NoneConversion;
import io.fabric8.crd.generator.victools.annotation.WebhookConversion;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceConversion;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceConversionBuilder;
import lombok.NonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static io.fabric8.crd.generator.victools.CRDUtils.emptyToNull;
import static java.util.Optional.ofNullable;

class ConversionCollector {

  private static final String ERROR_MSG_WEBHOOKCONVERSION_PREFIX = "Invalid WebhookConversion configuration: ";
  private static final String NONE_STRATEGY = "None";
  private static final String WEBHOOK_STRATEGY = "Webhook";

  private final CustomResourceConversion conversion;

  public ConversionCollector(@NonNull CustomResourceInfo crInfo) {
    this.conversion = findConversion(crInfo.definition())
        .orElse(null);
  }

  public Optional<CustomResourceConversion> findConversion() {
    return ofNullable(conversion);
  }

  private Optional<CustomResourceConversion> findConversion(Class<?> customResource) {
    return ofNullable(customResource.getAnnotation(NoneConversion.class))
        .map(ConversionCollector::from)
        .or(() -> ofNullable(customResource.getAnnotation(WebhookConversion.class))
            .map(ConversionCollector::from));
  }

  private static CustomResourceConversion from(NoneConversion noneConversion) {
    return new CustomResourceConversionBuilder()
        .withStrategy(NONE_STRATEGY)
        .build();
  }

  public static CustomResourceConversion from(WebhookConversion webhookConversion) {
    final List<String> versions = List.of(webhookConversion.versions());
    final String url = emptyToNull(webhookConversion.url());
    final String serviceName = emptyToNull(webhookConversion.serviceName());
    final String serviceNamespace = emptyToNull(webhookConversion.serviceNamespace());
    final String servicePath = emptyToNull(webhookConversion.servicePath());
    final Integer servicePort = webhookConversion.servicePort() != 443 ? webhookConversion.servicePort() : null;

    assertUniqueConversionReviewVersions(versions);
    assertUrlOrService(url, serviceName, serviceNamespace, servicePath, servicePort);
    assertValidServicePort(servicePort);
    assertValidUrl(url);

    return new CustomResourceConversionBuilder()
        .withStrategy(WEBHOOK_STRATEGY)
        .withNewWebhook()
        .addAllToConversionReviewVersions(versions)
        .withNewClientConfig()
        .withUrl(url)
        .withNewService(serviceName, serviceNamespace, servicePath, servicePort)
        .endClientConfig()
        .endWebhook()
        .build();
  }

  private static void assertUniqueConversionReviewVersions(List<String> versions) {
    if (versions.stream().distinct().count() != versions.size()) {
      throw new IllegalArgumentException(
          String.format("ConversionReviewVersions values must be distinct: %s", versions));
    }
  }

  private static void assertUrlOrService(
      String url,
      String serviceName,
      String serviceNamespace,
      String servicePath,
      Integer servicePort) {

    if (url != null) {
      // url
      if (serviceName != null || serviceNamespace != null || servicePath != null || servicePort != null) {
        throw new IllegalArgumentException(String.format(
            ERROR_MSG_WEBHOOKCONVERSION_PREFIX
                + "Exactly one of URL or serviceNamespace/serviceName must be specified. "
                + "serviceNamespace: %s, serviceName: %s, servicePath: %s, servicePort: %s, URL: %s",
            serviceNamespace, serviceName, servicePath, servicePort, url));
      }
    } else if (serviceName == null || serviceNamespace == null) {
      // service
      throw new IllegalArgumentException(String.format(
          ERROR_MSG_WEBHOOKCONVERSION_PREFIX
              + "Exactly one of URL or serviceNamespace/serviceName must be specified. "
              + "serviceNamespace: %s, serviceName: %s, servicePath: %s, servicePort: %s, URL: null",
          serviceNamespace, serviceName, servicePath, servicePort));
    }
  }

  private static void assertValidServicePort(Integer servicePort) {
    if (servicePort != null) {
      if (servicePort < 1 || servicePort > 65535) {
        throw new IllegalArgumentException(ERROR_MSG_WEBHOOKCONVERSION_PREFIX
            + "Service port must be a valid port number (1-65535, inclusive). ServicePort: " + servicePort);
      }
    }
  }

  private static void assertValidUrl(String urlString) {
    if (urlString != null) {
      try {
        final URL url = new URL(urlString);
        if (!"https".equals(url.getProtocol())) {
          throw new IllegalArgumentException(
              String.format(ERROR_MSG_WEBHOOKCONVERSION_PREFIX
                  + "URL schema of %s is invalid. "
                  + "Only https:// is allowed.", urlString));
        }
        if (url.getQuery() != null) {
          throw new IllegalArgumentException(
              String.format(ERROR_MSG_WEBHOOKCONVERSION_PREFIX + "URL %s contains query parameters "
                  + "which are not allowed.", urlString));
        }
        if (url.getRef() != null) {
          throw new IllegalArgumentException(
              String.format(ERROR_MSG_WEBHOOKCONVERSION_PREFIX + "URL %s contains fragment(s) "
                  + "which is not allowed.", urlString));
        }
      } catch (MalformedURLException e) {
        throw new IllegalArgumentException(
            String.format(ERROR_MSG_WEBHOOKCONVERSION_PREFIX + "Malformed URL: %s", e.getMessage()));
      }
    }
  }
}
