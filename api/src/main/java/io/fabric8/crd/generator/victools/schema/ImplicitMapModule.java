package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.ConfigFunction;
import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.TypeScope;
import com.github.victools.jsonschema.generator.impl.module.AdditionalPropertiesModule;
import io.fabric8.crd.generator.victools.CRDGeneratorContextInternal;
import io.fabric8.crd.generator.victools.spi.CRDGeneratorContext;

import java.lang.reflect.Type;
import java.util.Map;


/**
 * Module to support Maps with a value type of Object or undefined.
 * <p>
 * By default, the value type of a map is reflected in the resulting schema only if the value type
 * is defined and not of type Object. For example:
 * <pre>
 * Map&lt;String, String&gt; myMap;
 * </pre>
 * This module extends the default behaviour and defines the value type for the following cases:
 * </p>
 * <pre>
 * Map&lt;String, Object&gt; example1;
 * Map example2;
 * </pre>
 * Resulting schema:
 * <pre>
 * example1:
 *   additionalProperties:
 *     type: "object"
 *   type: "object"
 * example2:
 *   additionalProperties:
 *     type: "object"
 *   type: "object"
 * </pre>
 */
public class ImplicitMapModule extends AbstractCRDGeneratorModule {

  private final AdditionalPropertiesModule additionalPropertiesModule;

  private static final ConfigFunction<TypeScope, Type> RESOLVER = scope -> {
    if (scope.getType().isInstanceOf(Map.class)) {
      return Object.class;
    }

    return null;
  };

  public ImplicitMapModule(CRDGeneratorContextInternal context) {
    super(context);
    additionalPropertiesModule = new AdditionalPropertiesModule(
        RESOLVER, this::createDefinitionForMemberMap, this::createDefinitionForMemberMap);
  }

  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    additionalPropertiesModule.applyToConfigBuilder(builder);
  }

  private JsonNode createDefinitionForMemberMap(MemberScope<?, ?> member,
      SchemaGenerationContext context) {
    if (!member.getType().isInstanceOf(Map.class)) {
      return null;
    }

    ResolvedType valueType = member.getTypeParameterFor(Map.class, 1);
    if (valueType == null || valueType.getErasedType() == Object.class) {
      return member.getContext()
          .performActionOnMember(member.asFakeContainerItemScope(),
              field -> context.createDefinition(member.getContext().resolve(Map.class)),
              method -> context.createDefinition(member.getContext().resolve(Map.class)));
    }

    return null;
  }
}
