package io.fabric8.crd.generator.victools;

import io.fabric8.crd.generator.victools.model.PrinterColumnInfo;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.util.List;

import static io.fabric8.crd.generator.victools.CRDUtils.findRepeatingAnnotations;

@FunctionalInterface
public interface PrinterColumnProvider {

  List<PrinterColumnInfo> getPrinterColumns();

  @RequiredArgsConstructor
  abstract class TopLevelAnnotationPrinterColumnProvider<T extends Annotation> implements
      PrinterColumnProvider {
    private final CustomResourceInfo crInfo;
    private final Class<T> annotationClass;

    protected abstract PrinterColumnInfo map(T annotation);

    @Override
    public List<PrinterColumnInfo> getPrinterColumns() {
      return findRepeatingAnnotations(crInfo.definition(), annotationClass).stream()
          .map(this::map)
          .toList();
    }
  }
}
