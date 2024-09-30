package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.classmate.AnnotationInclusion;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.FieldScope;
import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.MethodScope;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import io.fabric8.crd.generator.victools.AnnotationUtils;
import io.fabric8.crd.generator.victools.CRDGeneratorContextInternal;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.fabric8.crd.generator.victools.spi.KubernetesSchemaKeyword.KUBERNETES_VALIDATIONS;
import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findRepeatingAnnotations;

@Slf4j
public abstract class AbstractKubernetesValidationModule<A extends Annotation, C extends Annotation>
    extends AbstractCRDGeneratorModule {

  private final Class<A> annotationClass;
  private final Class<C> annotationContainerClass;
  private final Function<C, A[]> annotationsFromContainerFunction;
  private final Function<A, Object> createRuleFromAnnotationFunction;

  protected AbstractKubernetesValidationModule(
      CRDGeneratorContextInternal context,
      Class<A> annotationClass,
      Class<C> annotationContainerClass,
      Function<C, A[]> annotationsFromContainerFunction,
      Function<A, Object> createRuleFromAnnotationFunction) {
    super(context);
    this.annotationClass = annotationClass;
    this.annotationContainerClass = annotationContainerClass;
    this.annotationsFromContainerFunction = annotationsFromContainerFunction;
    this.createRuleFromAnnotationFunction = createRuleFromAnnotationFunction;
  }

  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.forFields().withInstanceAttributeOverride(this::overrideInstanceAttributes);
    builder.forMethods().withInstanceAttributeOverride(this::overrideInstanceAttributes);

    builder.withAnnotationInclusionOverride(annotationClass, AnnotationInclusion.INCLUDE_AND_INHERIT);
    builder.withAnnotationInclusionOverride(annotationContainerClass, AnnotationInclusion.INCLUDE_AND_INHERIT);
  }
  private void overrideInstanceAttributes(
      ObjectNode attributes,
      MemberScope<?,?> member,
      SchemaGenerationContext context) {

    var annotationsOnClass = findValidationRulesOnClassLevel(member.getType());
    var annotationsOnField = findValidationRulesOnField(member);
    var annotationsOnGetter = findValidationRulesOnGetter(member);

    var annotations = Stream.of(annotationsOnClass, annotationsOnField, annotationsOnGetter)
        .flatMap(Collection::stream)
        .toList();

    mergeValidationRules(attributes, annotations);
  }

  private List<A> findValidationRulesOnField(MemberScope<?,?> member){
    if(member.isFakeContainerItemScope()) {
      return Collections.emptyList();
    }
    if(member instanceof FieldScope) {
      return findRepeatingAnnotations(member,
          annotationClass, annotationContainerClass, annotationsFromContainerFunction);
    }
    if(member instanceof MethodScope methodScope) {
      var field = methodScope.findGetterField();
      if(field != null) {
        return findRepeatingAnnotations(field,
            annotationClass, annotationContainerClass, annotationsFromContainerFunction);
      }
    }
    return Collections.emptyList();
  }

  private List<A> findValidationRulesOnGetter(MemberScope<?,?> member){
    if(member.isFakeContainerItemScope()) {
      return Collections.emptyList();
    }
    if(member instanceof FieldScope fieldScope) {
      var getter = fieldScope.findGetter();
      if(getter != null) {
        return findRepeatingAnnotations(getter,
            annotationClass, annotationContainerClass, annotationsFromContainerFunction);
      }
    }
    if(member instanceof MethodScope) {
      return findRepeatingAnnotations(member,
          annotationClass, annotationContainerClass, annotationsFromContainerFunction);
    }
    return Collections.emptyList();
  }

  private List<A> findValidationRulesOnClassLevel(ResolvedType type) {
    if(type.getErasedType().getPackageName().startsWith("java.lang")) {
      return Collections.emptyList();
    }
    return AnnotationUtils.findRepeatingAnnotations(type.getErasedType(), annotationClass);
  }

  private void mergeValidationRules(
      ObjectNode node,
      List<A> annotations) {

    if (!annotations.isEmpty()) {
      var validationsNode = node.withArrayProperty(KUBERNETES_VALIDATIONS.getValue());
      var rules = annotations.stream()
          .map(createRuleFromAnnotationFunction)
          .map(getContext()::convertValueToJsonNode)
          .toList();
      validationsNode.addAll(rules);
    }
  }
}
