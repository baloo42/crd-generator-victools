package io.fabric8.crd.generator.victools.schema;

import com.fasterxml.classmate.ResolvedType;
import com.github.victools.jsonschema.generator.CustomDefinition;
import com.github.victools.jsonschema.generator.CustomDefinitionProviderV2;
import com.github.victools.jsonschema.generator.CustomPropertyDefinition;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaKeyword;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Quantity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

import static io.fabric8.crd.generator.victools.spi.KubernetesSchemaKeyword.KUBERNETES_INT_OR_STRING;

public class IntOrStringModule implements Module {

  private static final Set<Class<?>> IMPLICIT_CLASSES = Set.of(
      IntOrString.class,
      Quantity.class);

  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.forTypesInGeneral().withCustomDefinitionProvider(
        new ImplicitPreserveUnknownFieldsCustomDefinitionProvider(IMPLICIT_CLASSES));
  }

  @Slf4j
  @RequiredArgsConstructor
  private static class ImplicitPreserveUnknownFieldsCustomDefinitionProvider
      implements CustomDefinitionProviderV2 {

    private final Set<Class<?>> classes;

    @Override
    public CustomDefinition provideCustomSchemaDefinition(
        ResolvedType javaType,
        SchemaGenerationContext context) {

      if (!classes.contains(javaType.getErasedType())) {
        return null;
      }

      var objectNode = context.getGeneratorConfig().createObjectNode();
      objectNode.put(KUBERNETES_INT_OR_STRING.getValue(), true);

      var stringSchema = context.createDefinition(context.getTypeContext().resolve(String.class));
      var intSchema = context.createDefinition(context.getTypeContext().resolve(Integer.class));

      objectNode.set(context.getKeyword(SchemaKeyword.TAG_ANYOF),
          context.getGeneratorConfig().createArrayNode()
              .add(stringSchema)
              .add(intSchema));
      return new CustomPropertyDefinition(objectNode);
    }
  }

}
