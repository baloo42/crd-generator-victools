package io.fabric8.crd.generator.victools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CRDOutputTest {

  @Test
  @DisplayName("interpolateString, String with no placeholders and empty parameters, should return input")
  void interpolateStringTest() {
    // Given
    final String input = "I don't have placeholders";
    // When
    final String result = CRDOutput.interpolateString(input, Collections.emptyMap());
    // Then
    assertEquals("I don't have placeholders", result);
  }

  @Test
  @DisplayName("interpolateString, String with no placeholders and null parameters, should return input")
  void interpolateStringNullParametersTest() {
    // Given
    final String input = "I don't have placeholders";
    // When
    final String result = CRDOutput.interpolateString(input, null);
    // Then
    assertEquals("I don't have placeholders", result);
  }

  @Test
  @DisplayName("interpolateString, String with no placeholders and null parameter values, should return input")
  void interpolateStringNullParameterValuesTest() {
    // Given
    final String input = "I don't have placeholders";
    // When
    final String result = CRDOutput.interpolateString(input, Collections.singletonMap("KEY", null));
    // Then
    assertEquals("I don't have placeholders", result);
  }

  @Test
  @DisplayName("interpolateString, String with mixed placeholders and parameters, should return interpolated input")
  void interpolateStringWithParametersTest() {
    // Given
    final String input = "This is a \"{{SINGLE_CURLY_BRACE}}\" and the following is code ${NOT_REPLACED}: \"{{RENDER}}\"";
    final Map<String, String> parameters = new HashMap<>();
    parameters.put("SINGLE_CURLY_BRACE", "template string");
    parameters.put("RENDER", "'1' === '1';");
    parameters.put("NOT_THERE", "/* END */");
    parameters.put(null, "NULL key is ignored");
    parameters.put("NULL_VALUE", null);
    // When
    final String result = CRDOutput.interpolateString(input, parameters);
    // Then
    assertEquals("This is a \"template string\" and the following is code ${NOT_REPLACED}: \"'1' === '1';\"", result);
  }
}
