package io.fabric8.crd.generator.victools.model;

import java.util.Optional;

public record ExternalDocsInfo(String description, String url) {

  public boolean isNotEmpty() {
    return description != null || url != null;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<String> getUrl() {
    return Optional.ofNullable(url);
  }
}
