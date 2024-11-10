package io.fabric8.crd.generator.victools.model;

import lombok.Builder;

import java.util.Optional;

@Builder
public record PrinterColumnInfo(
    String name,
    String description,
    String jsonPath,
    String type,
    String format,
    Integer priority) {

  public Optional<String> findName() {
    return Optional.ofNullable(name);
  }

}
