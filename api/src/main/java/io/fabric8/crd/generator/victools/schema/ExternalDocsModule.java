package io.fabric8.crd.generator.victools.schema;

import io.fabric8.crd.generator.victools.annotation.ExternalDocs;

import static io.fabric8.crd.generator.victools.CRDUtils.emptyToNull;

public class ExternalDocsModule extends AbstractExternalDocsModule<ExternalDocs> {

  public ExternalDocsModule() {
    super(ExternalDocs.class, ExternalDocsModule::from);
  }

  private static AbstractExternalDocsModule.ExternalDocsInfo from(ExternalDocs annotation) {
    return new ExternalDocsInfo(
      emptyToNull(annotation.description()),
      emptyToNull(annotation.url()));
  }
}
