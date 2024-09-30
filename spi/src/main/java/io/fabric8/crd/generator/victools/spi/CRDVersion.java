package io.fabric8.crd.generator.victools.spi;

import java.util.Optional;

public enum CRDVersion {

  V1("v1", "apiextensions.k8s.io");

  private final String versionName;
  private final String apiGroup;

  CRDVersion(String versionName, String apiGroup) {
    this.versionName = versionName;
    this.apiGroup = apiGroup;
  }

  public String getVersionName() {
    return versionName;
  }

  public String getApiGroup() {
    return apiGroup;
  }

  public String getApiVersion() {
    return getApiGroup() + "/" + getVersionName();
  }

  public static Optional<CRDVersion> findVersion(String value) {
    if (value.equalsIgnoreCase(V1.getVersionName())) {
      return Optional.of(V1);
    }
    return Optional.empty();
  }
}
