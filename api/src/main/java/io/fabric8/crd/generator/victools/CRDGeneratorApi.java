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
  CRDGeneratorApi withOutputDirectory(File outputDir);

  CRDGeneratorApi withOutput(CRDOutput<? extends OutputStream> output);

  CRDGeneratorApi withFilenameFormat(String filenameFormat);

  CRDGeneratorApi withImplicitPreserveUnknownFields(boolean implicitPreserveUnknownFields);

  CRDGeneratorApi withParallelGenerationEnabled(boolean parallel);

  CRDGeneratorApi withHeader(String header);

  CRDGeneratorApi withAnnotations(Map<String, String> annotations);

  CRDGeneratorApi withLabels(Map<String, String> labels);

  CRDGeneratorApi withObjectMapper(ObjectMapper mapper, KubernetesSerialization kubernetesSerialization);

  CRDGeneratorApi forCRDVersions(List<String> versions);

  CRDGeneratorApi forCRDVersions(String... versions);

  CRDGeneratorApi forCRDVersions(CRDVersion... versions);

  @SuppressWarnings("unchecked")
  CRDGeneratorApi customResourceClasses(Class<? extends HasMetadata>... crClasses);

  CRDGeneratorApi customResourceClasses(Collection<Class<? extends HasMetadata>> crClasses);

  CRDGeneratorApi customResources(Collection<CustomResourceInfo> infos);

  CRDGeneratorApi customResources(CustomResourceInfo... infos);

  int generate();

  CRDGenerationInfo detailedGenerate();
}
