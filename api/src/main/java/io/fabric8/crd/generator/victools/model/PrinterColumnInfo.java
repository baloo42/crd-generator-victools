package io.fabric8.crd.generator.victools.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record PrinterColumnInfo(
    String name,
    String description,
    String jsonPath,
    String type,
    String format,
    Integer priority) {
}
