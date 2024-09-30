package io.fabric8.crd.generator.victools.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a map or object to further describe its topology.
 * <p>
 * Emits {@code x-kubernetes-map-type}
 * </p>
 */
@Inherited
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MapType {

  /**
   * @return the map type
   */
  Type value() default Type.GRANULAR;

  enum Type {
    /**
     * These maps are actual maps (key-value pairs) and each field is independent
     * of each other (they can each be manipulated by separate actors). This is
     * the default behaviour for all maps.
     */
    GRANULAR,

    /**
     * The map is treated as a single entity, like a scalar.
     * Atomic maps will be entirely replaced when updated.
     */
    ATOMIC
  }


}
