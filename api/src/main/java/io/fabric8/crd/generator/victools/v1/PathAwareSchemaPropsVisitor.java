package io.fabric8.crd.generator.victools.v1;

import io.fabric8.kubernetes.api.model.apiextensions.v1.JSONSchemaProps;
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
 */
@Slf4j
@RequiredArgsConstructor
class PathAwareSchemaPropsVisitor {

  private static final String JSONPATH_SEPARATOR = ".";
  private static final String JSONPATH_SEPARATOR_ARRAY = "[]";

  private final List<PropertyVisitor> propertyVisitors = new LinkedList<>();
  private final List<IdentifiedPropertyVisitor> identifiedPropertyVisitors = new LinkedList<>();
  private final List<PropertyVisitor> directPropertyVisitors = new LinkedList<>();
  private final List<IdentifiedPropertyVisitor> directIdentifiedPropertyVisitors = new LinkedList<>();

  /**
   * Registers a visitor, which visits every property.
   *
   * @param visitor the visitor
   * @return this instance, for chaining.
   */
  public PathAwareSchemaPropsVisitor withPropertyVisitor(PropertyVisitor visitor) {
    this.propertyVisitors.add(visitor);
    return this;
  }

  /**
   * Registers a visitor, which visits every identified property.
   *
   * @param visitor the visitor
   * @return this instance, for chaining.
   */
  public PathAwareSchemaPropsVisitor withPropertyVisitor(IdentifiedPropertyVisitor visitor) {
    this.identifiedPropertyVisitors.add(visitor);
    return this;
  }

  /**
   * Registers a visitor, which visits every direct property (JSON Path from root contains no array).
   *
   * @param visitor the visitor
   * @return this instance, for chaining.
   */
  public PathAwareSchemaPropsVisitor withDirectPropertyVisitor(PropertyVisitor visitor) {
    this.directPropertyVisitors.add(visitor);
    return this;
  }

  /**
   * Registers a visitor, which visits every identified direct property (JSON Path from root contains no array).
   *
   * @param visitor the visitor
   * @return this instance, for chaining.
   */
  public PathAwareSchemaPropsVisitor withDirectPropertyVisitor(IdentifiedPropertyVisitor visitor) {
    this.directIdentifiedPropertyVisitors.add(visitor);
    return this;
  }

  public void visit(JSONSchemaProps schema) {
    visit("", schema);
  }

  private void visit(String parentPath, JSONSchemaProps schema) {
    visitProperty(parentPath, schema);
    if ("object".equals(schema.getType())) {
      for (var prop : schema.getProperties().entrySet()) {
        var propPath = parentPath + JSONPATH_SEPARATOR + prop.getKey();
        var propSchema = prop.getValue();
        visit(propPath, propSchema);
      }
    } else if ("array".equals(schema.getType())) {
      var path = parentPath + JSONPATH_SEPARATOR_ARRAY;
      var itemSchema = schema.getItems().getSchema();
      visit(path, itemSchema);
    }
  }

  private void visitProperty(String path, JSONSchemaProps schema) {
    onFieldProperty(path, schema);
    ofNullable(schema.getId())
        .ifPresent(id -> onIdentifiedFieldProperty(id, path, schema));
  }

  private void onFieldProperty(String path, JSONSchemaProps schema) {
    propertyVisitors.forEach(v -> v.visit(path, schema));
    if (isDirectPath(path)) {
      directPropertyVisitors.forEach(v -> v.visit(path, schema));
    }
  }

  private void onIdentifiedFieldProperty(String id, String path, JSONSchemaProps schema) {
    // remove id, not required anymore
    schema.setId(null);
    // execute registered visitors
    identifiedPropertyVisitors.forEach(v -> v.visit(id, path, schema));
    if (isDirectPath(path)) {
      directIdentifiedPropertyVisitors.forEach(v -> v.visit(id, path, schema));
    }
  }

  @FunctionalInterface
  public interface PropertyVisitor {
    void visit(String path, JSONSchemaProps schema);
  }

  @FunctionalInterface
  public interface IdentifiedPropertyVisitor {
    void visit(String id, String path, JSONSchemaProps schema);
  }

  private static boolean isDirectPath(String path) {
    return !path.contains(JSONPATH_SEPARATOR_ARRAY);
  }
}
