package io.fabric8.crd.generator.victools;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import lombok.Getter;

@Getter
class SpecAndStatus {

  private final String specClassName;
  private final String statusClassName;
  private final boolean unreliable;

  private SpecAndStatus(String specClassName, String statusClassName) {
    this.specClassName = specClassName;
    this.statusClassName = statusClassName;
    this.unreliable = specClassName == null || statusClassName == null;
  }

  /**
   * Determine the spec and status types via convention by looking for the
   * spec and status properties.
   * <br>
   * If we support eventually support spec and status interfaces or some other mechanism
   * then this logic will need to change
   */
  public static SpecAndStatus resolveSpecAndStatusTypes(Class<?> definition) {
    SerializationConfig config = new ObjectMapper().getSerializationConfig();
    BeanDescription description = config.introspect(config.constructType(definition));
    String specClassName = null;
    String statusClassName = null;
    for (BeanPropertyDefinition bpd : description.findProperties()) {
      if (bpd.getName().equals("spec") && bpd.getRawPrimaryType() != Void.class) {
        specClassName = bpd.getRawPrimaryType().getName();
      } else if (bpd.getName().equals("status") && bpd.getRawPrimaryType() != Void.class) {
        statusClassName = bpd.getRawPrimaryType().getName();
      }
    }
    return new SpecAndStatus(specClassName, statusClassName);
  }
}
