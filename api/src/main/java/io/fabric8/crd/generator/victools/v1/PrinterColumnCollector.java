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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static io.fabric8.crd.generator.victools.CRDUtils.distinctByKey;
import static java.util.Optional.ofNullable;

class PrinterColumnCollector implements PathAwareSchemaPropsVisitor.IdentifiedPropertyVisitor {

  private final Map<String, CustomResourceColumnDefinition> columns = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  private final CustomResourceContext context;

  public PrinterColumnCollector(
      @NonNull CustomResourceContext context,
      @NonNull List<AdditionalPrinterColumnProvider> providers) {
    this.context = context;
    providers.stream()
        .map(AdditionalPrinterColumnProvider::getAdditionalPrinterColumns)
        .flatMap(Collection::stream)
        .forEach(this::addColumn);
  }

  @Override
  public void visit(String id, String path, JSONSchemaProps schema) {
    context.findPrinterColumnInfo(id)
        .map(info -> createColumn(path, info, schema))
        .ifPresent(this::addColumn);
  }

  public List<CustomResourceColumnDefinition> getColumns() {
    return columns.values().stream()
        .filter(distinctByKey(CustomResourceColumnDefinition::getJsonPath))
        .sorted(Comparator.comparing(CustomResourceColumnDefinition::getJsonPath))
        .toList();
  }

  private void addColumn(PrinterColumnInfo info) {
    String name = ofNullable(info.name())
        .orElse(info.jsonPath().substring(info.jsonPath().lastIndexOf('.') + 1));

    var column = createColumnDefinition(info.toBuilder().name(name).build());
    columns.put(column.getJsonPath(), column);
  }

  private PrinterColumnInfo createColumn(
      String path,
      PrinterColumnInfo info,
      JSONSchemaProps schema) {

    return info.toBuilder()
        .jsonPath(path)
        .type(ofNullable(info.type()).orElse(schema.getType()))
        .format(schema.getFormat())
        .build();
  }

  private CustomResourceColumnDefinition createColumnDefinition(PrinterColumnInfo info) {
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
