package io.fabric8.crd.generator.victools.spi;

public enum KubernetesSchemaKeyword {

  NULLABLE("nullable"),
  EXTERNAL_DOCS("externalDocs"),
  KUBERNETES_VALIDATIONS("x-kubernetes-validations"),
  KUBERNETES_EMBEDDED_RESOURCE("x-kubernetes-embedded-resource"),
  KUBERNETES_PRESERVE_UNKNOWN_FIELDS("x-kubernetes-preserve-unknown-fields"),
  KUBERNETES_INT_OR_STRING("x-kubernetes-int-or-string"),
  KUBERNETES_LIST_TYPE("x-kubernetes-list-type"),
  KUBERNETES_MAP_TYPE("x-kubernetes-map-type");

  private final String value;

  KubernetesSchemaKeyword(String value){
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
