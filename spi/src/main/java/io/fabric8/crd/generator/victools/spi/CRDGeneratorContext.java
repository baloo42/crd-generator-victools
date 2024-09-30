package io.fabric8.crd.generator.victools.spi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface CRDGeneratorContext {

  ObjectMapper getObjectMapper();

  default <T> T convertValue(Object value, Class<T> tClass) {
    return getObjectMapper().convertValue(value, tClass);
  }

  default <T> T convertValue(Object value, TypeReference<T> tTypeReference) {
    return getObjectMapper().convertValue(value, tTypeReference);
  }

  default <T> T convertValue(Object value, JavaType javaType) {
    return getObjectMapper().convertValue(value, javaType);
  }

  default JsonNode convertValueToJsonNode(Object value) {
    return convertValue(value, JsonNode.class);
  }

  default BeanDescription introspect(Class<?> clazz) {
    return introspect(getObjectMapper().getTypeFactory().constructType(clazz));
  }

  default BeanDescription introspect(JavaType javaType) {
    return getObjectMapper().getSerializationConfig().introspect(javaType);
  }
}
