package io.fabric8.crd.generator.victools;

import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.generator.impl.PropertySortUtils;
import io.fabric8.crd.generator.victools.spi.CRDGeneratorSchemaModule;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import static java.util.Optional.ofNullable;

public abstract class AbstractSchemaGeneratorFactory {

  protected abstract OptionPreset getDefaultOptionPreset();
  private List<CRDGeneratorSchemaModule> modules;

  protected List<CRDGeneratorSchemaModule> getModules() {
    return ofNullable(modules).orElseGet(() -> this.modules = loadModules());
  }

  protected abstract void configureSchemaGenerator(
      CRDGeneratorContextInternal crdGeneratorContext,
      CustomResourceContext customResourceContext,
      SchemaGeneratorConfigBuilder builder);

  public SchemaGenerator createSchemaGenerator(
      CRDGeneratorContextInternal crdGeneratorContext,
      CustomResourceContext customResourceContext) {

    var builder = new SchemaGeneratorConfigBuilder(
        crdGeneratorContext.getObjectMapper(), SchemaVersion.DRAFT_6, getDefaultOptionPreset());

    configureSchemaGenerator(crdGeneratorContext, customResourceContext, builder);

    builder.forTypesInGeneral()
        .withPropertySorter(PropertySortUtils.SORT_PROPERTIES_BY_NAME_ALPHABETICALLY);
    return new SchemaGenerator(builder.build());
  }

  private static List<CRDGeneratorSchemaModule> loadModules() {
    List<CRDGeneratorSchemaModule> modules = new LinkedList<>();
    ServiceLoader<CRDGeneratorSchemaModule> loader = ServiceLoader.load(CRDGeneratorSchemaModule.class);
    loader.forEach(modules::add);
    return Collections.unmodifiableList(modules);
  }

}
