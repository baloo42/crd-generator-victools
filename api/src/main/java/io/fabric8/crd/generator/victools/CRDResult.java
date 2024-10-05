package io.fabric8.crd.generator.victools;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.kubernetes.api.model.HasMetadata;
import lombok.Builder;
import lombok.NonNull;

import java.util.Map;
import java.util.Set;

@Builder
public record CRDResult(
    @NonNull HasMetadata crd,
    @NonNull String resourceGroup,
    @NonNull String resourceKind,
    @NonNull String resourceSingular,
    @NonNull String resourcePlural,
    @NonNull Set<String> resourceVersions,
    @NonNull Map<String, JsonNode> schemas,
    @NonNull Set<String> dependentClasses) {
}
