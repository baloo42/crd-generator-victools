package io.fabric8.crd.generator.victools.v1;

import io.fabric8.crd.generator.victools.AdditionalPrinterColumnProvider;
import io.fabric8.crd.generator.victools.CustomResourceContext;
import io.fabric8.crd.generator.victools.model.PrinterColumnInfo;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceColumnDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceColumnDefinitionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.JSONSchemaProps;
import lombok.NonNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.fabric8.crd.generator.victools.CRDUtils.distinctByKey;

class PrinterColumnCollector implements PathAwareSchemaPropsVisitor.IdentifiedPropertyVisitor {

  private final Map<String, CustomResourceColumnDefinition> columns = new HashMap<>();

  private final CustomResourceContext context;
  private final List<AdditionalPrinterColumnProvider> providers;

  public PrinterColumnCollector(
      @NonNull CustomResourceContext context,
      @NonNull List<AdditionalPrinterColumnProvider> providers) {
    this.context = context;
    this.providers = providers;
  }

  @Override
  public void visit(String id, String path, JSONSchemaProps schema) {
    context.findPrinterColumnInfo(id)
        .map(info -> createColumn(path, info, schema))
        .ifPresent(this::addColumn);
  }

  public List<CustomResourceColumnDefinition> getColumns() {
    return Stream.of(columns.values(), getAdditionalColumns())
        .flatMap(Collection::stream)
        .filter(distinctByKey(CustomResourceColumnDefinition::getJsonPath))
        .sorted(Comparator.comparing(CustomResourceColumnDefinition::getJsonPath))
        .toList();
  }

  private List<CustomResourceColumnDefinition> getAdditionalColumns() {
    return providers.stream()
        .map(AdditionalPrinterColumnProvider::getAdditionalPrinterColumns)
        .flatMap(Collection::stream)
        .map(this::createColumn)
        .collect(Collectors.toList());
  }

  private void addColumn(CustomResourceColumnDefinition column) {
    columns.put(column.getJsonPath(), column);
  }

  private CustomResourceColumnDefinition createColumn(
      String path,
      PrinterColumnInfo info,
      JSONSchemaProps schema) {

    String name = info.findName().orElse(path.substring(path.lastIndexOf('.') + 1));
    return createColumn(PrinterColumnInfo.builder()
        .name(name)
        .jsonPath(path)
        .description(info.description())
        .type(schema.getType())
        .format(info.format())
        .priority(info.priority())
        .build());
  }

  private CustomResourceColumnDefinition createColumn(PrinterColumnInfo info) {
    return new CustomResourceColumnDefinitionBuilder()
        .withName(info.name())
        .withJsonPath(info.jsonPath())
        .withDescription(info.description())
        .withType(info.type())
        .withFormat(info.format())
        .withPriority(info.priority())
        .build();
  }

}
