/*
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.crd.generator.victools;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext;
import io.fabric8.kubernetes.client.utils.Utils;
import io.fabric8.kubernetes.model.Scope;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Getter
@Accessors(fluent = true)
public class CustomResourceInfo {

  private final String group;
  private final String version;
  private final String kind;
  private final String singular;
  private final String plural;
  private final String[] shortNames;
  private final boolean storage;
  private final boolean served;
  private final boolean deprecated;
  private final String deprecationWarning;
  private final Scope scope;
  private final Class<?> definition;
  private final String crClassName;
  private final String specClassName;
  private final String statusClassName;
  private final String id;
  private final int hash;

  private final Map<String, String> annotations;
  private final Map<String, String> labels;

  @Builder
  private CustomResourceInfo(
      @NonNull String group,
      @NonNull String version,
      @NonNull String kind,
      @NonNull String singular,
      String plural,
      @NonNull String[] shortNames,
      boolean storage,
      boolean served,
      boolean deprecated,
      String deprecationWarning,
      @NonNull Scope scope,
      @NonNull Class<?> definition,
      @NonNull String crClassName,
      String specClassName,
      String statusClassName,
      @NonNull Map<String, String> annotations,
      @NonNull Map<String, String> labels) {

    this.group = group;
    this.version = version;
    this.kind = kind;
    this.singular = singular;
    this.plural = plural;
    this.shortNames = shortNames;
    this.storage = storage;
    this.served = served;
    this.deprecated = deprecated;
    this.deprecationWarning = deprecationWarning;
    this.scope = scope;
    this.definition = definition;
    this.crClassName = crClassName;
    this.specClassName = specClassName;
    this.statusClassName = statusClassName;
    this.id = crdName() + "/" + version;
    this.hash = id.hashCode();
    this.annotations = annotations;
    this.labels = labels;
  }

  public String key() {
    return crdName();
  }

  public String crdName() {
    return plural() + "." + group;
  }

  public Optional<String> specClassName() {
    return Optional.ofNullable(specClassName);
  }

  public Optional<String> statusClassName() {
    return Optional.ofNullable(statusClassName);
  }

  public static CustomResourceInfo fromClass(Class<? extends HasMetadata> customResource) {
    try {
      final HasMetadata instance = customResource.getDeclaredConstructor().newInstance();

      final String[] shortNames = CustomResource.getShortNames(customResource);

      final Scope scope =
          Utils.isResourceNamespaced(customResource) ? Scope.NAMESPACED : Scope.CLUSTER;

      SpecAndStatus specAndStatus = SpecAndStatus.resolveSpecAndStatusTypes(customResource);
      if (specAndStatus.isUnreliable()) {
        log.warn(
            "Cannot reliably determine status types for {} because it isn't parameterized with only spec and status types. Status replicas detection will be deactivated.",
            customResource.getCanonicalName());
      }

      ResourceDefinitionContext rdc = ResourceDefinitionContext.fromResourceType(customResource);
      String singular = HasMetadata.getSingular(customResource);
      boolean deprecated = CustomResource.getDeprecated(customResource);
      String deprecationWarning = CustomResource.getDeprecationWarning(customResource);
      boolean storage = CustomResource.getStorage(customResource);
      boolean served = CustomResource.getServed(customResource);

      // instance level methods - TODO: deprecate?
      if (instance instanceof CustomResource<?, ?> cr) {
        singular = cr.getSingular();
        deprecated = cr.isDeprecated();
        deprecationWarning = cr.getDeprecationWarning();
        storage = cr.isStorage();
        served = cr.isServed();
      }

      return CustomResourceInfo.builder()
          .group(rdc.getGroup())
          .version(rdc.getVersion())
          .kind(rdc.getKind())
          .singular(singular)
          .plural(rdc.getPlural())
          .shortNames(shortNames)
          .storage(storage)
          .served(served)
          .deprecated(deprecated)
          .deprecationWarning(deprecationWarning)
          .scope(scope)
          .definition(customResource)
          .crClassName(customResource.getCanonicalName())
          .specClassName(specAndStatus.getSpecClassName())
          .statusClassName(specAndStatus.getStatusClassName())
          .annotations(instance.getMetadata().getAnnotations())
          .labels(instance.getMetadata().getLabels())
          .build();
    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
        InvocationTargetException e) {
      throw KubernetesClientException.launderThrowable(e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CustomResourceInfo that = (CustomResourceInfo) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return hash;
  }
}
