package io.fabric8.crd.generator.victools.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExternalDocs {

  /**
   * A short description of the target documentation.
   *
   * @return the documentation description
   **/
  String description() default "";

  /**
   * The URL for the target documentation. Value must be in the format of a URL.
   *
   * @return the documentation URL
   **/
  String url() default "";

}
