package io.fabric8.crd.generator.victools;

import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@UtilityClass
public class CRDUtils {

  /**
   * Walks up the class hierarchy to consume the repeating annotation
   */
  private static <A extends Annotation> void consumeRepeatingAnnotation(
      Class<?> beanClass,
      Class<A> annotation,
      Consumer<A> consumer) {

    while (beanClass != null && beanClass != Object.class) {
      Stream.of(beanClass.getAnnotationsByType(annotation)).forEach(consumer);
      beanClass = beanClass.getSuperclass();
    }
  }

  public static <A extends Annotation> List<A> findRepeatingAnnotations(Class<?> clazz, Class<A> annotation) {
    var list = new LinkedList<A>();
    consumeRepeatingAnnotation(clazz, annotation, list::add);
    return list;
  }

  public static <T> Predicate<T> distinctByKey(
      Function<? super T, ?> keyExtractor) {

    Map<Object, Boolean> seen = new ConcurrentHashMap<>();
    return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
  }

  public static String emptyToNull(String value) {
    return ofNullable(value).filter(s -> !s.isEmpty()).orElse(null);
  }

  public static Integer zeroToNull(Integer value) {
    return ofNullable(value).filter(i -> i != 0).orElse(null);
  }

  public static Boolean falseToNull(boolean value) {
    return value ? Boolean.TRUE : null;
  }
}
