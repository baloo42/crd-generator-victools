package io.fabric8.crd.generator.victools.schema;

import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import io.fabric8.crd.generator.victools.CRDGeneratorContextInternal;
import io.fabric8.generator.annotation.Default;
import io.fabric8.generator.annotation.Max;
import io.fabric8.generator.annotation.Min;
import io.fabric8.generator.annotation.Nullable;
import io.fabric8.generator.annotation.Pattern;
import io.fabric8.generator.annotation.Required;

import java.math.BigDecimal;

import static io.fabric8.crd.generator.victools.schema.SchemaGeneratorUtils.findAnnotationConsideringFieldAndGetter;

/**
 * Module that enables injection of attributes from Fabric8 annotations.
 */
public class Fabric8Module extends AbstractCRDGeneratorModule {

  public Fabric8Module(CRDGeneratorContextInternal context) {
    super(context);
  }

  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.forFields().withDefaultResolver(this::resolveDefault);
    builder.forFields().withNumberInclusiveMinimumResolver(this::resolveNumberInclusiveMinimum);
    builder.forFields().withNumberInclusiveMaximumResolver(this::resolveNumberInclusiveMaximum);
    builder.forFields().withStringMaxLengthResolver(this::resolveStringMaxLength);
    builder.forFields().withStringMinLengthResolver(this::resolveStringMinLength);
    builder.forFields().withStringPatternResolver(this::resolvePattern);
    builder.forFields().withNullableCheck(this::checkNullable);
    builder.forFields().withRequiredCheck(this::checkRequired);

    builder.forMethods().withDefaultResolver(this::resolveDefault);
    builder.forMethods().withNumberInclusiveMinimumResolver(this::resolveNumberInclusiveMinimum);
    builder.forMethods().withNumberExclusiveMaximumResolver(this::resolveNumberInclusiveMaximum);
    builder.forMethods().withStringMaxLengthResolver(this::resolveStringMaxLength);
    builder.forMethods().withStringMinLengthResolver(this::resolveStringMinLength);
    builder.forMethods().withStringPatternResolver(this::resolvePattern);
    builder.forMethods().withNullableCheck(this::checkNullable);
    builder.forMethods().withRequiredCheck(this::checkRequired);
  }

  public Object resolveDefault(MemberScope<?, ?> member) {
    return findAnnotationConsideringFieldAndGetter(member, Default.class)
        .map(Default::value)
        .orElse(null);
  }

  private BigDecimal resolveNumberInclusiveMinimum(MemberScope<?, ?> member) {
    return findAnnotationConsideringFieldAndGetter(member, Min.class)
        .map(Min::value)
        .map(BigDecimal::valueOf)
        .orElse(null);
  }

  private BigDecimal resolveNumberInclusiveMaximum(MemberScope<?, ?> member) {
    return findAnnotationConsideringFieldAndGetter(member, Max.class)
        .map(Max::value)
        .map(BigDecimal::valueOf)
        .orElse(null);
  }

  public Integer resolveStringMaxLength(MemberScope<?, ?> member) {
    if(member.getType().getErasedType().equals(String.class)) {
      return findAnnotationConsideringFieldAndGetter(member, Max.class)
          .map(ann -> (int) ann.value())
          .orElse(null);
    }
    return null;
  }

  public Integer resolveStringMinLength(MemberScope<?, ?> member) {
    if(member.getType().getErasedType().equals(String.class)) {
      return findAnnotationConsideringFieldAndGetter(member, Min.class)
          .map(ann -> (int) ann.value())
          .orElse(null);
    }
    return null;
  }

  public String resolvePattern(MemberScope<?, ?> member) {
    return findAnnotationConsideringFieldAndGetter(member, Pattern.class)
        .map(Pattern::value)
        .orElse(null);
  }

  public Boolean checkNullable(MemberScope<?, ?> member) {
    return findAnnotationConsideringFieldAndGetter(member, Nullable.class).isPresent();
  }

  public Boolean checkRequired(MemberScope<?, ?> member) {
    return findAnnotationConsideringFieldAndGetter(member, Required.class).isPresent();
  }

}
