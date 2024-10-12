package io.fabric8.crd.generator.victools.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Decorates the CRD with the "Webhook" conversion strategy.
 * <p>
 * If this strategy is used, the Kubernetes API server will request an external webhook to do the conversion.
 * </p>
 * <p>
 * Note that only one strategy can be used at the same time for a single CRD.
 * If the "Webhook" strategy doesn't fit and no field has changed, use the {@link NoneConversion} strategy.
 * </p>
 *
 * @see <a href=
 *      "https://kubernetes.io/docs/tasks/extend-kubernetes/custom-resources/custom-resource-definition-versioning/#webhook-conversion">
 *      Kubernetes Docs - CRD Versioning - Webhook Conversion
 *      </a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebhookConversion {
  /**
   * The name of this strategy.
   */
  String NAME = "Webhook";

  /**
   * The ConversionReviewVersions.
   * <p>
   * ConversionReviewVersions is an ordered list of preferred `ConversionReview` versions the Webhook expects.
   * The API server will use the first version in the list which it supports. If none of the versions specified
   * in this list are supported by API server, conversion will fail for the custom resource. If a persisted Webhook
   * configuration specifies allowed versions and does not include any versions known to the API Server,
   * calls to the webhook will fail.
   * </p>
   *
   * @return the conversion review versions.
   */
  String[] versions();

  /**
   * The location of the webhook, in standard URL form (`scheme://host:port/path`).
   * Exactly one of `url` or `svcNamespace` / `svcName` must be specified.
   *
   * @return the URL of the webhook.
   * @see <a href=
   *      "https://kubernetes.io/docs/tasks/extend-kubernetes/custom-resources/custom-resource-definition-versioning/#url">
   *      Kubernetes Docs - CRD Versioning - Webhook Conversion - URL
   *      </a>
   */
  String url() default "";

  /**
   * The name of the service.
   * Required if service instead of a URL is used.
   *
   * @return the service name.
   * @see <a href=
   *      "https://kubernetes.io/docs/tasks/extend-kubernetes/custom-resources/custom-resource-definition-versioning/#service-reference">
   *      Kubernetes Docs - CRD Versioning - Webhook Conversion - Service Reference
   *      </a>
   */
  String serviceName() default "";

  /**
   * The namespace of the service.
   * Required if the service instead of a URL is used.
   *
   * @return the namespace of the service.
   * @see <a href=
   *      "https://kubernetes.io/docs/tasks/extend-kubernetes/custom-resources/custom-resource-definition-versioning/#service-reference">
   *      Kubernetes Docs - CRD Versioning - Webhook Conversion - Service Reference
   *      </a>
   */
  String serviceNamespace() default "";

  /**
   * Service path is an optional URL path at which the webhook will be contacted.
   *
   * @return the path;
   */
  String servicePath() default "";

  /**
   * The service port.
   *
   * @return the service port
   */
  int servicePort() default 443;

}
