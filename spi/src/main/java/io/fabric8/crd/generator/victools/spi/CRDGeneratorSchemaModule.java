package io.fabric8.crd.generator.victools.spi;

import com.github.victools.jsonschema.generator.FieldScope;
import com.github.victools.jsonschema.generator.MethodScope;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigPart;
import com.github.victools.jsonschema.generator.SchemaGeneratorGeneralConfigPart;

import java.util.Set;

public interface CRDGeneratorSchemaModule {

  /**
   * Returns the CRD Versions, this module should be used on.
   *
   * @return the supported CRD Versions
   */
  default Set<CRDVersion> getSupportedCRDVersions() {
    return Set.of(CRDVersion.V1);
  }

  /**
   * Configure the schema generator for fields.
   *
   * @param context the context of the CRD-Generator.
   * @param builder the schema config part builder for fields.
   */
  default void forFields(
      CRDGeneratorContext context,
      SchemaGeneratorConfigPart<FieldScope> builder) {
    // no-op
  }

  /**
   * Configure the schema generator for methods.
   *
   * @param context the context of the CRD-Generator.
   * @param builder the schema config part builder for methods.
   */
  default void forMethods(
      CRDGeneratorContext context,
      SchemaGeneratorConfigPart<MethodScope> builder) {
    // no-op
  }

  /**
   * Configure the schema generator for types in general.
   *
   * @param context the context of the CRD-Generator.
   * @param builder the schema config part builder for types in general.
   */
  default void forTypesInGeneral(
      CRDGeneratorContext context,
      SchemaGeneratorGeneralConfigPart builder) {
    // no-op
  }

}
