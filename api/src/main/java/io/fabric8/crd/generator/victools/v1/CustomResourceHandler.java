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

import com.github.victools.jsonschema.generator.SchemaGenerator;
import io.fabric8.crd.generator.victools.AbstractCustomResourceHandler;
import io.fabric8.crd.generator.victools.CRDGeneratorContextInternal;
import io.fabric8.crd.generator.victools.CRDGeneratorSchemaOption;
import io.fabric8.crd.generator.victools.CustomResourceContext;
import io.fabric8.crd.generator.victools.CustomResourceInfo;
import io.fabric8.crd.generator.victools.annotation.AdditionalPrinterColumn;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceColumnDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionVersion;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionVersionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.JSONSchemaProps;
import io.fabric8.kubernetes.api.model.apiextensions.v1.JSONSchemaPropsBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.ValidationRule;
import lombok.extern.slf4j.Slf4j;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.fabric8.crd.generator.victools.AnnotationUtils.findRepeatingAnnotations;
import static io.fabric8.kubernetes.client.utils.KubernetesVersionPriority.sortByPriority;
import static java.util.Optional.ofNullable;

@Slf4j
class CustomResourceHandler extends AbstractCustomResourceHandler {

  private final Queue<Entry<CustomResourceDefinition, Set<String>>> crds = new ConcurrentLinkedQueue<>();

  @Override
  public void handle(
      CRDGeneratorContextInternal generatorContext,
      CustomResourceInfo crInfo,
      SchemaGenerator schemaGenerator,
      CustomResourceContext customResourceContext) {

    final String name = crInfo.crdName();
    final String version = crInfo.version();

    // >>> Schema-Generation Phase ---
    var rootSchemaBuilder = new JSONSchemaPropsBuilder()
        .withType("object");

    var rootSchema = schemaGenerator.generateSchema(crInfo.definition());
    log.info("rootSchema: {}", rootSchema);

    rootSchema.get("properties").properties().stream()
        .filter(prop -> !Set.of("metadata", "kind", "apiVersion").contains(prop.getKey()))
        .sorted(Entry.comparingByKey())
        .forEach(prop -> rootSchemaBuilder.addToProperties(prop.getKey(),
            generatorContext.convertValue(prop.getValue(), JSONSchemaProps.class)));

    rootSchemaBuilder.addAllToXKubernetesValidations(findTopLevelValidationRules(crInfo));

    if (generatorContext.isEnabled(CRDGeneratorSchemaOption.IMPLICIT_REQUIRED_SPEC)) {
      rootSchemaBuilder.withRequired("spec");
    }
    // <<< Schema-Generation Phase ---

    // >>> Post-Processing Phase ---
    var printerColumnCollector = new PrinterColumnCollector(customResourceContext);
    var scaleSubresourceCollector = new ScaleSubresourceCollector(customResourceContext);
    new PathAwareSchemaPropsVisitor()
        .withIdentifiedPropertyVisitor(printerColumnCollector)
        .withIdentifiedPropertyVisitor(scaleSubresourceCollector)
        .visit(rootSchemaBuilder);

    CustomResourceDefinitionVersionBuilder builder = new CustomResourceDefinitionVersionBuilder()
        .withName(version)
        .withStorage(crInfo.storage())
        .withServed(crInfo.served())
        .withDeprecated(crInfo.deprecated() ? true : null)
        .withDeprecationWarning(crInfo.deprecationWarning())
        .withNewSchema()
        .withOpenAPIV3Schema(rootSchemaBuilder.build())
        .endSchema();

    builder.addAllToAdditionalPrinterColumns(
        getAdditionalPrinterColumns(printerColumnCollector, crInfo));

    scaleSubresourceCollector.findScaleSubresource()
        .ifPresent(scale -> builder.editOrNewSubresources()
            .withScale(scale)
            .endSubresources());

    if (crInfo.statusClassName().isPresent()) {
      builder.editOrNewSubresources()
          .withNewStatus()
          .endStatus()
          .endSubresources();
    }

    CustomResourceDefinition crd = new CustomResourceDefinitionBuilder()
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
        .endSpec()
        .build();
    // <<< Post-Processing Phase ---

    crds.add(new AbstractMap.SimpleEntry<>(crd, customResourceContext.getDependentClasses()));
  }

  /**
   * Finalizes the Custom Resource Definition by combining the versions.
   *
   * @return a stream of the produced Custom Resource Definitions
   */
  @Override
  public Stream<Entry<? extends HasMetadata, Set<String>>> finish() {
    return crds.stream()
        .collect(Collectors.groupingBy(crd -> crd.getKey().getMetadata().getName()))
        .values().stream()
        .map(this::combine);
  }

  private Entry<CustomResourceDefinition, Set<String>> combine(
      List<Entry<CustomResourceDefinition, Set<String>>> definitions) {
    Entry<CustomResourceDefinition, Set<String>> primary = definitions.get(0);
    if (definitions.size() == 1) {
      return primary;
    }

    List<CustomResourceDefinitionVersion> versions = definitions.stream()
        .flatMap(crd -> crd.getKey().getSpec().getVersions().stream())
        .toList();

    Set<String> allDependentClasses = definitions.stream()
        .flatMap(crd -> crd.getValue().stream())
        .collect(Collectors.toSet());

    List<String> storageVersions = versions.stream()
        .filter(v -> ofNullable(v.getStorage()).orElse(true))
        .map(CustomResourceDefinitionVersion::getName)
        .toList();

    if (storageVersions.size() > 1) {
      throw new IllegalStateException(String.format(
          "'%s' custom resource has versions %s marked as storage. Only one version can be marked as storage per custom resource.",
          primary.getKey().getMetadata().getName(), storageVersions));
    }

    versions = sortByPriority(versions, CustomResourceDefinitionVersion::getName);

    //TODO: we could double check that the top-level metadata is consistent across all versions
    return new AbstractMap.SimpleEntry<>(
        new CustomResourceDefinitionBuilder(primary.getKey())
            .editSpec()
            .withVersions(versions)
            .endSpec()
            .build(),
        allDependentClasses);
  }

  private Collection<ValidationRule> findTopLevelValidationRules(CustomResourceInfo crInfo) {
    return findRepeatingAnnotations(
        crInfo.definition(),
        io.fabric8.generator.annotation.ValidationRule.class).stream()
        .map(CRDv1Utils::createValidationRule)
        .toList();
  }

  private Collection<CustomResourceColumnDefinition> findTopLevelPrinterColumns(CustomResourceInfo crInfo) {

    return findRepeatingAnnotations(crInfo.definition(), AdditionalPrinterColumn.class).stream()
        .map(CRDv1Utils::createColumnDefinition)
        .toList();
  }

  private Collection<CustomResourceColumnDefinition> getAdditionalPrinterColumns(
      PrinterColumnCollector collector, CustomResourceInfo crInfo) {

    return Stream.of(collector.getColumns(), findTopLevelPrinterColumns(crInfo))
        .flatMap(Collection::stream)
        .sorted(Comparator.comparing(CustomResourceColumnDefinition::getJsonPath))
        .toList();
  }

}
