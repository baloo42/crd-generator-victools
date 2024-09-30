package io.fabric8.crd.generator.victools.example;


import io.fabric8.kubernetes.api.model.apiextensions.v1.JSONSchemaProps;
import io.fabric8.kubernetes.client.utils.KubernetesSerialization;
import org.junit.jupiter.api.Test;

public class JacksonTest {


  void test(){
    var json = """
        {
          "type": "object",
          "properties": {
            "message": {
              "type": "string"
            },
            "state": {
              "type": "string",
              "enum": [
                "CREATED",
                "ERROR",
                "ROLLING_UPDATE",
                "RUNNING",
                "SCALING",
                "STARTING"
              ],
              "additionalProperties": {
                "xyz": {
                  "name": "State",
                  "format": null,
                  "priority": 0
                }
              }
            }
          }
        }
        """;
    var schema = new KubernetesSerialization().unmarshal(json, JSONSchemaProps.class);
    System.out.println(schema);
  }
}
