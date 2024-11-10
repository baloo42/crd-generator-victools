package io.fabric8.crd.generator.victools.v1;

import io.fabric8.crd.generator.victools.CustomResourceContext;
import io.fabric8.crd.generator.victools.PrinterColumnProvider;
import io.fabric8.crd.generator.victools.model.PrinterColumnInfo;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceColumnDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceColumnDefinitionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.JSONSchemaProps;
import lombok.NonNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static io.fabric8.crd.generator.victools.CRDUtils.distinctByKey;
import static java.util.Optional.ofNullable;

class PrinterColumnCollector implements PathAwareSchemaPropsVisitor.IdentifiedPropertyVisitor {

  private final List<PrinterColumnInfo> columns = new LinkedList<>();
  private final List<PrinterColumnInfo> additionalColumns = new LinkedList<>();

  private final CustomResourceContext context;

  public PrinterColumnCollector(
      @NonNull CustomResourceContext context,
      @NonNull List<PrinterColumnProvider> providers) {
    this.context = context;
    providers.stream()
        .map(PrinterColumnProvider::getPrinterColumns)
        .flatMap(Collection::stream)
        .forEach(additionalColumns::add);
  }

  @Override
  public void visit(String id, String path, JSONSchemaProps schema) {
    context.findPrinterColumnInfo(id)
        .map(info -> createColumn(path, info, schema))
        .ifPresent(columns::add);
  }

  public List<CustomResourceColumnDefinition> getColumns() {
    return Stream.of(columns, additionalColumns)
        .flatMap(Collection::stream)
        .map(this::ensureName)
        .filter(distinctByKey(PrinterColumnInfo::jsonPath))
        .sorted(Comparator.comparing(PrinterColumnInfo::jsonPath))
        .map(this::createColumnDefinition)
        .toList();
  }

  private PrinterColumnInfo ensureName(PrinterColumnInfo info) {
    String name = ofNullable(info.name())
        .orElse(info.jsonPath().substring(info.jsonPath().lastIndexOf('.') + 1));
    return info.toBuilder().name(name).build();
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
