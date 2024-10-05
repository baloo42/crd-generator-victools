package io.fabric8.crd.generator.victools;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.crd.generator.victools.spi.CRDVersion;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.utils.KubernetesSerialization;

import java.io.File;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The API for the CRD-Generator.
 * Methods in this interface can be considered as stable.
 */
public interface CRDGeneratorApi {
  CRDGenerator withOutputDirectory(File outputDir);

  CRDGenerator withOutput(CRDOutput<? extends OutputStream> output);

  CRDGenerator withFilenameFormat(String filenameFormat);

  CRDGenerator withImplicitPreserveUnknownFields(boolean implicitPreserveUnknownFields);

  CRDGenerator withParallelGenerationEnabled(boolean parallel);

  CRDGenerator withHeader(String header);

  CRDGenerator withAnnotations(Map<String, String> annotations);

  CRDGenerator withLabels(Map<String, String> labels);

  CRDGenerator withObjectMapper(ObjectMapper mapper, KubernetesSerialization kubernetesSerialization);

  CRDGenerator forCRDVersions(List<String> versions);

  CRDGenerator forCRDVersions(String... versions);

  CRDGenerator forCRDVersions(CRDVersion... versions);

  @SuppressWarnings("unchecked")
  CRDGenerator customResourceClasses(Class<? extends HasMetadata>... crClasses);

  CRDGenerator customResourceClasses(Collection<Class<? extends HasMetadata>> crClasses);

  CRDGenerator customResources(Collection<CustomResourceInfo> infos);

  CRDGenerator customResources(CustomResourceInfo... infos);

  int generate();

  CRDGenerationInfo detailedGenerate();
}
