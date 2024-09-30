package io.fabric8.crd.generator.victools.v1;

import io.fabric8.crd.generator.victools.AbstractCRDVersionHandler;
import io.fabric8.crd.generator.victools.CRDGeneratorContextInternal;
import lombok.Getter;

@Getter
public class CRDv1Handler extends AbstractCRDVersionHandler {

  public CRDv1Handler(CRDGeneratorContextInternal context) {
    super(context, new SchemaGeneratorFactory(), new CustomResourceHandler());
  }

}
