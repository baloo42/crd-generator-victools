package io.fabric8.crd.generator.victools.annotation;

// https://kubernetes.io/docs/tasks/extend-kubernetes/custom-resources/custom-resource-definitions/#format
public enum PrinterColumnFormat {

  NONE(null),
  INT32("int32"),
  INT64("int64"),
  FLOAT("float"),
  DOUBLE("double"),
  BYTE("byte"),
  DATE("date"),
  DATE_TIME("date-time"),
  PASSWORD("password");

  private final String value;

  PrinterColumnFormat(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
