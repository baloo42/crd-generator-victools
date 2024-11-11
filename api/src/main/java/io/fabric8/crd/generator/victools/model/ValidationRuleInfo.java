package io.fabric8.crd.generator.victools.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ValidationRuleInfo {
  String fieldPath;
  String message;
  String messageExpression;
  Boolean optionalOldSelf;
  String reason;
  String rule;
}
