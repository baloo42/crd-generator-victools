package io.fabric8.crd.generator.victools;

import io.fabric8.crd.generator.victools.model.ValidationRuleInfo;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.util.List;

import static io.fabric8.crd.generator.victools.CRDUtils.findRepeatingAnnotations;

@FunctionalInterface
public interface KubernetesValidationRuleProvider {

  List<ValidationRuleInfo> getValidationRules();

  @RequiredArgsConstructor
  abstract class TopLevelAnnotationKubernetesValidationRuleProvider<T extends Annotation>
      implements KubernetesValidationRuleProvider {

    private final CustomResourceInfo crInfo;
    private final Class<T> annotationClass;

    protected abstract ValidationRuleInfo map(T annotation);

    @Override
    public List<ValidationRuleInfo> getValidationRules() {
      return findRepeatingAnnotations(crInfo.definition(), annotationClass).stream()
          .map(this::map)
          .toList();
    }
  }
}
