package io.fabric8.crd.generator.victools.v1;

import io.fabric8.crd.generator.victools.CustomResourceContext;
import io.fabric8.crd.generator.victools.CustomResourceInfo;
import io.fabric8.crd.generator.victools.annotation.AdditionalSelectableField;
import io.fabric8.kubernetes.api.model.apiextensions.v1.JSONSchemaProps;
import io.fabric8.kubernetes.api.model.apiextensions.v1.SelectableField;
import io.fabric8.kubernetes.api.model.apiextensions.v1.SelectableFieldBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.fabric8.crd.generator.victools.CRDUtils.distinctByKey;
import static io.fabric8.crd.generator.victools.CRDUtils.findRepeatingAnnotations;

@RequiredArgsConstructor
class SelectableFieldCollector implements PathAwareSchemaPropsVisitor.IdentifiedPropertyVisitor {

  private final Map<String, SelectableField> topLevelSelectableFields = new HashMap<>();
  private final Map<String, SelectableField> selectableFields = new HashMap<>();

  @NonNull
  private final CustomResourceContext context;

  public SelectableFieldCollector(
      @NonNull CustomResourceInfo crInfo,
      @NonNull CustomResourceContext context) {
    this.context = context;

    findTopLevelSelectableFields(crInfo)
        .forEach(this::addTopLevelSelectableField);
  }

  @Override
  public void visit(String id, String path, JSONSchemaProps schema) {
    if (context.isSelectableFieldPath(id)) {
      addSelectableField(createSelectableField(path));
    }
  }

  public List<SelectableField> getSelectableFields() {
    return Stream.of(selectableFields.values(), topLevelSelectableFields.values())
        .flatMap(Collection::stream)
        .filter(distinctByKey(SelectableField::getJsonPath))
        .sorted(Comparator.comparing(SelectableField::getJsonPath))
        .toList();
  }

  private void addTopLevelSelectableField(SelectableField column) {
    topLevelSelectableFields.put(column.getJsonPath(), column);
  }

  private void addSelectableField(SelectableField column) {
    selectableFields.put(column.getJsonPath(), column);
  }

  private static SelectableField createSelectableField(String jsonPath) {
    return new SelectableFieldBuilder()
        .withJsonPath(jsonPath)
        .build();
  }

  private static Collection<SelectableField> findTopLevelSelectableFields(
      CustomResourceInfo crInfo) {
    return findRepeatingAnnotations(crInfo.definition(), AdditionalSelectableField.class).stream()
        .map(SelectableFieldCollector::createSelectableField)
        .toList();
  }

  private static SelectableField createSelectableField(AdditionalSelectableField annotation) {
    return createSelectableField(annotation.jsonPath());
  }

}
