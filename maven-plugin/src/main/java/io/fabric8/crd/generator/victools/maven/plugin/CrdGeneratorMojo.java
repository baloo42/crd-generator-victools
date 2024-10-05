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
package io.fabric8.crd.generator.victools.maven.plugin;

import io.fabric8.crd.generator.collector.CustomResourceCollector;
import io.fabric8.crd.generator.victools.CRDGenerationInfo;
import io.fabric8.crd.generator.victools.CRDGenerator;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

// spotless:off
@Mojo(
  name = "generate",
  defaultPhase = LifecyclePhase.PROCESS_CLASSES,
  requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
  requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
  threadSafe = true
)
// spotless:on
public class CrdGeneratorMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  MavenProject mavenProject;

  /**
   * The input directory to be used to scan for Custom Resource classes
   */
  @Parameter(property = "fabric8.crd-generator.classesToScan", defaultValue = "${project.build.outputDirectory}", readonly = true)
  File classesToScan;

  /**
   * Custom Resource classes, which should be considered to generate the CRDs.
   * If set, scanning is disabled.
   */
  @Parameter(property = "fabric8.crd-generator.customResourceClasses")
  List<String> customResourceClasses = new LinkedList<>();

  /**
   * Dependencies which should be scanned for Custom Resources.
   */
  @Parameter(property = "fabric8.crd-generator.dependenciesToScan")
  List<Dependency> dependenciesToScan = new LinkedList<>();

  /**
   * Inclusions, used to filter Custom Resource classes after scanning.
   */
  @Parameter(property = "fabric8.crd-generator.inclusions")
  FilterSet inclusions = new FilterSet();

  /**
   * Exclusions, used to filter Custom Resource classes after scanning.
   */
  @Parameter(property = "fabric8.crd-generator.exclusions")
  FilterSet exclusions = new FilterSet();

  /**
   * The classpath which should be used during the CRD generation.
   * <br>
   * Choice of:
   * <ul>
   * <li>{@code PROJECT_ONLY}: Only classes in the project.</li>
   * <li>{@code WITH_RUNTIME_DEPENDENCIES}: Classes from the project and any runtime dependencies.</li>
   * <li>{@code WITH_COMPILE_DEPENDENCIES}: Classes from the project and any compile time dependencies.</li>
   * <li>{@code WITH_ALL_DEPENDENCIES}: Classes from the project, compile time and runtime dependencies.</li>
   * <li>{@code WITH_ALL_DEPENDENCIES_AND_TESTS}: Classes from the project (including tests), compile time, runtime and test
   * dependencies.</li>
   * </ul>
   */
  @Parameter(property = "fabric8.crd-generator.classpath", defaultValue = "WITH_RUNTIME_DEPENDENCIES")
  ClasspathType classpath;

  /**
   * The output directory where the CRDs are emitted.
   */
  @Parameter(property = "fabric8.crd-generator.outputDirectory", defaultValue = "${project.build.outputDirectory}/META-INF/fabric8/")
  File outputDirectory;

  /**
   * If {@code true}, a Jandex index will be created even if the directory or JAR file contains an existing index.
   */
  @Parameter(property = "fabric8.crd-generator.forceIndex", defaultValue = "false")
  boolean forceIndex;

  /**
   * If {@code true}, directories and JAR files are scanned even if Custom Resource classes are given.
   */
  @Parameter(property = "fabric8.crd-generator.forceScan", defaultValue = "false")
  boolean forceScan;

  /**
   * If {@code true}, the CRDs will be generated in parallel.
   */
  @Parameter(property = "fabric8.crd-generator.parallel", defaultValue = "true")
  boolean parallel;

  /**
   * If {@code true}, {@code x-kubernetes-preserve-unknown-fields: true} will be added on objects
   * which contain an any-setter or any-getter.
   */
  @Parameter(property = "fabric8.crd-generator.implicitPreserveUnknownFields", defaultValue = "false")
  boolean implicitPreserveUnknownFields;

  /**
   * The format to create the filename for the CRD files.
   */
  @Parameter(property = "fabric8.crd-generator.filenameFormat", defaultValue = CRDGenerator.DEFAULT_FILENAME_FORMAT)
  String filenameFormat;

  /**
   * Enables emitting JSON-Schemas in addition to the CRDs.
   */
  @Parameter(property = "fabric8.crd-generator.schema", defaultValue = "false")
  boolean schemaEnabled;

  /**
   * The format to create the filename for the JSON-Schema files.
   */
  @Parameter(property = "fabric8.crd-generator.schemaFilenameFormat", defaultValue = CRDGenerator.DEFAULT_SCHEMA_FILENAME_FORMAT)
  String schemaFilenameFormat;

  /**
   * Additional labels for all CRDs. Useful to add e.g. vcs or build details.
   */
  @Parameter
  Map<String, String> labels = new HashMap<>();

  /**
   * Additional annotations for all CRDs. Useful to add e.g. vcs or build details.
   */
  @Parameter
  Map<String, String> annotations = new HashMap<>();

  /**
   * The header content.
   * If not empty, this text will be added as comment on top of each CRD file.
   * Multiline texts are supported and will be handled accordingly.
   */
  @Parameter(property = "fabric8.crd-generator.header", defaultValue = CRDGenerator.DEFAULT_HEADER)
  String header;

  /**
   * If {@code true}, execution will be skipped.
   */
  @Parameter(property = "fabric8.crd-generator.skip", defaultValue = "false")
  boolean skip;

  private final CustomResourceCollector customResourceCollector;
  private final CRDGenerator crdGenerator;

  public CrdGeneratorMojo() {
    this(null, null);
  }

  CrdGeneratorMojo(CustomResourceCollector customResourceCollector, CRDGenerator crdGenerator) {
    this.customResourceCollector = ofNullable(customResourceCollector)
        .orElseGet(CustomResourceCollector::new);
    this.crdGenerator = ofNullable(crdGenerator)
        .orElseGet(CRDGenerator::new);
  }

  @Override
  public void execute() throws MojoExecutionException {
    if (skip) {
      getLog().info("CRD-Generator execution skipped");
      return;
    }

    List<File> filesToScan = new LinkedList<>();
    if (classesToScan.exists()) {
      filesToScan.add(classesToScan);
    }
    filesToScan.addAll(getDependencyArchives());

    customResourceCollector
        .withParentClassLoader(Thread.currentThread().getContextClassLoader())
        .withClasspathElements(classpath.getClasspathElements(mavenProject))
        .withFilesToScan(filesToScan)
        .withForceIndex(forceIndex)
        .withForceScan(forceScan)
        .withIncludePackages(inclusions.getPackages())
        .withExcludePackages(exclusions.getPackages())
        .withCustomResourceClasses(customResourceClasses);

    List<Class<? extends HasMetadata>> customResourceClassesLoaded = customResourceCollector.findCustomResourceClasses();

    try {
      Files.createDirectories(outputDirectory.toPath());
    } catch (IOException e) {
      throw new MojoExecutionException("Could not create output directory: " + e.getMessage());
    }

    crdGenerator
        .customResourceClasses(customResourceClassesLoaded)
        .withParallelGenerationEnabled(parallel)
        .withImplicitPreserveUnknownFields(implicitPreserveUnknownFields)
        .withFilenameFormat(filenameFormat)
        .withSchemaFilenameFormat(schemaFilenameFormat)
        .withLabels(labels)
        .withAnnotations(annotations)
        .withHeader(header);

    if (schemaEnabled) {
      crdGenerator.withOutputDirectory(outputDirectory);
    } else {
      crdGenerator.withCrdOutputDirectory(outputDirectory);
    }

    CRDGenerationInfo crdGenerationInfo = crdGenerator.detailedGenerate();
    crdGenerationInfo.getCRDDetailsPerNameAndVersion().forEach((crdName, versionToInfo) -> {
      getLog().info("Generated CRD " + crdName + ":");
      versionToInfo.forEach(
          (version, info) -> getLog().info(" " + version + " -> " + info.getFilePath()));
    });
  }

  /**
   * Returns a list of archive files derived from the given dependencies.
   *
   * @return the archive files
   */
  private List<File> getDependencyArchives() {
    return dependenciesToScan.stream()
        .map(this::getDependencyArchive)
        .flatMap(Optional::stream)
        .toList();
  }

  private Optional<File> getDependencyArchive(Dependency dependency) {
    for (Artifact artifact : mavenProject.getArtifacts()) {
      if (artifact.getGroupId().equals(dependency.getGroupId())
          && artifact.getArtifactId().equals(dependency.getArtifactId())
          && (dependency.getClassifier() == null || artifact.getClassifier()
              .equals(dependency.getClassifier()))) {
        File jarFile = artifact.getFile();
        if (jarFile == null) {
          getLog().warn(
              "Skip scanning dependency, artifact file does not exist for dependency: " + dependency);
          return Optional.empty();
        }

        return Optional.of(jarFile);
      }
    }
    getLog().warn("Skip scanning dependency, artifact for dependency not found: " + dependency);
    return Optional.empty();
  }

}
