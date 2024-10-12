package io.fabric8.crd.generator.victools.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Decorates the CRD with the "None" conversion strategy.
 * <p>
 * If this strategy is used, the built-in converter of the Kubernetes API server changes the apiVersion to the version
 * which is marked as stored and would not touch any other field in the custom resource.
 * Note that only one strategy can be used at the same time for a single CRD.
 * If the "None" strategy doesn't fit, use the {@link WebhookConversion} strategy.
 * </p>
 *
 * @see <a href=
 *      "https://kubernetes.io/docs/tasks/extend-kubernetes/custom-resources/custom-resource-definition-versioning/#specify-multiple-versions">
 *      Kubernetes Docs - CRD Versioning
 *      </a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoneConversion {
}
