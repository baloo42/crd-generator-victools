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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.crd.generator.victools.spi.CRDVersion;
import io.fabric8.crd.generator.victools.v1.CRDv1Handler;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.utils.ApiVersionUtil;
import io.fabric8.kubernetes.client.utils.KubernetesSerialization;
import io.fabric8.kubernetes.client.utils.Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@Slf4j
public class CRDGenerator implements CRDGeneratorApi {

  public static final String DEFAULT_HEADER = "Generated by Fabric8 CRDGenerator, manual edits might get overwritten!";
  public static final String DEFAULT_FILENAME_FORMAT = "{{crdName}}-{{crdSpecVersion}}.yml";
  public static final String DEFAULT_SCHEMA_FILENAME_FORMAT = "{{resourceGroup}}/{{resourceKind}}_{{resourceAPIVersion}}.json";

  private final Set<CRDGeneratorSchemaOption> enabledOptions = new HashSet<>();
  private final Set<CRDVersion> targetCRDVersions = new HashSet<>();
  private CRDOutput<? extends OutputStream> crdOutput;
  private CRDOutput<? extends OutputStream> crdSchemaOutput;
  private boolean parallel;
  private String header = DEFAULT_HEADER;
  private String filenameFormat = DEFAULT_FILENAME_FORMAT;
  private String schemaFilenameFormat = DEFAULT_SCHEMA_FILENAME_FORMAT;
  private final Map<String, String> annotations = new HashMap<>();
  private final Map<String, String> labels = new HashMap<>();
  private ObjectMapper objectMapper;
  private KubernetesSerialization kubernetesSerialization;
  private Map<String, CustomResourceInfo> infos;

  @Override
  public CRDGenerator withOutputDirectory(File outputDir) {
    withCrdOutputDirectory(outputDir);
    withCrdSchemaOutputDirectory(outputDir);
    return this;
  }

  public CRDGenerator withCrdOutputDirectory(File outputDir) {
    crdOutput = new CRDOutput.DirCRDOutput(outputDir);
    return this;
  }

  public CRDGenerator withCrdSchemaOutputDirectory(File outputDir) {
    crdSchemaOutput = new CRDOutput.DirCRDOutput(outputDir);
    return this;
  }

  @Override
  public CRDGenerator withOutput(CRDOutput<? extends OutputStream> output) {
    withCrdOutput(output);
    withCrdSchemaOutput(output);
    return this;
  }

  public CRDGenerator withCrdOutput(CRDOutput<? extends OutputStream> output) {
    this.crdOutput = output;
    return this;
  }

  public CRDGenerator withCrdSchemaOutput(CRDOutput<? extends OutputStream> output) {
    this.crdSchemaOutput = output;
    return this;
  }

  @Override
  public CRDGenerator withFilenameFormat(String filenameFormat) {
    Objects.requireNonNull(filenameFormat);
    this.filenameFormat = filenameFormat;
    return this;
  }

  public CRDGenerator withSchemaFilenameFormat(String filenameFormat) {
    Objects.requireNonNull(filenameFormat);
    this.schemaFilenameFormat = filenameFormat;
    return this;
  }

  public CRDGenerator withOption(CRDGeneratorSchemaOption option) {
    enabledOptions.add(option);
    return this;
  }

  public CRDGenerator withoutOption(CRDGeneratorSchemaOption option) {
    enabledOptions.remove(option);
    return this;
  }

  @Override
  public CRDGenerator withImplicitPreserveUnknownFields(boolean implicitPreserveUnknownFields) {
    if (implicitPreserveUnknownFields) {
      withOption(CRDGeneratorSchemaOption.IMPLICIT_PRESERVE_UNKNOWN_FIELDS);
    } else {
      enabledOptions.remove(CRDGeneratorSchemaOption.IMPLICIT_PRESERVE_UNKNOWN_FIELDS);
    }
    return this;
  }

  @Override
  public CRDGenerator withParallelGenerationEnabled(boolean parallel) {
    this.parallel = parallel;
    return this;
  }

  @Override
  public CRDGenerator withHeader(String header) {
    this.header = header;
    return this;
  }

  @Override
  public CRDGenerator withAnnotations(Map<String, String> annotations) {
    this.annotations.putAll(annotations);
    return this;
  }

  @Override
  public CRDGenerator withLabels(Map<String, String> labels) {
    this.labels.putAll(labels);
    return this;
  }

  @Override
  public CRDGenerator withObjectMapper(ObjectMapper mapper,
      KubernetesSerialization kubernetesSerialization) {
    this.objectMapper = mapper;
    this.kubernetesSerialization = kubernetesSerialization;
    return this;
  }

  @Override
  public CRDGenerator forCRDVersions(List<String> versions) {
    return versions != null && !versions.isEmpty() ? forCRDVersions(versions.toArray(new String[0]))
        : this;
  }

  @Override
  public CRDGenerator forCRDVersions(String... versions) {
    if (versions != null) {
      Stream.of(versions)
          .flatMap(s -> CRDVersion.findVersion(s).stream())
          .forEach(this::forCRDVersions);
    }
    return this;
  }

  @Override
  public CRDGenerator forCRDVersions(CRDVersion... versions) {
    if (versions != null) {
      for (CRDVersion version : versions) {
        if (version != null) {
          targetCRDVersions.add(version);
        }
      }
    }
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public final CRDGenerator customResourceClasses(Class<? extends HasMetadata>... crClasses) {
    if (crClasses == null) {
      return this;
    }
    return customResourceClasses(Arrays.asList(crClasses));
  }

  @Override
  public CRDGenerator customResourceClasses(Collection<Class<? extends HasMetadata>> crClasses) {
    if (crClasses == null) {
      return this;
    }
    return customResources(crClasses.stream()
        .filter(Objects::nonNull)
        .map(CustomResourceInfo::fromClass)
        .toArray(CustomResourceInfo[]::new));
  }

  @Override
  public CRDGenerator customResources(Collection<CustomResourceInfo> infos) {
    if (infos == null) {
      return this;
    }
    return customResources(infos.toArray(new CustomResourceInfo[0]));
  }

  @Override
  public CRDGenerator customResources(CustomResourceInfo... infos) {
    if (infos != null && infos.length > 0) {
      if (this.infos == null) {
        this.infos = new HashMap<>(infos.length);
      }
      Arrays.stream(infos)
          .filter(Objects::nonNull)
          // make sure we record all versions of the CR
          .forEach(info -> this.infos.put(info.id(), info));
    }
    return this;
  }

  Set<CustomResourceInfo> getCustomResourceInfos() {
    return this.infos == null ? Collections.emptySet() : new HashSet<>(infos.values());
  }

  private CRDGeneratorContextInternal createContext() {
    return CRDGeneratorContextImpl.builder()
        .objectMapper(objectMapper)
        .kubernetesSerialization(kubernetesSerialization)
        .options(enabledOptions)
        .build();
  }

  private Map<CRDVersion, AbstractCRDVersionHandler> createHandlers(
      CRDGeneratorContextInternal context) {
    return targetCRDVersions.stream()
        .collect(Collectors.toMap(v -> v, v -> switch (v) {
          case V1 -> new CRDv1Handler(context);
        }));
  }

  @Override
  public int generate() {
    return detailedGenerate().numberOfGeneratedCRDs();
  }

  @Override
  public CRDGenerationInfo detailedGenerate() {
    if (getCustomResourceInfos().isEmpty()) {
      log.warn("No resources were registered with the 'customResources' method to be generated");
      return CRDGenerationInfo.EMPTY;
    }

    if (crdOutput == null) {
      log.warn("No output option was selected. Skipping generation.");
      return CRDGenerationInfo.EMPTY;
    }

    // if no CRD version is specified, generate for all supported versions
    if (targetCRDVersions.isEmpty()) {
      forCRDVersions(CRDVersion.V1);
    }

    var context = createContext();
    Map<CRDVersion, AbstractCRDVersionHandler> handlers = createHandlers(context);

    for (CustomResourceInfo info : infos.values()) {
      if (info != null) {
        if (log.isInfoEnabled()) {
          log.info("Generating '{}' version '{}' with {} (spec: {} / status {})...",
              info.crdName(), info.version(), info.crClassName(),
              info.specClassName().orElse("undetermined"),
              info.statusClassName().orElse("undetermined"));
        }

        if (parallel) {
          handlers.values().stream()
              .map(h -> ForkJoinPool.commonPool()
                  .submit(() -> h.handle(info)))
              .forEach(ForkJoinTask::join);
        } else {
          handlers.values()
              .forEach(h -> h.handle(info));
        }
      }
    }

    var crdResults = handlers.values().stream()
        .flatMap(handler -> handler.finish(context))
        .collect(Collectors.toSet());

    return finalize(crdResults, context);
  }

  private CRDGenerationInfo finalize(Set<CRDResult> crdResults, CRDGeneratorContextInternal context) {
    var results = new HashSet<CRDInfo>();
    for (var crdResult : crdResults) {
      addMetadata(crdResult.crd());
      var crdInfo = emitCrdContent(crdResult, context);
      results.add(crdInfo);
    }

    return new CRDGenerationInfo(results);
  }

  private void addMetadata(HasMetadata crd) {
    var meta = crd.getMetadata().edit()
        .addToLabels(labels)
        .addToAnnotations(annotations)
        .build();
    crd.setMetadata(meta);
  }

  private CRDInfo emitCrdContent(
      CRDResult crdResult,
      CRDGeneratorContextInternal context) {

    final String crdVersion = ApiVersionUtil.trimVersion(crdResult.crd().getApiVersion());
    final String crdName = crdResult.crd().getMetadata().getName();

    final var crdOutContext = CRDOutput.contextBuilder()
        .filenameFormat(filenameFormat)
        .crdSpecVersion(crdVersion)
        .crdName(crdName)
        .resourceGroup(crdResult.resourceGroup())
        .resourceKind(crdResult.resourceKind())
        .resourceSingular(crdResult.resourceSingular())
        .resourcePlural(crdResult.resourcePlural())
        .build();

    var crdFile = emitCrd(crdResult.crd(), crdOutContext, context);

    var crdSchemaFiles = ofNullable(crdSchemaOutput)
        .map(o -> emitCrdSchemas(crdResult, crdOutContext, context))
        .orElseGet(Collections::emptyMap);

    if (crdSchemaFiles.isEmpty()) {
      log.debug("No JSON-Schemas emitted: No output has been defined");
    }

    return CRDInfo.builder()
        .crdName(crdName)
        .crdSpecVersion(crdVersion)
        .resourceGroup(crdResult.resourceGroup())
        .resourceKind(crdResult.resourceKind())
        .resourceSingular(crdResult.resourceSingular())
        .resourcePlural(crdResult.resourcePlural())
        .filePath(crdFile)
        .schemaFilePaths(crdSchemaFiles)
        .dependentClassNames(crdResult.dependentClasses())
        .resourceVersions(crdResult.resourceVersions())
        .build();
  }

  private String emitCrd(
      HasMetadata crd,
      CRDOutput.CRDOutputContextImpl crdOutContext,
      CRDGeneratorContextInternal context) {

    try {
      try (final OutputStreamWriter writer = new OutputStreamWriter(crdOutput.getOutputStream(crdOutContext),
          StandardCharsets.UTF_8)) {
        if (Utils.isNotNullOrEmpty(header)) {
          for (String headerLine : header.split("\n")) {
            writer.write("# " + headerLine + "\n");
          }
        }
        String yaml = context.getKubernetesSerialization().asYaml(crd);
        // strip the explicit start added by default
        writer.write(yaml.substring(4));
        return new File(crdOutput.getURI(crdOutContext)).getAbsolutePath();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Map<String, String> emitCrdSchemas(
      CRDResult crdResult,
      CRDOutput.CRDOutputContextImpl crdOutContext,
      CRDGeneratorContextInternal context) {

    var result = new HashMap<String, String>();

    var crdSchemaOutContextBuilder = CRDOutput.schemaContextBuilder(crdOutContext)
        .filenameFormat(schemaFilenameFormat);

    for (var schemaEntry : crdResult.schemas().entrySet()) {
      var crdSchemaOutContext = crdSchemaOutContextBuilder
          .resourceAPIVersion(schemaEntry.getKey())
          .build();

      result.put(schemaEntry.getKey(),
          emitCrdSchema(schemaEntry.getValue(), crdSchemaOutContext, context));
    }

    return result;
  }

  private String emitCrdSchema(
      JsonNode schema,
      CRDOutput.CRDSchemaOutputContextImpl crdOutContext,
      CRDGeneratorContextInternal context) {

    try {
      try (final OutputStreamWriter writer = new OutputStreamWriter(crdSchemaOutput.getOutputStream(crdOutContext),
          StandardCharsets.UTF_8)) {
        writer.write(context.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(schema));
        return new File(crdSchemaOutput.getURI(crdOutContext)).getAbsolutePath();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
