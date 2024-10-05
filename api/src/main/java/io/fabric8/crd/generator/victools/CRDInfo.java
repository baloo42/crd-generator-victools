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

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Map;
import java.util.Set;

@Value
@Builder
public class CRDInfo {
  /**
   * The name of the CRD. e.g. issuers.cert-manager.io
   */
  @NonNull
  String crdName;
  /**
   * The version of the CRD spec.
   */
  @NonNull
  String crdSpecVersion;

  /**
   * The group of the resource, e.g. cert-manager.io
   */
  @NonNull
  String resourceGroup;
  /**
   * The kind of the resource, e.g. Issuer
   */
  @NonNull
  String resourceKind;
  /**
   * The resource name in singular, e.g. issuer
   */
  @NonNull
  String resourceSingular;
  /**
   * The resource name in plural, e.g. issuers
   */
  @NonNull
  String resourcePlural;
  /**
   * The resource versions, this CRD contains.
   */
  @NonNull
  Set<String> resourceVersions;
  /**
   * The file path of the CRD.
   */
  @NonNull
  String filePath;
  /**
   * The file paths for the JSON-Schemas of each version.
   */
  @NonNull
  Map<String, String> schemaFilePaths;
  /**
   * Dependent classes, used to generate this CRD.
   */
  @NonNull
  Set<String> dependentClassNames;
}
