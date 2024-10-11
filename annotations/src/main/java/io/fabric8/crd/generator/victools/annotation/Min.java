package io.fabric8.crd.generator.victools.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Java representation of the {@code minimum} field of JSONSchemaProps.
 *
 * @see <a href=
 *      "https://kubernetes.io/docs/reference/kubernetes-api/extend-resources/custom-resource-definition-v1/#JSONSchemaProps">
 *      Kubernetes Docs - API Reference - CRD v1 - JSONSchemaProps
 *      </a>
 *
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Min {
  /**
   * @return the element must be higher or equal to
   */
  double value();

  /**
   * Specifies whether the specified minimum is inclusive or exclusive.
   * By default, it is inclusive.
   *
   * @return {@code true} if the value must be higher or equal to the specified minimum,
   *         {@code false} if the value must be higher
   */
  boolean inclusive() default true;
}
