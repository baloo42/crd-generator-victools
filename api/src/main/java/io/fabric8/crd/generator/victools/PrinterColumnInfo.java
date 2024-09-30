package io.fabric8.crd.generator.victools;

import java.util.Optional;

public record PrinterColumnInfo(String name, String format, int priority) {

  public Optional<String> findName() {
    return Optional.ofNullable(name);
  }

}
