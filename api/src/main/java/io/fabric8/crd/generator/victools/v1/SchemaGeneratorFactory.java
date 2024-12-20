package io.fabric8.crd.generator.victools.v1;

import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationModule;
import com.github.victools.jsonschema.module.swagger2.Swagger2Module;
import io.fabric8.crd.generator.victools.AbstractSchemaGeneratorFactory;
import io.fabric8.crd.generator.victools.CRDGeneratorContextInternal;
import io.fabric8.crd.generator.victools.CRDGeneratorSchemaOption;
import io.fabric8.crd.generator.victools.CustomResourceContext;
import io.fabric8.crd.generator.victools.schema.ConstToEnumInAllOfModule;
import io.fabric8.crd.generator.victools.schema.DependentClassesModule;
import io.fabric8.crd.generator.victools.schema.EmbeddedResourceModule;
import io.fabric8.crd.generator.victools.schema.ExplicitNullableModule;
import io.fabric8.crd.generator.victools.schema.ExternalDocsModule;
import io.fabric8.crd.generator.victools.schema.ImplicitMapModule;
import io.fabric8.crd.generator.victools.schema.IntOrStringModule;
import io.fabric8.crd.generator.victools.schema.JacksonEnumModule;
import io.fabric8.crd.generator.victools.schema.KubernetesListTypeModule;
import io.fabric8.crd.generator.victools.schema.KubernetesMapTypeModule;
import io.fabric8.crd.generator.victools.schema.KubernetesValidationRuleProvider;
import io.fabric8.crd.generator.victools.schema.MetadataModule;
import io.fabric8.crd.generator.victools.schema.PrinterColumnProvider;
import io.fabric8.crd.generator.victools.schema.ScaleSubresourceProvider;
import io.fabric8.crd.generator.victools.schema.SelectableFieldProvider;
import io.fabric8.crd.generator.victools.schema.ValidationModule;
import io.fabric8.crd.generator.victools.schema.fkc.FkcKubernetesValidationRuleProvider;
import io.fabric8.crd.generator.victools.schema.fkc.FkcPreserveUnknownFieldsModule;
import io.fabric8.crd.generator.victools.schema.fkc.FkcPrinterColumnProvider;
import io.fabric8.crd.generator.victools.schema.fkc.FkcScaleSubresourceProvider;
import io.fabric8.crd.generator.victools.schema.fkc.FkcSchemaFromModule;
import io.fabric8.crd.generator.victools.schema.fkc.FkcValidationModule;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;

@Slf4j
class SchemaGeneratorFactory extends AbstractSchemaGeneratorFactory {

  private static final OptionPreset DEFAULT_OPTIONS = new OptionPreset(
      Option.INLINE_ALL_SCHEMAS,
      Option.ADDITIONAL_FIXED_TYPES,
      Option.STANDARD_FORMATS,
      Option.FLATTENED_OPTIONALS,
      Option.FLATTENED_SUPPLIERS,
      Option.ENUM_KEYWORD_FOR_SINGLE_VALUES,
      Option.MAP_VALUES_AS_ADDITIONAL_PROPERTIES,
      Option.PUBLIC_NONSTATIC_FIELDS,
      Option.GETTER_METHODS,
      Option.FIELDS_DERIVED_FROM_ARGUMENTFREE_METHODS,
      Option.NONPUBLIC_NONSTATIC_FIELDS_WITH_GETTERS,
      Option.NONPUBLIC_NONSTATIC_FIELDS_WITHOUT_GETTERS,
      Option.ALLOF_CLEANUP_AT_THE_END,
      Option.DUPLICATE_MEMBER_ATTRIBUTE_CLEANUP_AT_THE_END);

  @Override
  public OptionPreset getDefaultOptionPreset() {
    return DEFAULT_OPTIONS;
  }

  @Override
  public void configureSchemaGenerator(
      CRDGeneratorContextInternal context,
      CustomResourceContext customResourceContext,
      SchemaGeneratorConfigBuilder builder) {

    builder.with(new JacksonModule(
        JacksonOption.FLATTENED_ENUMS_FROM_JSONPROPERTY,
        JacksonOption.RESPECT_JSONPROPERTY_ORDER));

    if (context.isEnabled(CRDGeneratorSchemaOption.SERVICE_LOADER)) {
      getModules()
          .forEach(module -> {
            module.forFields(context, builder.forFields());
            module.forMethods(context, builder.forMethods());
            module.forTypesInGeneral(context, builder.forTypesInGeneral());
          });
    }

    if (context.isEnabled(CRDGeneratorSchemaOption.JAKARTA_VALIDATION)) {
      // fails intentional to load if required dependencies are not in classpath
      // could be improved to fail with a better error message
      builder.with(new JakartaValidationModule());
    }

    if (context.isEnabled(CRDGeneratorSchemaOption.SWAGGER_2)) {
      // fails intentional to load if required dependencies are not in classpath
      // could be improved to fail with a better error message
      builder.with(new Swagger2Module());
    }

    var metadataProvider = new LinkedList<MetadataModule.MetadataProvider>();

    if (context.isEnabled(CRDGeneratorSchemaOption.FKC_ANNOTATIONS)) {
      metadataProvider.add(new FkcScaleSubresourceProvider());
      metadataProvider.add(new FkcPrinterColumnProvider());
      // TODO: add FkcSelectableFieldProvider once updated to fabric8/kubernetes-client v7
      // metadataProvider.add(new FkcSelectableFieldProvider());
      metadataProvider.add(new FkcKubernetesValidationRuleProvider());

      builder
          .with(new FkcSchemaFromModule())
          .with(new FkcValidationModule())
          .with(new FkcPreserveUnknownFieldsModule(context));
    }

    if (context.isEnabled(CRDGeneratorSchemaOption.OWN_ANNOTATIONS)) {
      metadataProvider.add(new ScaleSubresourceProvider());
      metadataProvider.add(new PrinterColumnProvider());
      metadataProvider.add(new SelectableFieldProvider());
      metadataProvider.add(new KubernetesValidationRuleProvider());

      builder
          .with(new ValidationModule())
          .with(new ExternalDocsModule())
          .with(new KubernetesMapTypeModule())
          .with(new KubernetesListTypeModule())
          .with(new EmbeddedResourceModule());
    }

    builder
        .with(new JacksonEnumModule())
        .with(new ImplicitMapModule())
        .with(new IntOrStringModule())
        .with(new ExplicitNullableModule())
        .with(new ConstToEnumInAllOfModule())
        .with(new DependentClassesModule(customResourceContext))
        .with(new MetadataModule(customResourceContext, metadataProvider));
  }
}
