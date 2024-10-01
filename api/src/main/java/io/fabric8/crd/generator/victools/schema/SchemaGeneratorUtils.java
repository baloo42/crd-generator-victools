package io.fabric8.crd.generator.victools.schema;

import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.TypeScope;
import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

@UtilityClass
public class SchemaGeneratorUtils {

  public static String emptyToNull(String value) {
    return ofNullable(value).filter(s -> !s.isEmpty()).orElse(null);
  }

  public static <A extends Annotation, R extends Annotation> List<A> findRepeatingAnnotations(
      MemberScope<?, ?> member,
      Class<A> annotationClass,
      Class<R> repeatingAnnotationClass,
      Function<R, A[]> fn) {

    return findAnnotation(member, repeatingAnnotationClass)
        .map(fn)
        .map(List::of)
        .orElseGet(() -> findAnnotation(member, annotationClass).stream().toList());
  }

  public static <A extends Annotation> Optional<A> findAnnotation(
      MemberScope<?, ?> member,
      Class<A> clazz) {

    return Optional.ofNullable(member.getAnnotation(clazz));
  }

  public static <A extends Annotation> Optional<A> findAnnotationConsideringFieldAndGetter(
      MemberScope<?, ?> member,
      Class<A> clazz) {

    return Optional.ofNullable(member.getAnnotationConsideringFieldAndGetter(clazz));
  }

  public static <A extends Annotation> Optional<A> findAnnotation(
      TypeScope scope,
      Class<A> clazz) {
    return Optional.ofNullable(scope.getType().getErasedType().getAnnotation(clazz));
  }

  public static <A extends Annotation> List<A> findRepeatingAnnotations(
      TypeScope typeScope,
      Class<A> clazz) {

    return List.of(typeScope.getType().getErasedType().getAnnotationsByType(clazz));
  }
}
