package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.classmate.ResolvedType;
import com.github.victools.jsonschema.generator.FieldScope;
import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.MethodScope;
import io.fabric8.crd.generator.victools.CRDUtils;
import io.fabric8.crd.generator.victools.model.ValidationRuleInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findRepeatingAnnotations;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractKubernetesValidationRuleProvider<A extends Annotation, C extends Annotation>
    implements MetadataModule.MetadataProvider {

  private final Class<A> annotationClass;
  private final Class<C> annotationContainerClass;

  protected abstract A[] extractRepeatedAnnotation(C annotation);

  protected abstract ValidationRuleInfo map(A annotation);

  @Override
  public List<ValidationRuleInfo> getValidationRules(MemberScope<?, ?> scope) {
    return Stream.of(
        findValidationRulesOnClassLevel(scope.getType()),
        findValidationRulesOnField(scope),
        findValidationRulesOnGetter(scope))
        .flatMap(Collection::stream)
        .map(this::map)
        .toList();
  }

  private List<A> findValidationRulesOnField(MemberScope<?, ?> member) {
    if (member.isFakeContainerItemScope()) {
      return Collections.emptyList();
    }
    if (member instanceof FieldScope) {
      return findRepeatingAnnotations(member,
          annotationClass, annotationContainerClass, this::extractRepeatedAnnotation);
    }
    if (member instanceof MethodScope methodScope) {
      var field = methodScope.findGetterField();
      if (field != null) {
        return findRepeatingAnnotations(field,
            annotationClass, annotationContainerClass, this::extractRepeatedAnnotation);
      }
    }
    return Collections.emptyList();
  }

  private List<A> findValidationRulesOnGetter(MemberScope<?, ?> member) {
    if (member.isFakeContainerItemScope()) {
      return Collections.emptyList();
    }
    if (member instanceof FieldScope fieldScope) {
      var getter = fieldScope.findGetter();
      if (getter != null) {
        return findRepeatingAnnotations(getter,
            annotationClass, annotationContainerClass, this::extractRepeatedAnnotation);
      }
    }
    if (member instanceof MethodScope) {
      return findRepeatingAnnotations(member,
          annotationClass, annotationContainerClass, this::extractRepeatedAnnotation);
    }
    return Collections.emptyList();
  }

  private List<A> findValidationRulesOnClassLevel(ResolvedType type) {
    if (type.getErasedType().getPackageName().startsWith("java.lang")) {
      return Collections.emptyList();
    }
    return CRDUtils.findRepeatingAnnotations(type.getErasedType(), annotationClass);
  }
}
