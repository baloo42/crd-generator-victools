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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.crd.generator.victools.spi.CRDGeneratorContext;
import io.fabric8.kubernetes.client.utils.KubernetesSerialization;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

import static java.util.Optional.ofNullable;

/**
 * The CRD-Generator context.
 * <p>
 * This class contains settings, which are required during the schema generation phase.
 * </p>
 */
@Slf4j
@Getter
class CRDGeneratorContextImpl implements CRDGeneratorContext, CRDGeneratorContextInternal {

  private final KubernetesSerialization kubernetesSerialization;
  private final ObjectMapper objectMapper;
  private final Set<CRDGeneratorSchemaOption> options;

  @Builder
  private CRDGeneratorContextImpl(
      KubernetesSerialization kubernetesSerialization,
      ObjectMapper objectMapper,
      Set<CRDGeneratorSchemaOption> options) {

    this.objectMapper = ofNullable(objectMapper)
        .orElseGet(ObjectMapper::new);
    this.kubernetesSerialization = ofNullable(kubernetesSerialization)
        .orElseGet(() -> new KubernetesSerialization(this.objectMapper, false));

    this.options = options;
  }

  @Override
  public boolean isEnabled(CRDGeneratorSchemaOption option) {
    return options.contains(option);
  }
}
