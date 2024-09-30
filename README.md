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
  Command line interface using this implementation
- `maven-plugin`  
  Maven plugin using this implementation
- `compliance-test`  
  Tests to compare different implementations. (The same as upstream)
- `test`  
  Tests for features which are not (yet) supported in upstream.
