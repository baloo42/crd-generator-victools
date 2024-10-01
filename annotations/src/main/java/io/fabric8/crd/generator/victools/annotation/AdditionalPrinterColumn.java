package io.fabric8.crd.generator.victools.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that allows additionalPrinterColumns entries to be created with arbitrary JSONPaths.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AdditionalPrinterColumn.List.class)
public @interface AdditionalPrinterColumn {

  String name();

  String jsonPath();

  Type type() default Type.STRING;

  Format format() default Format.NONE;

  String description() default "";

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
