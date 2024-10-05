package io.fabric8.crd.generator.victools.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines an additional printer column. Must be placed at the root of the
 * custom resource.
 *
 * @see <a href=
 *      "https://kubernetes.io/docs/tasks/extend-kubernetes/custom-resources/custom-resource-definitions/#additional-printer-columns">Kubernetes
 *      Docs - Additional Printer Columns</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AdditionalPrinterColumn.List.class)
public @interface AdditionalPrinterColumn {

  /**
   * The name of the column. An empty column name implies the use of the last path
   * element.
   *
   * @return the column name, or empty string if the last path element should be
   *         used.
   */
  String name() default "";

  /**
   * The JSON Path to the field.
   *
   * @return the JSON path
   */
  String jsonPath();

  /**
   * The type of the printer column.
   *
   * @return the type
   */
  Type type() default Type.STRING;

  /**
   * The printer column format.
   *
   * @return the format or NONE if no format is specified.
   */
  Format format() default Format.NONE;

  /**
   * The description of the printer column.
   *
   * @return the description
   */
  String description() default "";

  /**
   * The printer column priority.
   *
   * @return the priority or 0 if no priority is specified.
   */
  int priority() default 0;

  // https://kubernetes.io/docs/tasks/extend-kubernetes/custom-resources/custom-resource-definitions/#type
  enum Type {

    STRING("string"),
    INTEGER("integer"),
    NUMBER("number"),
    BOOLEAN("boolean"),
    DATE("date");

    private final String value;

    Type(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  // https://kubernetes.io/docs/tasks/extend-kubernetes/custom-resources/custom-resource-definitions/#format
  enum Format {

    NONE(""),
    INT32("int32"),
    INT64("int64"),
    FLOAT("float"),
    DOUBLE("double"),
    BYTE("byte"),
    DATE("date"),
    DATE_TIME("date-time"),
    PASSWORD("password");

    private final String value;

    Format(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @interface List {
    AdditionalPrinterColumn[] value();
  }
}
