package io.fabric8.crd.generator.victools.v1;

import io.fabric8.crd.generator.victools.CustomResourceContext;
import io.fabric8.crd.generator.victools.CustomResourceInfo;
import io.fabric8.crd.generator.victools.PrinterColumnInfo;
import io.fabric8.crd.generator.victools.annotation.AdditionalPrinterColumn;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceColumnDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceColumnDefinitionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.JSONSchemaProps;
import lombok.NonNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.fabric8.crd.generator.victools.CRDUtils.distinctByKey;
import static io.fabric8.crd.generator.victools.CRDUtils.emptyToNull;
import static io.fabric8.crd.generator.victools.CRDUtils.findRepeatingAnnotations;

class PrinterColumnCollector implements PathAwareSchemaPropsVisitor.IdentifiedPropertyVisitor {

  private final Map<String, CustomResourceColumnDefinition> topLevelColumns = new HashMap<>();
  private final Map<String, CustomResourceColumnDefinition> columns = new HashMap<>();

  @NonNull
  private final CustomResourceContext context;

  public PrinterColumnCollector(
      @NonNull CustomResourceInfo crInfo,
      @NonNull CustomResourceContext context) {
    this.context = context;

    findTopLevelColumns(crInfo)
        .forEach(this::addTopLevelColumn);
  }

  @Override
  public void visit(String id, String path, JSONSchemaProps schema) {
    context.findPrinterColumnInfo(id)
        .map(info -> createColumn(path, info, schema))
        .ifPresent(this::addColumn);
  }

  public List<CustomResourceColumnDefinition> getColumns() {
    return Stream.of(columns.values(), topLevelColumns.values())
        .flatMap(Collection::stream)
        .filter(distinctByKey(CustomResourceColumnDefinition::getJsonPath))
        .sorted(Comparator.comparing(CustomResourceColumnDefinition::getJsonPath))
        .toList();
  }

  private void addTopLevelColumn(CustomResourceColumnDefinition column) {
    topLevelColumns.put(column.getJsonPath(), column);
  }

  private void addColumn(CustomResourceColumnDefinition column) {
    columns.put(column.getJsonPath(), column);
  }

  private CustomResourceColumnDefinition createColumn(
      String path,
      PrinterColumnInfo info,
      JSONSchemaProps schema) {

    String name = info.findName().orElse(path.substring(path.lastIndexOf('.') + 1));
    return new CustomResourceColumnDefinitionBuilder()
        .withName(name)
        .withType(schema.getType())
        .withFormat(info.format())
        .withPriority(info.priority())
        .withJsonPath(path)
        .build();
  }

  private Collection<CustomResourceColumnDefinition> findTopLevelColumns(
      CustomResourceInfo crInfo) {
    return findRepeatingAnnotations(crInfo.definition(), AdditionalPrinterColumn.class).stream()
        .map(PrinterColumnCollector::createColumn)
        .toList();
  }

  private static CustomResourceColumnDefinition createColumn(AdditionalPrinterColumn annotation) {
    return new CustomResourceColumnDefinitionBuilder()
        .withName(emptyToNull(annotation.name()))
        .withJsonPath(annotation.jsonPath())
        .withPriority(annotation.priority())
        .build();
  }

}
