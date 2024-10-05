package io.fabric8.crd.generator.victools.schema;

import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import io.fabric8.generator.annotation.Default;
import io.fabric8.generator.annotation.Max;
import io.fabric8.generator.annotation.Min;
import io.fabric8.generator.annotation.Nullable;
import io.fabric8.generator.annotation.Pattern;
import io.fabric8.generator.annotation.Required;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotationConsideringFieldAndGetter;
import static java.util.Optional.ofNullable;

/**
 * Module that enables injection of attributes from Fabric8 annotations.
 */
@Slf4j
public class Fabric8Module implements Module {

  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.forFields().withDefaultResolver(this::resolveDefault);
    builder.forFields().withNumberInclusiveMinimumResolver(this::resolveNumberInclusiveMinimum);
    builder.forFields().withNumberInclusiveMaximumResolver(this::resolveNumberExclusiveMaximum);
    builder.forFields().withStringMaxLengthResolver(this::resolveStringMaxLength);
    builder.forFields().withStringMinLengthResolver(this::resolveStringMinLength);
    builder.forFields().withStringPatternResolver(this::resolvePattern);
    builder.forFields().withNullableCheck(this::checkNullable);
    builder.forFields().withRequiredCheck(this::checkRequired);

    builder.forMethods().withDefaultResolver(this::resolveDefault);
    builder.forMethods().withNumberInclusiveMinimumResolver(this::resolveNumberInclusiveMinimum);
    builder.forMethods().withNumberExclusiveMaximumResolver(this::resolveNumberExclusiveMaximum);
    builder.forMethods().withStringMaxLengthResolver(this::resolveStringMaxLength);
    builder.forMethods().withStringMinLengthResolver(this::resolveStringMinLength);
    builder.forMethods().withStringPatternResolver(this::resolvePattern);
    builder.forMethods().withNullableCheck(this::checkNullable);
    builder.forMethods().withRequiredCheck(this::checkRequired);
  }

  public Object resolveDefault(MemberScope<?, ?> member) {
    if (member.isFakeContainerItemScope()) {
      return null;
    }
    return findAnnotationConsideringFieldAndGetter(member, Default.class)
        .map(Default::value)
        .orElse(null);
  }

  private BigDecimal resolveNumberInclusiveMinimum(MemberScope<?, ?> member) {
    if (member.getType().getErasedType().equals(String.class)) {
      return null;
    }
    if (member.isFakeContainerItemScope()) {
      return ofNullable(member.getContainerItemAnnotation(io.fabric8.crd.generator.victools.annotation.Min.class))
          .map(io.fabric8.crd.generator.victools.annotation.Min::value)
          .map(BigDecimal::valueOf)
          .orElse(null);
    }

    return findAnnotationConsideringFieldAndGetter(member, Min.class)
        .map(Min::value)
        .map(BigDecimal::valueOf)
        .orElse(null);
  }

  private BigDecimal resolveNumberExclusiveMaximum(MemberScope<?, ?> member) {
    if (member.getType().getErasedType().equals(String.class)) {
      return null;
    }
    if (member.isFakeContainerItemScope()) {
      return ofNullable(member.getContainerItemAnnotation(io.fabric8.crd.generator.victools.annotation.Max.class))
          .map(io.fabric8.crd.generator.victools.annotation.Max::value)
          .map(BigDecimal::valueOf)
          .orElse(null);
    }

    return findAnnotationConsideringFieldAndGetter(member, Max.class)
        .map(Max::value)
        .map(BigDecimal::valueOf)
        .orElse(null);
  }

  public Integer resolveStringMaxLength(MemberScope<?, ?> member) {
    if (!member.getType().getErasedType().equals(String.class)) {
      return null;
    }
    if (member.isFakeContainerItemScope()) {
      return ofNullable(member.getContainerItemAnnotation(io.fabric8.crd.generator.victools.annotation.Max.class))
          .map(io.fabric8.crd.generator.victools.annotation.Max::value)
          .map(Double::intValue)
          .orElse(null);
    }

    return findAnnotationConsideringFieldAndGetter(member, Max.class)
        .map(ann -> (int) ann.value())
        .orElse(null);
  }

  public Integer resolveStringMinLength(MemberScope<?, ?> member) {
    if (!member.getType().getErasedType().equals(String.class)) {
      return null;
    }
    if (member.isFakeContainerItemScope()) {
      return ofNullable(member.getContainerItemAnnotation(io.fabric8.crd.generator.victools.annotation.Min.class))
          .map(io.fabric8.crd.generator.victools.annotation.Min::value)
          .map(Double::intValue)
          .orElse(null);
    }

    return findAnnotationConsideringFieldAndGetter(member, Min.class)
        .map(ann -> (int) ann.value())
        .orElse(null);
  }

  public String resolvePattern(MemberScope<?, ?> member) {
    if (!member.getType().getErasedType().equals(String.class)) {
      return null;
    }
    if (member.isFakeContainerItemScope()) {
      return ofNullable(member.getContainerItemAnnotation(io.fabric8.crd.generator.victools.annotation.Pattern.class))
          .map(io.fabric8.crd.generator.victools.annotation.Pattern::value)
          .orElse(null);
    }

    return findAnnotationConsideringFieldAndGetter(member, Pattern.class)
        .map(Pattern::value)
        .orElse(null);
  }

  public Boolean checkNullable(MemberScope<?, ?> member) {
    if (member.isFakeContainerItemScope()) {
      return null;
    }
    return findAnnotationConsideringFieldAndGetter(member, Nullable.class).isPresent();
  }

  public Boolean checkRequired(MemberScope<?, ?> member) {
    if (member.isFakeContainerItemScope()) {
      return null;
    }
    return findAnnotationConsideringFieldAndGetter(member, Required.class).isPresent();
  }

}
