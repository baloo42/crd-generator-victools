package io.fabric8.crd.generator.victools;

import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

@UtilityClass
public class AnnotationUtils {

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
}
