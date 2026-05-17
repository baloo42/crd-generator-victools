package io.fabric8.crd.generator.victools.approvaltests.validation;

import io.fabric8.generator.annotation.Max;
import io.fabric8.generator.annotation.Min;
import io.fabric8.generator.annotation.Pattern;
import io.fabric8.generator.annotation.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Tests for Fabric8 validation annotations
 */
@Data
public class FkcValidationSpec {

  private ValidationOnInteger onInteger;
  private ValidationOnIntegerPrim onIntegerPrim;
  private ValidationOnLong onLong;
  private ValidationOnLongPrim onLongPrim;
  private ValidationOnFloat onFloat;
  private ValidationOnFloatPrim onFloatPrim;
  private ValidationOnDouble onDouble;
  private ValidationOnDoublePrim onDoublePrim;
  private ValidationOnString onString;
  private ValidationExclusive onExclusive;
  private ValidationSize onSize;

  @Data
  static class ValidationOnInteger {
    @Min(1)
    private Integer minimum1;
    @Max(1)
    private Integer maximum1;
    @Min(1)
    @Max(3)
    private Integer minimum1Maximum3;
  }

  @Data
  static class ValidationOnIntegerPrim {
    @Min(1)
    private int minimum1;
    @Max(1)
    private int maximum1;
    @Min(1)
    @Max(3)
    private int minimum1Maximum3;
  }

  @Data
  static class ValidationOnLong {
    @Min(1)
    private Long minimum1;
    @Max(1)
    private Long maximum1;
    @Min(1)
    @Max(3)
    private Long minimum1Maximum3;
  }

  @Data
  static class ValidationOnLongPrim {
    @Min(1)
    private long minimum1;
    @Max(1)
    private long maximum1;
    @Min(1)
    @Max(3)
    private long minimum1Maximum3;
  }

  @Data
  static class ValidationOnFloat {
    @Min(1)
    private Float minimum1;
    @Max(1)
    private Float maximum1;
    @Min(1)
    @Max(3)
    private Float minimum1Maximum3;
  }

  @Data
  static class ValidationOnFloatPrim {
    @Min(1)
    private float minimum1;
    @Max(1)
    private float maximum1;
    @Min(1)
    @Max(3)
    private float minimum1Maximum3;
  }

  @Data
  static class ValidationOnDouble {
    @Min(1)
    private Double minimum1;
    @Max(1)
    private Double maximum1;
    @Min(1)
    @Max(3)
    private Double minimum1Maximum3;
  }

  @Data
  static class ValidationOnDoublePrim {
    @Min(1)
    private double minimum1;
    @Max(1)
    private double maximum1;
    @Min(1)
    @Max(3)
    private double minimum1Maximum3;
  }

  @Data
  static class ValidationOnString {
    @Pattern("(a|b)+")
    private String pattern;
  }

  @Data
  static class ValidationExclusive {
    @Min(value = 1, inclusive = false)
    private Integer exclusiveMinimum1;
    @Max(value = 3, inclusive = false)
    private Integer exclusiveMaximum3;
    @Min(value = 1, inclusive = false)
    @Max(value = 3, inclusive = false)
    private Integer exclusiveMinimum1Maximum3;
  }

  @Data
  static class ValidationSize {
    @Size(min = 1, max = 5)
    private List<String> list;
    @Size(min = 2, max = 10)
    private String string;
    @Size(min = 1, max = 4)
    private Map<String, String> map;
  }

}
