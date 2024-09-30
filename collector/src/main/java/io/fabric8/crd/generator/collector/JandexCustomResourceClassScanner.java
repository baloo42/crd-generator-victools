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
package io.fabric8.crd.generator.collector;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Collects multiple Jandex index sources and allows to find Custom Resource class names in the
 * resulting composite index by using {@link JandexCustomResourceClassScanner#findCustomResourceClasses()}.
 * <p>
 * If not overridden by {@link JandexCustomResourceClassScanner#forceIndex}, the implementation uses an
 * existing Jandex index if found in the source. Otherwise, the index will be created on the fly.
 * </p>
 *
 * @see Index
 * @see JandexIndexer
 * @see JandexUtils
 */
public class JandexCustomResourceClassScanner {

  private final List<IndexView> indices = new LinkedList<>();
  private final Set<File> filesToScan = new HashSet<>();

  /**
   * If <code>true</code>, indices will always be created even if an index exists at the source.
   */
  private boolean forceIndex = false;

  @SuppressWarnings("UnusedReturnValue")
  public JandexCustomResourceClassScanner withForceIndex(boolean forceIndex) {
    this.forceIndex = forceIndex;
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public JandexCustomResourceClassScanner withIndex(IndexView... index) {
    if (index != null) {
      withIndices(Arrays.asList(index));
    }
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public JandexCustomResourceClassScanner withIndices(Collection<IndexView> indices) {
    if (indices != null) {
      indices.stream()
          .filter(Objects::nonNull)
          .forEach(this.indices::add);
    }
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public JandexCustomResourceClassScanner withFileToScan(File... files) {
    if (files != null) {
      withFilesToScan(Arrays.asList(files));
    }
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public JandexCustomResourceClassScanner withFilesToScan(Collection<File> files) {
    if (files != null) {
      files.stream()
          .filter(Objects::nonNull)
          .forEach(this.filesToScan::add);
    }
    return this;
  }

  /**
   * Finds Custom Resource classes in the pre-configured scope.
   *
   * @return the Custom Resource classes
   */
  public Collection<String> findCustomResourceClasses() {
    List<IndexView> actualIndices = new ArrayList<>(indices);
    actualIndices.add(createBaseIndex());

    IndexView index = JandexUtils.createIndex(actualIndices, filesToScan, forceIndex);

    return findCustomResourceClasses(index).stream()
        .map(ClassInfo::toString)
        .collect(Collectors.toList());
  }

  /**
   * Finds Custom Resource classes in an index.
   *
   * @param index the index
   * @return the Custom Resource classes
   *
   * @see JandexCustomResourceClassScanner#createBaseIndex()
   */
  private List<ClassInfo> findCustomResourceClasses(IndexView index) {
    // Only works if HasMetadata and all intermediate classes
    // (like CustomResource) are included in given index.
    return index.getAllKnownImplementors(HasMetadata.class)
        .stream()
        .filter(classInfo -> classInfo.hasAnnotation(Group.class))
        .filter(classInfo -> classInfo.hasAnnotation(Version.class))
        .collect(Collectors.toList());
  }

  /**
   * Creates the base index required to scan for Custom Resource classes.
   *
   * @return the base index.
   *
   * @see JandexCustomResourceClassScanner#findCustomResourceClasses(IndexView)
   */
  private Index createBaseIndex() {
    try {
      Indexer indexer = new Indexer();
      indexer.indexClass(HasMetadata.class);
      indexer.indexClass(CustomResource.class);
      return indexer.complete();
    } catch (IOException e) {
      throw new JandexException("Could not create base index", e);
    }
  }

  void reset() {
    indices.clear();
    filesToScan.clear();
    forceIndex = false;
  }

}
