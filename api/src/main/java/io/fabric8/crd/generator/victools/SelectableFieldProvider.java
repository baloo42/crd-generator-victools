package io.fabric8.crd.generator.victools;

import io.fabric8.crd.generator.victools.model.SelectableFieldInfo;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.util.List;

import static io.fabric8.crd.generator.victools.CRDUtils.findRepeatingAnnotations;

@FunctionalInterface
public interface SelectableFieldProvider {

  List<SelectableFieldInfo> getSelectableFields();

  @RequiredArgsConstructor
  abstract class TopLevelAnnotationSelectableFieldProvider<T extends Annotation> implements
      SelectableFieldProvider {
    private final CustomResourceInfo crInfo;
    private final Class<T> annotationClass;

    protected abstract SelectableFieldInfo map(T annotation);

    @Override
    public List<SelectableFieldInfo> getSelectableFields() {
      return findRepeatingAnnotations(crInfo.definition(), annotationClass).stream()
          .map(this::map)
          .toList();
    }
  }
}
