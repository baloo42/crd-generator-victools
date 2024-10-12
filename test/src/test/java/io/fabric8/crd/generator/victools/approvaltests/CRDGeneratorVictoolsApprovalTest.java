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
package io.fabric8.crd.generator.victools.approvaltests;

import com.spun.util.tests.TestUtils;
import io.fabric8.crd.generator.victools.approvaltests.conversion.NoneConversionExample;
import io.fabric8.crd.generator.victools.approvaltests.externaldocs.ExternalDoc;
import io.fabric8.crd.generator.victools.approvaltests.k8svalidation.K8sValidation;
import io.fabric8.crd.generator.victools.approvaltests.maptype.MapType;
import io.fabric8.crd.generator.victools.approvaltests.printercolum.PrinterColumn;
import io.fabric8.crd.generator.victools.approvaltests.replica.Replica;
import io.fabric8.crd.generator.victools.approvaltests.schemafrom.SchemaFrom;
import io.fabric8.crd.generator.victools.approvaltests.selectablefield.SelectableField;
import io.fabric8.crd.generator.victools.approvaltests.subtype.SubType;
import io.fabric8.crd.generator.victools.approvaltests.validation.FkcValidation;
import io.fabric8.crd.generator.victools.approvaltests.validation.Validation;
import io.fabric8.kubernetes.client.CustomResource;
import org.approvaltests.Approvals;
import org.approvaltests.namer.StackTraceNamer;
import org.approvaltests.writers.FileApprovalWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CRDGeneratorVictoolsApprovalTest {

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  File tempDir;

  @BeforeEach
  void setUp() {
    Approvals.settings().allowMultipleVerifyCallsForThisClass();
  }

  @ParameterizedTest(name = "{1}.{2} parallel={3}")
  @MethodSource("crdV1ApprovalTests")
  @DisplayName("CRD Generator Victools Approval Tests")
  void apiVictoolsApprovalTest(
      Class<? extends CustomResource<?, ?>>[] crClasses, String expectedCrd, String version, boolean parallel) {
    Approvals.settings().allowMultipleVerifyCallsForThisMethod();
    final Map<String, Map<String, io.fabric8.crd.generator.victools.CRDInfo>> result = new io.fabric8.crd.generator.victools.CRDGenerator()
        .withParallelGenerationEnabled(parallel)
        .withOutputDirectory(tempDir)
        .customResourceClasses(crClasses)
        .forCRDVersions(version)
        .detailedGenerate()
        .getCRDDetailsPerNameAndVersion();

    assertThat(result)
        .withFailMessage(() -> "Could not find expected CRD " + expectedCrd
            + " in results. Found instead: " + result.keySet())
        .containsKey(expectedCrd)
        .extractingByKey(expectedCrd)
        .isNotNull();

    Approvals.verify(
        new FileApprovalWriter(new File(result.get(expectedCrd).get(version).getFilePath())),
        new Namer(expectedCrd, version));

    for (var schemaFilePathEntry : result.get(expectedCrd).get(version).getSchemaFilePaths().entrySet()) {
      final var resourceAPIVersion = schemaFilePathEntry.getKey();
      final var schemaFile = schemaFilePathEntry.getValue();

      Approvals.verify(
          new FileApprovalWriter(new File(schemaFile)),
          new Namer(expectedCrd, version, resourceAPIVersion));
    }
  }

  static Stream<Arguments> crdV1ApprovalTests() {
    return crdApprovalBaseCases("v1")
        .map(tc -> Arguments.of(tc.crClasses, tc.expectedCrd, tc.version, tc.parallel));
  }

  static Stream<TestCase> crdApprovalBaseCases(String crdVersion) {
    final List<TestCase> cases = new ArrayList<>();
    for (boolean parallel : new boolean[] { false, true }) {
      cases.add(new TestCase("validations.samples.fabric8.io", crdVersion, parallel, Validation.class));
      cases.add(new TestCase("fkcvalidations.samples.fabric8.io", crdVersion, parallel, FkcValidation.class));
      cases.add(new TestCase("k8svalidations.samples.fabric8.io", crdVersion, parallel, K8sValidation.class));
      cases.add(new TestCase("replicas.samples.fabric8.io", crdVersion, parallel, Replica.class));
      cases.add(new TestCase("externaldocs.samples.fabric8.io", crdVersion, parallel, ExternalDoc.class));
      cases.add(new TestCase("maptypes.samples.fabric8.io", crdVersion, parallel, MapType.class));
      cases.add(new TestCase("subtypes.samples.fabric8.io", crdVersion, parallel, SubType.class));
      cases.add(new TestCase("printercolumns.samples.fabric8.io", crdVersion, parallel, PrinterColumn.class));
      cases.add(new TestCase("selectablefields.samples.fabric8.io", crdVersion, parallel, SelectableField.class));
      cases.add(new TestCase("schemafroms.samples.fabric8.io", crdVersion, parallel, SchemaFrom.class));
      cases.add(new TestCase("deprecationexamples.samples.fabric8.io", crdVersion, parallel,
          io.fabric8.crd.generator.victools.approvaltests.deprecated.v1.DeprecationExample.class,
          io.fabric8.crd.generator.victools.approvaltests.deprecated.v1beta1.DeprecationExample.class,
          io.fabric8.crd.generator.victools.approvaltests.deprecated.v2.DeprecationExample.class));
      cases.add(new TestCase("noneconversions.samples.fabric8.io", crdVersion, parallel, NoneConversionExample.class));
      cases.add(new TestCase("webhookconversions.samples.fabric8.io", crdVersion, parallel,
          io.fabric8.crd.generator.victools.approvaltests.conversion.v1.WebhookConversionExample.class,
          io.fabric8.crd.generator.victools.approvaltests.conversion.v2.WebhookConversionExample.class));
    }
    return cases.stream();
  }

  private static final class TestCase {
    private final Class<? extends CustomResource<?, ?>>[] crClasses;
    private final String expectedCrd;
    private final String version;
    private final boolean parallel;

    @SafeVarargs
    public TestCase(
        String expectedCrd,
        String version,
        boolean parallel,
        Class<? extends CustomResource<?, ?>>... crClasses) {

      this.expectedCrd = expectedCrd;
      this.version = version;
      this.parallel = parallel;
      this.crClasses = crClasses;
    }
  }

  private static final class Namer extends StackTraceNamer {
    private final String additionalInformation;

    public Namer(String... parameters) {
      super(TestUtils.getCurrentFileForMethod(0), null);
      additionalInformation = String.join(".", parameters);
    }

    @Override
    public String getApprovalName() {
      return String.format("%s.approvalTest.%s", CRDGeneratorVictoolsApprovalTest.class.getSimpleName(), additionalInformation);
    }
  }

}
