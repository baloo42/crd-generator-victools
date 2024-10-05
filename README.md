# Fabric8 CRD-Generator (victools/json-schema)

Generate Custom Resource Definitions (CRD) for Kubernetes from Java.

Despite the [CRD-Generator](https://github.com/fabric8io/kubernetes-client/blob/main/doc/CRD-generator.md)
from [fabric8/kubernetes-client](https://github.com/fabric8io/kubernetes-client), this implementation uses
[victools/jsonschema-generator](https://github.com/victools/jsonschema-generator) instead
of [jackson/jsonSchema](https://github.com/FasterXML/jackson-module-jsonSchema).

## Modules

- `annotations`  
  Additional annotations.
- `spi`  
  Contains interfaces to extend the CRD-Generator.
- `api`  
  Implementation and main interface.
- `collector`  
  Allows to search for custom resource classes.
  Backported from fabric8/kubernetes-client. Can be replaced with the implementation from fabric8/kubernetes-client once v7 is
  released.
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

- No support for `@SchemaSwap`
- Support for `@AdditionalPrinterColumn`
- Support for `@MapType`
- Support for `@ExternalDocs`
- Support for Jackson including `anyOf`/`@JsonSubTypes`.
- Support for Jakarta Validations Annotations (not yet fully tested)
- Support for Swagger 2 Annotations (not yet fully tested)
- Support for adding annotations and labels to resulting CRD (e.g. build-timestamp from maven var)
- Support for [emitting JSON-Schemas](#json-schemas)

## Features

### JSON-Schemas

JSON-Schemas can be emitted in addition to the regular CRDs.
Those schemas can be used by various tools such as
[Datree](https://github.com/datreeio/datree), [Kubeconform](https://github.com/yannh/kubeconform)
and [Kubeval](https://github.com/instrumenta/kubeval), as an alternative to `kubectl --dry-run`, to perform validation
on custom (and native) Kubernetes resources.

Running Kubernetes schema validation checks helps to apply the **"shift-left approach"** on machines **without** giving
them access to your cluster (e.g. locally or on CI).

### SPI

This CRD-Generator implementation can be extended with _CRD-Generator Schema Modules_.
A module must implement the `CRDGeneratorSchemaModule` interface which allows to hook into victools
schema-generator configuration. Victools modules can't be used directly because they allow too much to modify.

The CRD-Generator loads modules via ServiceLoader to implement autoconfiguration.
This requires modules to include a file
`META-INF/services/io.fabric8.crd.generator.victools.spi.CRDGeneratorSchemaModule`
with the fully qualified class name of the module implementation.
