package io.fabric8.crd.generator.victools.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a list or array to further describe its topology.
 * <p>
 * Emits {@code x-kubernetes-list-type}
 * </p>
 */
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ListType {

  /**
   * @return the list type
   */
  Type value() default Type.ATOMIC;

  /**
   * @return the map keys. Only used if {@link Type} is {@link Type#MAP}.
   */
  String[] mapKeys() default {};

  /**
   * The Kubernetes List Type
   */
  enum Type {
    /**
     * The list is treated as a single entity, like a scalar.
     * Atomic lists will be entirely replaced when updated.
     */
    ATOMIC,

    /**
     * Sets are lists that must not have multiple items with the same value. Each
     * value must be a scalar, an object with x-kubernetes-map-type <code>atomic</code>
     * ({@link MapType.Type#ATOMIC}) or an array with x-kubernetes-list-type
     * <code>atomic</code> ({@link ListType.Type#ATOMIC}).
     */
    SET,

    /**
     * These lists are like maps in that their elements have a non-index key
     * used to identify them. Order is preserved upon merge. The map tag
     * must only be used on a list with elements of type object.
     *
     * @see ListMapKey
     */
    MAP
  }
}
