# Fabric8 CRD-Generator (victools/json-schema)

Generate Custom Resource Definitions (CRD) for Kubernetes from Java.

Despite the CRD-Generator from fabric8/kubernetes-client, this implementation uses 
victools/json-schema-generator instead of jackson/json-schema-generator.


## Modules

- `annotations`  
  Additional annotations.
- `spi`  
  Contains interfaces to extend the CRD-Generator.
- `api`  
  Implementation and main interface.
- `collector`  
  Allows to search for custom resource classes.
  Backported from fabric8/kubernetes-client. Could be replaced with the one from fabric8/kubernetes-client once v7 is released.
- `cli`  
  Command line interface using this implementation.
- `maven-plugin`  
  Maven plugin using this implementation.
- `compliance-test`  
  Tests to compare different implementations. (same as upstream)
- `test`  
  Tests for features which are not (yet) supported in upstream.


## Differences

_compared to fabric8/kubernetes-client CRD-Generator v2_

- No support for `@SchemaSwap`/`@SchemaFrom`
- Support for `@AdditionalPrinterColumn`
- Support for `@MapType`
- Support for `@ExternalDocs`
- Support for Jackson including `anyOf`/`@JsonSubTypes`.
- Support for Jakarta Validations Annotations (not yet fully tested)
- Support for Swagger 2 Annotations (not yet fully tested)
- Support for adding annotations and labels to resulting CRD (e.g. build-timestamp from maven var)

## SPI

This CRD-Generator implementation can be extended with _CRD-Generator Schema Modules_.
A module must implement the `CRDGeneratorSchemaModule` interface which allows to hook into victools
schema-generator configuration. Victools modules can't be used directly because they allow too much to modify.

The CRD-Generator loads modules via ServiceLoader to implement autoconfiguration.
This requires modules to include a file `META-INF/services/io.fabric8.crd.generator.victools.spi.CRDGeneratorSchemaModule`
with the fully qualified class name of the module implementation.
