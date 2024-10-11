package io.fabric8.crd.generator.victools.approvaltests.validation;

import io.fabric8.generator.annotation.Max;
import io.fabric8.generator.annotation.Min;
import io.fabric8.generator.annotation.Pattern;
import lombok.Data;

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

}
