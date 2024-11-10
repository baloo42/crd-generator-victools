package io.fabric8.crd.generator.victools.v1;

import io.fabric8.crd.generator.victools.CustomResourceContext;
import io.fabric8.crd.generator.victools.SelectableFieldProvider;
import io.fabric8.crd.generator.victools.model.SelectableFieldInfo;
import io.fabric8.kubernetes.api.model.apiextensions.v1.JSONSchemaProps;
import io.fabric8.kubernetes.api.model.apiextensions.v1.SelectableField;
import io.fabric8.kubernetes.api.model.apiextensions.v1.SelectableFieldBuilder;
import lombok.NonNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static io.fabric8.crd.generator.victools.CRDUtils.distinctByKey;

class SelectableFieldCollector implements PathAwareSchemaPropsVisitor.IdentifiedPropertyVisitor {

  private final List<SelectableFieldInfo> selectableFields = new LinkedList<>();
  private final List<SelectableFieldInfo> additionalSelectableFields = new LinkedList<>();

  @NonNull
  private final CustomResourceContext context;

  public SelectableFieldCollector(
      @NonNull CustomResourceContext context,
      @NonNull List<SelectableFieldProvider> providers) {
    this.context = context;

    providers.stream()
        .map(SelectableFieldProvider::getSelectableFields)
        .flatMap(Collection::stream)
        .forEach(additionalSelectableFields::add);
  }

  @Override
  public void visit(String id, String path, JSONSchemaProps schema) {
    if (context.isSelectableFieldPath(id)) {
      selectableFields.add(new SelectableFieldInfo(path));
    }
  }

  public List<SelectableField> getSelectableFields() {
    return Stream.of(selectableFields, additionalSelectableFields)
        .flatMap(Collection::stream)
        .filter(distinctByKey(SelectableFieldInfo::jsonPath))
        .sorted(Comparator.comparing(SelectableFieldInfo::jsonPath))
        .map(this::createSelectableField)
        .toList();
  }

  private SelectableField createSelectableField(SelectableFieldInfo info) {
    return new SelectableFieldBuilder()
        .withJsonPath(info.jsonPath())
        .build();
  }

}
