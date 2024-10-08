package io.fabric8.crd.generator.victools.schema;

import com.github.victools.jsonschema.generator.MemberScope;
import io.fabric8.generator.annotation.Default;
import io.fabric8.generator.annotation.Max;
import io.fabric8.generator.annotation.Min;
import io.fabric8.generator.annotation.Nullable;
import io.fabric8.generator.annotation.Pattern;
import io.fabric8.generator.annotation.Required;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotationOnFieldAndGetter;
import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotationOnFieldGetterContainerItem;

/**
 * Module for common validation constraints, declared by Fabric8 annotations.
 */
@Slf4j
public class Fabric8ValidationModule extends AbstractValidationModule {

  @Override
  protected Object resolveDefault(MemberScope<?, ?> member) {
    if (member.isFakeContainerItemScope()) {
      return null;
    }
    return findAnnotationOnFieldAndGetter(member, Default.class)
        .map(Default::value)
        .orElse(null);
  }

  @Override
  protected Boolean checkNullable(MemberScope<?, ?> member) {
    if (member.isFakeContainerItemScope()) {
      return false;
    }
    return findAnnotationOnFieldAndGetter(member, Nullable.class).isPresent();
  }

  @Override
  protected Boolean checkRequired(MemberScope<?, ?> member) {
    if (member.isFakeContainerItemScope()) {
      return false;
    }
    return findAnnotationOnFieldAndGetter(member, Required.class).isPresent();
  }

  @Override
  protected BigDecimal resolveNumberInclusiveMinimum(MemberScope<?, ?> member) {
    if (member.getType().getErasedType().equals(Character.class)) {
      return null;
    }
    return findAnnotationOnFieldGetterContainerItem(member, Min.class)
        .map(Min::value)
        .map(BigDecimal::valueOf)
        .orElse(null);
  }

  @Override
  protected BigDecimal resolveNumberExclusiveMinimum(MemberScope<?, ?> member) {
    // TODO: not yet supported
    return null;
  }

  @Override
  protected BigDecimal resolveNumberInclusiveMaximum(MemberScope<?, ?> member) {
    if (member.getType().getErasedType().equals(Character.class)) {
      return null;
    }
    return findAnnotationOnFieldGetterContainerItem(member, Max.class)
        .map(Max::value)
        .map(BigDecimal::valueOf)
        .orElse(null);
  }

  @Override
  protected BigDecimal resolveNumberExclusiveMaximum(MemberScope<?, ?> member) {
    // TODO: not yet supported
    return null;
  }

  @Override
  protected String resolvePattern(MemberScope<?, ?> member) {
    if (member.getType().isInstanceOf(CharSequence.class)) {
      return findAnnotationOnFieldGetterContainerItem(member, Pattern.class)
          .map(Pattern::value)
          .orElse(null);
    }
    return null;
  }

  @Override
  protected Integer resolveArrayMinItems(MemberScope<?, ?> member) {
    if (member.isContainerType()) {
      return findAnnotationOnFieldGetterContainerItem(member, Min.class)
          .map(Min::value)
          .map(Double::intValue)
          .filter(min -> min > 0)
          .orElse(null);
    }
    return null;
  }

  @Override
  protected Integer resolveArrayMaxItems(MemberScope<?, ?> member) {
    if (member.isContainerType()) {
      return findAnnotationOnFieldGetterContainerItem(member, Max.class)
          .map(Max::value)
          .filter(max -> max < Integer.MAX_VALUE)
          .map(Double::intValue)
          .orElse(null);
    }
    return null;
  }

  @Override
  protected Integer resolveStringMinLength(MemberScope<?, ?> member) {
    if (member.getType().isInstanceOf(CharSequence.class)) {
      return findAnnotationOnFieldGetterContainerItem(member, Min.class)
          .map(Min::value)
          .map(Double::intValue)
          .filter(min -> min > 0)
          .orElse(null);
    }
    return null;
  }

  @Override
  protected Integer resolveStringMaxLength(MemberScope<?, ?> member) {
    if (member.getType().isInstanceOf(CharSequence.class)) {
      return findAnnotationOnFieldGetterContainerItem(member, Max.class)
          .map(Max::value)
          .filter(max -> max < Integer.MAX_VALUE)
          .map(Double::intValue)
          .orElse(null);
    }
    return null;
  }

  @Override
  protected Integer resolveMapMinEntries(MemberScope<?, ?> member) {
    return findAnnotationOnFieldGetterContainerItem(member, Min.class)
        .map(Min::value)
        .map(Double::intValue)
        .filter(min -> min > 0)
        .orElse(null);
  }

  @Override
  protected Integer resolveMapMaxEntries(MemberScope<?, ?> member) {
    return findAnnotationOnFieldGetterContainerItem(member, Max.class)
        .map(Max::value)
        .filter(max -> max < Integer.MAX_VALUE)
        .map(Double::intValue)
        .orElse(null);
  }

}
