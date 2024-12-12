# Fabric8 CRD-Generator (victools/json-schema)

Generate Custom Resource Definitions (CRD) for Kubernetes from Java.

Despite the [CRD-Generator](https://github.com/fabric8io/kubernetes-client/blob/main/doc/CRD-generator.md)
from [fabric8/kubernetes-client](https://github.com/fabric8io/kubernetes-client), this implementation uses
[victools/jsonschema-generator](https://github.com/victools/jsonschema-generator) instead
of [jackson/jsonSchema](https://github.com/FasterXML/jackson-module-jsonSchema).

## Features

The CRD-Generator allows to generate Custom Resource Definitions (CRD) from a Java model using reflection.
Annotations can be added to the model to enhance the resulting schema.
The following annotation sets can be used:

### Fabric8/kubernetes-client annotations

_Enabled by default_

It's possible to use the annotations from fabric8/kubernetes-client in the same way you might be used to. The output
should be in most cases exactly the same. The only known difference is, that this implementation doesn't support
`@SchemaSwap`.

### Own annotations

_Enabled by default_

This project provides an own set of annotations, too. This set is at the moment more feature complete
compared to the set from fabric8/kubernetes-client. 

### Jackson annotations

_Enabled by default_

A lot Jackson annotations are supported including `@JsonSubTypes` to created `anyOf` schemas.

See [victools/jsonschema-generator Jackson Module](https://victools.github.io/jsonschema-generator/#jackson-module)
for details.

### Jakarta Validation annotations

_Disabled by default_

Jakarta validation annotations are supported.

See [victools/jsonschema-generator Jakarta Validation Module](https://victools.github.io/jsonschema-generator/#jakarta-validation-module)
for details.

### Swagger 2 annotations

_Disabled by default_

Swagger 2 annotations are supported.

See [victools/jsonschema-generator Swagger 2 Module](https://victools.github.io/jsonschema-generator/#swagger-2-module)
for details.

### JSON-Schemas

JSON-Schemas can be emitted in addition to the regular CRDs.  
Those schemas can be used by various tools e.g. [Kubeconform](https://github.com/yannh/kubeconform) as an alternative to
`kubectl --dry-run`,
to perform validation on custom (and native) Kubernetes resources.

Running Kubernetes schema validation checks helps to apply the **"shift-left approach"** on machines **without** giving
them access to your cluster (e.g. locally or on CI).

### SPI

The CRD-Generator can be extended with _CRD-Generator Schema Modules_.

A module must implement the `CRDGeneratorSchemaModule` interface which allows to hook into victools
schema-generator configuration. Victools modules can't be used directly because they allow too much to modify.

The CRD-Generator loads modules via ServiceLoader to implement autoconfiguration.
This requires modules to include a file
`META-INF/services/io.fabric8.crd.generator.victools.spi.CRDGeneratorSchemaModule`
with the fully qualified class name of the module implementation.

## Modules

- `annotations`  
  Our own annotations.
- `spi`  
  Contains interfaces to extend the CRD-Generator.
- `api`  
  Implementation and main interface.
- `cli`  
  Command line interface using this implementation.
- `maven-plugin`  
  Maven plugin using this implementation.
- `compliance-test`  
  Tests to compare different implementations. (same as upstream)
- `test`  
  Tests for features which are not (yet) supported in upstream.

## Differences

_compared to fabric8/kubernetes-client v6 CRD-Generator_

- No support for `@SchemaSwap`
- Support for `@AdditionalPrinterColumn`
- Support for `@MapType`
- Support for `@ExternalDocs`
- Support for `@AdditionalSelectableField` / `@SelectableField`
- Support for Jackson including `anyOf`/`@JsonSubTypes`.
- Support for Jakarta Validations Annotations
- Support for Swagger 2 Annotations
- Support for adding annotations and labels to resulting CRD (e.g. build-timestamp from maven var)
- Support for [emitting JSON-Schemas](#json-schemas)
- Maven Plugin and CLI
