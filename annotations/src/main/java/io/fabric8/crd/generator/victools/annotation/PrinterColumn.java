package io.fabric8.crd.generator.victools.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PrinterColumn {

  /**
   * The name of the column.
   *
   * @return the column name.
   */
  String name() default "";

  /**
   * The printer column format.
   *
   * @return the format
   */
  PrinterColumnFormat format() default PrinterColumnFormat.NONE;

  /**
   * The printer column priority.
   *
   * @return the priority.
   */
  int priority() default 0;
}
