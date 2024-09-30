package io.fabric8.crd.generator.victools.v1;

import io.fabric8.crd.generator.victools.CustomResourceContext;
import io.fabric8.crd.generator.victools.PrinterColumnInfo;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceColumnDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceColumnDefinitionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.JSONSchemaProps;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
class PrinterColumnCollector implements PathAwareSchemaPropsVisitor.IdentifiedPropertyVisitor {

  @Getter
  private final List<CustomResourceColumnDefinition> columns = new LinkedList<>();
  @NonNull
  private final CustomResourceContext context;

  @Override
  public void visit(String id, String path, JSONSchemaProps schema) {
    context.findPrinterColumnInfo(id)
        .map(info -> map(path, info, schema))
        .ifPresent(columns::add);
  }

  private CustomResourceColumnDefinition map(
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
}
