package io.fabric8.crd.generator.victools.v1;

import io.fabric8.kubernetes.api.model.apiextensions.v1.JSONSchemaProps;
import io.fabric8.kubernetes.api.model.apiextensions.v1.JSONSchemaPropsBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

import static java.util.Optional.ofNullable;

/**
 * Component to traverse the resulting schema in the post-processing phase.
 * <p>
 * Registered visitors are able to manipulate the schema and are executed if their conditions apply.
 * Associated field metadata, if available, is offered to the visitors and the ID used to find the
 * associated metadata is removed from the schema.
 * </p>
 *
 * @see io.fabric8.crd.generator.victools.schema.MetadataModule
 * @see io.fabric8.crd.generator.victools.CustomResourceContext
 * @see PrinterColumnCollector
 * @see ScaleSubresourceCollector
 */
@Slf4j
@RequiredArgsConstructor
class PathAwareSchemaPropsVisitor {

  private final List<PropertyVisitor> propertyVisitors = new LinkedList<>();
  private final List<IdentifiedPropertyVisitor> identifiedPropertyVisitors = new LinkedList<>();

  public PathAwareSchemaPropsVisitor withPropertyVisitor(PropertyVisitor visitors){
    this.propertyVisitors.add(visitors);
    return this;
  }

  public PathAwareSchemaPropsVisitor withIdentifiedPropertyVisitor(IdentifiedPropertyVisitor visitors){
    this.identifiedPropertyVisitors.add(visitors);
    return this;
  }

  public void visit(JSONSchemaPropsBuilder schema) {
    visit("", schema);
  }

  private void visit(String parentPath, JSONSchemaPropsBuilder schema) {
    if("object".equals(schema.getType())) {
      for(var prop : schema.getProperties().entrySet()) {
        var fieldPath = parentPath + "." + prop.getKey();
        var fieldSchema = prop.getValue();
        visitField(fieldPath, fieldSchema);
        visit(fieldPath, fieldSchema.edit());
      }
    }
  }

  private void visitField(String path, JSONSchemaProps schema){
    onFieldProperty(path, schema);
    ofNullable(schema.getId())
        .ifPresent(id -> onIdentifiedFieldProperty(id, path, schema));
  }

  private void onFieldProperty(String path, JSONSchemaProps schema) {
    propertyVisitors.forEach(v -> v.visit(path, schema));
  }

  private void onIdentifiedFieldProperty(String id, String path, JSONSchemaProps schema){
    // remove id, not required anymore
    schema.setId(null);
    // execute registered visitors
    identifiedPropertyVisitors.forEach(v -> v.visit(id, path, schema));
  }

  @FunctionalInterface
  public interface PropertyVisitor {
    void visit(String path, JSONSchemaProps schema);
  }

  @FunctionalInterface
  public interface IdentifiedPropertyVisitor {
    void visit(String id, String path, JSONSchemaProps schema);
  }

}
