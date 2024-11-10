package io.fabric8.crd.generator.victools;

import io.fabric8.crd.generator.victools.model.PrinterColumnInfo;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Function;

import static io.fabric8.crd.generator.victools.CRDUtils.findRepeatingAnnotations;

@FunctionalInterface
public interface AdditionalPrinterColumnProvider {

  List<PrinterColumnInfo> getAdditionalPrinterColumns();

  @RequiredArgsConstructor
  class TopLevelAnnotationPrinterColumnProvider<T extends Annotation> implements AdditionalPrinterColumnProvider {
    private final CustomResourceInfo crInfo;
    private final Class<T> annotationClass;
    private final Function<T, PrinterColumnInfo> mapper;

    @Override
    public List<PrinterColumnInfo> getAdditionalPrinterColumns() {
      return findRepeatingAnnotations(crInfo.definition(), annotationClass).stream()
          .map(mapper)
          .toList();
    }
  }
}
