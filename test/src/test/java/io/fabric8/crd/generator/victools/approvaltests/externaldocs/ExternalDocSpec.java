package io.fabric8.crd.generator.victools.approvaltests.externaldocs;

import io.fabric8.crd.generator.victools.annotation.ExternalDocs;
import lombok.Data;

@Data
public class ExternalDocSpec {

  @ExternalDocs(url = "https://docs.example.com/message")
  private String url;

  @ExternalDocs(description = "A description")
  private String description;

  @ExternalDocs(url = "https://docs.example.com/message", description = "A description")
  private String urlAndDescription;

  @ExternalDocs
  private String noExternalDocs;

  private ClassWithExternalDocs classWithExternalDocs;

  @ExternalDocs(description = "A description on a class")
  static class ClassWithExternalDocs{
  }

}
