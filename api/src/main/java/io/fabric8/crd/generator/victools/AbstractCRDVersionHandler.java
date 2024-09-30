package io.fabric8.crd.generator.victools;

import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public abstract class AbstractCRDVersionHandler {

  private final CRDGeneratorContextInternal crdGeneratorContext;
  private final AbstractCustomResourceHandler customResourceHandler;
  private final AbstractSchemaGeneratorFactory schemaGeneratorFactory;

  protected AbstractCRDVersionHandler(
      CRDGeneratorContextInternal crdGeneratorContext,
      AbstractSchemaGeneratorFactory schemaGeneratorFactory,
      AbstractCustomResourceHandler customResourceHandler) {
    this.crdGeneratorContext = crdGeneratorContext;
    this.schemaGeneratorFactory = schemaGeneratorFactory;
    this.customResourceHandler = customResourceHandler;
  }

  public void handle(CustomResourceInfo crInfo) {
    var customResourceContext = new CustomResourceContext();
    var schemaGenerator = schemaGeneratorFactory
        .createSchemaGenerator(crdGeneratorContext, customResourceContext);
    customResourceHandler.handle(crdGeneratorContext, crInfo, schemaGenerator, customResourceContext);
  }

  public Stream<Map.Entry<? extends HasMetadata, Set<String>>> finish() {
    return customResourceHandler.finish();
  }

}
