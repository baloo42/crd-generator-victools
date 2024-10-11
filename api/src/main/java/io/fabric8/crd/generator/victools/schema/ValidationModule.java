package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victools.jsonschema.generator.MemberScope;
import io.fabric8.crd.generator.victools.annotation.Default;
import io.fabric8.crd.generator.victools.annotation.Max;
import io.fabric8.crd.generator.victools.annotation.Min;
import io.fabric8.crd.generator.victools.annotation.Nullable;
import io.fabric8.crd.generator.victools.annotation.Pattern;
import io.fabric8.crd.generator.victools.annotation.Required;
import io.fabric8.crd.generator.victools.annotation.Size;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotationOnFieldAndGetter;
import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotationOnFieldGetterContainerItem;
import static java.util.function.Predicate.not;

/**
 * Module for common validation constraints, declared by our own annotations.
 */
@Slf4j
public class ValidationModule extends AbstractValidationModule {

  @Override
  protected Object resolveDefault(MemberScope<?, ?> member, ObjectMapper objectMapper) {
    if (member.isFakeContainerItemScope()) {
      return null;
    }
    return findAnnotationOnFieldAndGetter(member, Default.class)
        .map(Default::value)
        .map(s -> objectMapper.convertValue(s, JsonNode.class))
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
    if (!member.getType().isInstanceOf(CharSequence.class)) {
      var res = findAnnotationOnFieldGetterContainerItem(member, Min.class)
          .filter(Min::inclusive)
          .map(Min::value)
          .map(BigDecimal::valueOf)
          .orElse(null);

      return res;
    }
    return null;
  }

  @Override
  protected BigDecimal resolveNumberExclusiveMinimum(MemberScope<?, ?> member) {
    if (!member.getType().isInstanceOf(CharSequence.class)) {
      return findAnnotationOnFieldGetterContainerItem(member, Min.class)
          .filter(not(Min::inclusive))
          .map(Min::value)
          .map(BigDecimal::valueOf)
          .orElse(null);
    }
    return null;
  }

  @Override
  protected BigDecimal resolveNumberInclusiveMaximum(MemberScope<?, ?> member) {
    if (!member.getType().isInstanceOf(CharSequence.class)) {
      return findAnnotationOnFieldGetterContainerItem(member, Max.class)
          .filter(Max::inclusive)
          .map(Max::value)
          .map(BigDecimal::valueOf)
          .orElse(null);
    }
    return null;
  }

  @Override
  protected BigDecimal resolveNumberExclusiveMaximum(MemberScope<?, ?> member) {
    if (!member.getType().isInstanceOf(CharSequence.class)) {
      return findAnnotationOnFieldGetterContainerItem(member, Max.class)
          .filter(not(Max::inclusive))
          .map(Max::value)
          .map(BigDecimal::valueOf)
          .orElse(null);
    }
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
      return findAnnotationOnFieldGetterContainerItem(member, Size.class)
          .map(Size::min)
          .filter(min -> min > 0)
          .map(Math::toIntExact)
          .orElse(null);
    }
    return null;
  }

  @Override
  protected Integer resolveArrayMaxItems(MemberScope<?, ?> member) {
    if (member.isContainerType()) {
      return findAnnotationOnFieldGetterContainerItem(member, Size.class)
          .map(Size::max)
          .filter(max -> max < Integer.MAX_VALUE)
          .map(Math::toIntExact)
          .orElse(null);
    }
    return null;
  }

  @Override
  protected Integer resolveStringMinLength(MemberScope<?, ?> member) {
    if (member.getType().isInstanceOf(CharSequence.class)) {
      return findAnnotationOnFieldGetterContainerItem(member, Size.class)
          .map(Size::min)
          .filter(min -> min > 0)
          .map(Math::toIntExact)
          .orElse(null);
    }
    return null;
  }

  @Override
  protected Integer resolveStringMaxLength(MemberScope<?, ?> member) {
    if (member.getType().isInstanceOf(CharSequence.class)) {
      return findAnnotationOnFieldGetterContainerItem(member, Size.class)
          .map(Size::max)
          .filter(max -> max < Integer.MAX_VALUE)
          .map(Math::toIntExact)
          .orElse(null);
    }
    return null;
  }

  @Override
  protected Integer resolveMapMinEntries(MemberScope<?, ?> member) {
    return findAnnotationOnFieldGetterContainerItem(member, Size.class)
        .map(Size::min)
        .filter(min -> min > 0)
        .map(Math::toIntExact)
        .orElse(null);
  }

  @Override
  protected Integer resolveMapMaxEntries(MemberScope<?, ?> member) {
    return findAnnotationOnFieldGetterContainerItem(member, Size.class)
        .map(Size::max)
        .filter(max -> max < Integer.MAX_VALUE)
        .map(Math::toIntExact)
        .orElse(null);
  }

}
