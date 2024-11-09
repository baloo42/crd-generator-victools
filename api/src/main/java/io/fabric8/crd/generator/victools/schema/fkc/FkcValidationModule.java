package io.fabric8.crd.generator.victools.schema.fkc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victools.jsonschema.generator.MemberScope;
import io.fabric8.crd.generator.victools.schema.AbstractValidationModule;
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
public class FkcValidationModule extends AbstractValidationModule {

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
      return findAnnotationOnFieldGetterContainerItem(member, Min.class)
          .map(Min::value)
          .map(BigDecimal::valueOf)
          .orElse(null);
    }
    return null;
  }

  @Override
  protected BigDecimal resolveNumberExclusiveMinimum(MemberScope<?, ?> member) {
    // TODO: not yet supported
    return null;
  }

  @Override
  protected BigDecimal resolveNumberInclusiveMaximum(MemberScope<?, ?> member) {
    if (!member.getType().isInstanceOf(CharSequence.class)) {
      return findAnnotationOnFieldGetterContainerItem(member, Max.class)
          .map(Max::value)
          .map(BigDecimal::valueOf)
          .orElse(null);
    }
    return null;
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
      // TODO: not yet supported
    }
    return null;
  }

  @Override
  protected Integer resolveArrayMaxItems(MemberScope<?, ?> member) {
    if (member.isContainerType()) {
      // TODO: not yet supported
    }
    return null;
  }

  @Override
  protected Integer resolveStringMinLength(MemberScope<?, ?> member) {
    if (member.getType().isInstanceOf(CharSequence.class)) {
      // TODO: not yet supported
    }
    return null;
  }

  @Override
  protected Integer resolveStringMaxLength(MemberScope<?, ?> member) {
    if (member.getType().isInstanceOf(CharSequence.class)) {
      // TODO: not yet supported
    }
    return null;
  }

  @Override
  protected Integer resolveMapMinEntries(MemberScope<?, ?> member) {
    // TODO: not yet supported
    return null;
  }

  @Override
  protected Integer resolveMapMaxEntries(MemberScope<?, ?> member) {
    // TODO: not yet supported
    return null;
  }

}
