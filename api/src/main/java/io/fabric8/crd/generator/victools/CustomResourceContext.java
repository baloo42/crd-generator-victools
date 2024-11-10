package io.fabric8.crd.generator.victools;

import io.fabric8.crd.generator.victools.model.PrinterColumnInfo;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;

/**
 * Custom resource specific context.
 * <p>
 * Intended to be used to collect metadata during the generation and provide it to post-processing.
 * As such it's lifecycle is bound to the generation of as single Custom Resource Definition version.
 * </p>
 *
 * @see FieldMetadata
 * @see io.fabric8.crd.generator.victools.schema.MetadataModule
 * @see io.fabric8.crd.generator.victools.v1.PathAwareSchemaPropsVisitor
 */
@Slf4j
@RequiredArgsConstructor
public class CustomResourceContext {

  private final Map<String, FieldMetadata> fieldMeta = new HashMap<>();
  @Getter
  private final Set<String> dependentClasses = new HashSet<>();

  public boolean addDependentClass(String name) {
    return dependentClasses.add(name);
  }

  public void setPrinterColumnInfo(String fieldId, PrinterColumnInfo info) {
    getFieldMeta(fieldId).setPrinterColumnInfo(info);
  }

  public Optional<PrinterColumnInfo> findPrinterColumnInfo(String fieldId) {
    return findFieldMeta(fieldId)
        .flatMap(FieldMetadata::getPrinterColumnInfo);
  }

  public void setSpecReplicasPath(String fieldId, boolean isSpecReplicasPath) {
    getFieldMeta(fieldId).setSpecReplicasPath(isSpecReplicasPath);
  }

  public boolean isSpecReplicasPath(String fieldId) {
    return findFieldMeta(fieldId)
        .map(FieldMetadata::isSpecReplicasPath)
        .orElse(false);
  }

  public void setStatusReplicasPath(String fieldId, boolean isStatusReplicasPath) {
    getFieldMeta(fieldId).setStatusReplicasPath(isStatusReplicasPath);
  }

  public boolean isStatusReplicasPath(String fieldId) {
    return findFieldMeta(fieldId)
        .map(FieldMetadata::isStatusReplicasPath)
        .orElse(false);
  }

  public void setLabelSelectorPath(String fieldId, boolean isLabelSelectorPath) {
    getFieldMeta(fieldId).setLabelSelectorPath(isLabelSelectorPath);
  }

  public boolean isLabelSelectorPath(String fieldId) {
    return findFieldMeta(fieldId)
        .map(FieldMetadata::isLabelSelectorPath)
        .orElse(false);
  }

  public void setSelectableFieldPath(String fieldId, boolean isSelectableFieldPath) {
    getFieldMeta(fieldId).setSelectableFieldPath(isSelectableFieldPath);
  }

  public boolean isSelectableFieldPath(String fieldId) {
    return findFieldMeta(fieldId)
        .map(FieldMetadata::isSelectableFieldPath)
        .orElse(false);
  }

  private FieldMetadata getFieldMeta(String fieldId) {
    fieldMeta.putIfAbsent(fieldId, new FieldMetadata());
    return fieldMeta.get(fieldId);
  }

  private Optional<FieldMetadata> findFieldMeta(String id) {
    return ofNullable(fieldMeta.get(id));
  }

  /**
   * Additional metadata of a field.
   */
  @Data
  private static class FieldMetadata {

    private PrinterColumnInfo printerColumnInfo;
    private boolean specReplicasPath;
    private boolean statusReplicasPath;
    private boolean labelSelectorPath;
    private boolean selectableFieldPath;

    public Optional<PrinterColumnInfo> getPrinterColumnInfo() {
      return ofNullable(printerColumnInfo);
    }
  }

}
