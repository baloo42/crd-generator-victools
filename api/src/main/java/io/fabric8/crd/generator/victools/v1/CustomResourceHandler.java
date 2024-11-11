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
package io.fabric8.crd.generator.victools.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import io.fabric8.crd.generator.victools.AbstractCustomResourceHandler;
import io.fabric8.crd.generator.victools.CRDGeneratorContextInternal;
import io.fabric8.crd.generator.victools.CRDGeneratorSchemaOption;
import io.fabric8.crd.generator.victools.CRDResult;
import io.fabric8.crd.generator.victools.CustomResourceContext;
import io.fabric8.crd.generator.victools.CustomResourceInfo;
import io.fabric8.crd.generator.victools.KubernetesValidationRuleProvider;
import io.fabric8.crd.generator.victools.PrinterColumnProvider;
import io.fabric8.crd.generator.victools.SelectableFieldProvider;
import io.fabric8.crd.generator.victools.schema.AdditionalKubernetesValidationRuleProvider;
import io.fabric8.crd.generator.victools.schema.AdditionalPrinterColumnProvider;
import io.fabric8.crd.generator.victools.schema.AdditionalSelectableFieldProvider;
import io.fabric8.crd.generator.victools.schema.fkc.FkcAdditionalKubernetesValidationRuleProvider;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionVersion;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionVersionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.JSONSchemaProps;
import io.fabric8.kubernetes.api.model.apiextensions.v1.JSONSchemaPropsBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.fabric8.kubernetes.client.utils.KubernetesVersionPriority.sortByPriority;
import static java.util.Optional.ofNullable;

@Slf4j
class CustomResourceHandler extends AbstractCustomResourceHandler {

  private record CrdEntry(CustomResourceDefinition crd, Set<String> dependentClasses) {
  }

  private final Queue<CrdEntry> crdQueue = new ConcurrentLinkedQueue<>();

  @Override
  public void handle(
      CRDGeneratorContextInternal generatorContext,
      CustomResourceInfo crInfo,
      SchemaGenerator schemaGenerator,
      CustomResourceContext customResourceContext) {

    final String name = crInfo.crdName();
    final String version = crInfo.version();

    // >>> Schema-Generation Phase ---
    final var rootSchemaBuilder = new JSONSchemaPropsBuilder()
        .withType("object");

    final var rootSchemaFromGenerator = schemaGenerator.generateSchema(crInfo.definition());

    rootSchemaFromGenerator.get("properties").properties().stream()
        .filter(prop -> !Set.of("metadata", "kind", "apiVersion").contains(prop.getKey()))
        .sorted(Entry.comparingByKey())
        .forEach(prop -> rootSchemaBuilder.addToProperties(prop.getKey(),
            generatorContext.convertValue(prop.getValue(), JSONSchemaProps.class)));

    if (generatorContext.isEnabled(CRDGeneratorSchemaOption.IMPLICIT_REQUIRED_SPEC)) {
      rootSchemaBuilder.withRequired("spec");
    }
    final var rootSchema = rootSchemaBuilder.build();
    // <<< Schema-Generation Phase ---

    // >>> Post-Processing Phase ---
    var printerColumnProviders = new LinkedList<PrinterColumnProvider>();
    var selectableFieldProviders = new LinkedList<SelectableFieldProvider>();
    var validationRuleProviders = new LinkedList<KubernetesValidationRuleProvider>();

    if (generatorContext.isEnabled(CRDGeneratorSchemaOption.OWN_ANNOTATIONS)) {
      printerColumnProviders.add(new AdditionalPrinterColumnProvider(crInfo));
      selectableFieldProviders.add(new AdditionalSelectableFieldProvider(crInfo));
      validationRuleProviders.add(new AdditionalKubernetesValidationRuleProvider(crInfo));
    }

    if (generatorContext.isEnabled(CRDGeneratorSchemaOption.FKC_ANNOTATIONS)) {
      // TODO: add FkcAdditionalPrinterColumnProvider(crInfo) once updated to fabric8/kubernetes-client v7
      // TODO: add FkcAdditionalSelectableFieldProvider(crInfo) once updated to fabric8/kubernetes-client v7
      validationRuleProviders.add(new FkcAdditionalKubernetesValidationRuleProvider(crInfo));
    }

    var conversionCollector = new ConversionCollector(crInfo);
    var printerColumnCollector = new PrinterColumnCollector(customResourceContext, printerColumnProviders);
    var selectableFieldCollector = new SelectableFieldCollector(customResourceContext, selectableFieldProviders);
    var scaleSubresourceCollector = new ScaleSubresourceCollector(customResourceContext);
    var kubernetesValidationCollector = new KubernetesValidationCollector(customResourceContext, validationRuleProviders);
    new PathAwareSchemaPropsVisitor()
        .withDirectPropertyVisitor(printerColumnCollector)
        .withDirectPropertyVisitor(selectableFieldCollector)
        .withDirectPropertyVisitor(scaleSubresourceCollector)
        .withPropertyVisitor(kubernetesValidationCollector)
        .visit(rootSchema);

    rootSchema.setXKubernetesValidations(kubernetesValidationCollector.getTopLevelValidationRules());

    CustomResourceDefinitionVersionBuilder builder = new CustomResourceDefinitionVersionBuilder()
        .withName(version)
        .withStorage(crInfo.storage())
        .withServed(crInfo.served())
        .withDeprecated(crInfo.deprecated() ? true : null)
        .withDeprecationWarning(crInfo.deprecationWarning())
        .withNewSchema()
        .withOpenAPIV3Schema(rootSchema)
        .endSchema();

    builder.addAllToAdditionalPrinterColumns(printerColumnCollector.getColumns());
    builder.addAllToSelectableFields(selectableFieldCollector.getSelectableFields());

    scaleSubresourceCollector.findScaleSubresource()
        .ifPresent(scale -> builder.editOrNewSubresources()
            .withScale(scale)
            .endSubresources());

    crInfo.statusClassName()
        .ifPresent(s -> builder.editOrNewSubresources()
            .withNewStatus()
            .endStatus()
            .endSubresources());

    var crdBuilder = new CustomResourceDefinitionBuilder()
        .withNewMetadata()
        .withName(name)
        .withAnnotations(crInfo.annotations())
        .withLabels(crInfo.labels())
        .endMetadata()
        .withNewSpec()
        .withScope(crInfo.scope().value())
        .withGroup(crInfo.group())
        .withNewNames()
        .withKind(crInfo.kind())
        .withShortNames(crInfo.shortNames())
        .withPlural(crInfo.plural())
        .withSingular(crInfo.singular())
        .endNames()
        .addToVersions(builder.build())
        .endSpec();

    conversionCollector.findConversion()
        .ifPresent(customResourceConversion -> crdBuilder.editSpec()
            .withConversion(customResourceConversion)
            .endSpec());

    CustomResourceDefinition crd = crdBuilder.build();
    // <<< Post-Processing Phase ---

    crdQueue.add(new CrdEntry(crd, customResourceContext.getDependentClasses()));
  }

  /**
   * Finalizes the Custom Resource Definition by combining the versions.
   *
   * @return a stream of the produced Custom Resource Definitions
   */
  @Override
  public Stream<CRDResult> finish(CRDGeneratorContextInternal context) {
    return crdQueue.stream()
        .collect(Collectors.groupingBy(entry -> getName(entry.crd())))
        .values().stream()
        .peek(CustomResourceHandler::assertOneVersionPerDefinition)
        .map(CustomResourceHandler::sortByVersion)
        .map(crdEntries -> combine(context, crdEntries));
  }

  /**
   * Combines multiple CrdEntries of the same Custom Resource kind to the final CRD.
   *
   * @param context the generator context.
   * @param crdEntries the list of CrdEntries to combine.
   * @return the resulting CRD and metadata.
   */
  private CRDResult combine(
      CRDGeneratorContextInternal context,
      List<CrdEntry> crdEntries) {
    // At this stage it is ensured that:
    // - each entry contains only one version
    // - the entries are sorted by version
    // - the first entry contains the latest version (--> primary)
    var primary = crdEntries.get(0);
    if (crdEntries.size() == 1) {
      // no combining necessary
      var version = getFirstVersion(primary.crd());
      var schemas = Map.of(version.getName(), convertToJsonNode(version, context));
      return CRDResult.builder()
          .crd(primary.crd())
          .resourceGroup(getResourceGroup(primary.crd()))
          .resourceKind(getResourceKind(primary.crd()))
          .resourceSingular(getResourceSingular(primary.crd()))
          .resourcePlural(getResourcePlural(primary.crd()))
          .resourceVersions(Set.of(version.getName()))
          .schemas(schemas)
          .dependentClasses(primary.dependentClasses())
          .build();
    }

    List<CustomResourceDefinition> crds = crdEntries.stream()
        .map(CrdEntry::crd)
        .toList();

    assertConsistentMetadata(crds);

    List<CustomResourceDefinitionVersion> versions = crds.stream()
        .flatMap(crd -> crd.getSpec().getVersions().stream())
        .toList();

    assertSingleStorageVersion(versions, getName(primary.crd()));

    Set<String> allDependentClasses = crdEntries.stream()
        .flatMap(crd -> crd.dependentClasses().stream())
        .collect(Collectors.toSet());

    var schemas = versions.stream()
        .collect(Collectors.toMap(CustomResourceDefinitionVersion::getName,
            customResourceDefinitionVersion -> convertToJsonNode(customResourceDefinitionVersion, context)));

    return CRDResult.builder()
        .crd(new CustomResourceDefinitionBuilder(primary.crd())
            .editSpec()
            .withVersions(versions)
            .endSpec()
            .build())
        .dependentClasses(allDependentClasses)
        .schemas(schemas)
        .resourceGroup(getResourceGroup(primary.crd()))
        .resourceKind(getResourceKind(primary.crd()))
        .resourceSingular(getResourceSingular(primary.crd()))
        .resourcePlural(getResourcePlural(primary.crd()))
        .resourceVersions(versions.stream()
            .map(CustomResourceDefinitionVersion::getName)
            .collect(Collectors.toSet()))
        .build();
  }

  private static JsonNode convertToJsonNode(CustomResourceDefinitionVersion version, CRDGeneratorContextInternal context) {
    return context.convertValueToJsonNode(version.getSchema().getOpenAPIV3Schema());
  }

  private static String getName(CustomResourceDefinition crd) {
    return crd.getMetadata().getName();
  }

  private static String getResourceKind(CustomResourceDefinition crd) {
    return crd.getSpec().getNames().getKind();
  }

  private static String getResourceGroup(CustomResourceDefinition crd) {
    return crd.getSpec().getGroup();
  }

  private static String getResourcePlural(CustomResourceDefinition crd) {
    return crd.getSpec().getNames().getPlural();
  }

  private static String getResourceSingular(CustomResourceDefinition crd) {
    return crd.getSpec().getNames().getSingular();
  }

  private static CustomResourceDefinitionVersion getFirstVersion(CustomResourceDefinition crd) {
    return crd.getSpec().getVersions().stream().findFirst().orElseThrow();
  }

  private static List<CrdEntry> sortByVersion(List<CrdEntry> definitions) {
    return sortByPriority(definitions, entry -> getFirstVersion(entry.crd()).getName());
  }

  private static void assertOneVersionPerDefinition(List<CrdEntry> definitions) {
    for (CrdEntry entry : definitions) {
      if (entry.crd().getSpec().getVersions() == null) {
        throw new IllegalStateException("CrdEntry contains no version");
      }
      if (entry.crd().getSpec().getVersions().size() != 1) {
        throw new IllegalStateException(
            "CrdEntry contains %s versions. At this stage exactly one is expected.".formatted(
                entry.crd().getSpec().getVersions().size()));
      }
    }
  }

  private static void assertSingleStorageVersion(List<CustomResourceDefinitionVersion> versions, String crdName) {
    List<String> storageVersions = versions.stream()
        .filter(v -> ofNullable(v.getStorage()).orElse(true))
        .map(CustomResourceDefinitionVersion::getName)
        .toList();

    if (storageVersions.size() > 1) {
      throw new IllegalStateException(String.format(
          "'%s' custom resource has versions %s marked as storage. Only one version can be marked as storage per custom resource.",
          crdName, storageVersions));
    }
  }

  private static void assertConsistentMetadata(List<CustomResourceDefinition> crds) {
    if (crds.isEmpty()) {
      // should never happen
      throw new IllegalStateException("At least one version must be generated");
    }
    var iterator = crds.iterator();
    var primary = iterator.next();

    while (iterator.hasNext()) {
      var crd = iterator.next();

      if (!getResourceKind(primary).equals(getResourceKind(crd))) {
        throw new IllegalStateException(
            "ResourceKind is not consistent across all definitions: "
                + getResourceKind(primary) + " != " + getResourceKind(crd));
      }

      if (!getResourceGroup(primary).equals(getResourceGroup(crd))) {
        throw new IllegalStateException(
            "ResourceGroup is not consistent across all definitions: "
                + getResourceGroup(primary) + " != " + getResourceGroup(crd));
      }

      if (!getResourceSingular(primary).equals(getResourceSingular(crd))) {
        throw new IllegalStateException(
            "ResourceSingular is not consistent across all definitions: "
                + getResourceSingular(primary) + " != " + getResourceSingular(crd));
      }

      if (!getResourcePlural(primary).equals(getResourcePlural(crd))) {
        throw new IllegalStateException(
            "ResourcePlural is not consistent across all definitions: "
                + getResourcePlural(primary) + " != " + getResourcePlural(crd));
      }
    }
  }

}
