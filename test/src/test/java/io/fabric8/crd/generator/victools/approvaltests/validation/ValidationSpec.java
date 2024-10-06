package io.fabric8.crd.generator.victools.approvaltests.validation;

import io.fabric8.generator.annotation.Max;
import io.fabric8.generator.annotation.Min;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ValidationSpec {

  ValidationOnInteger onInteger;
  ValidationOnIntegerPrim onIntegerPrim;
  ValidationOnLong onLong;
  ValidationOnLongPrim onLongPrim;
  ValidationOnFloat onFloat;
  ValidationOnFloatPrim onFloatPrim;
  ValidationOnDouble onDouble;
  ValidationOnDoublePrim onDoublePrim;
  ValidationOnString onString;

  @Data
  static class ValidationOnInteger {
    @Min(1)
    private Integer mustBeGreaterThanOne;
    @Max(1)
    private Integer mustBeLessThanOne;
    @Min(1)
    @Max(3)
    private Integer mustBeGreaterThanOneAndLessThanThree;

    private List<@io.fabric8.crd.generator.victools.annotation.Min(1) Integer> inListMustBeGreaterThanOne;
    private List<@io.fabric8.crd.generator.victools.annotation.Max(3) Integer> inListMustBeLessThanThree;
    private List<@io.fabric8.crd.generator.victools.annotation.Min(1) @io.fabric8.crd.generator.victools.annotation.Max(3) Integer> inListMustBeGreaterThanOneAndLessThanThree;

    private Map<String, @io.fabric8.crd.generator.victools.annotation.Min(1) Integer> inMapMustBeGreaterThanOne;
    private Map<String, @io.fabric8.crd.generator.victools.annotation.Max(3) Integer> inMapMustBeLessThanThree;
    private Map<String, @io.fabric8.crd.generator.victools.annotation.Min(1) @io.fabric8.crd.generator.victools.annotation.Max(3) Integer> inMapMustBeGreaterThanOneAndLessThanThree;
  }

  @Data
  static class ValidationOnIntegerPrim {
    @Min(1)
    private int mustBeGreaterThanOne;
    @Max(1)
    private int mustBeLessThanOne;
    @Min(1)
    @Max(3)
    private int mustBeGreaterThanOneAndLessThanThree;
  }

  @Data
  static class ValidationOnLong {
    @Min(1)
    private Long mustBeGreaterThanOne;
    @Max(1)
    private Long mustBeLessThanOne;
    @Min(1)
    @Max(3)
    private Long mustBeGreaterThanOneAndLessThanThree;
  }

  @Data
  static class ValidationOnLongPrim {
    @Min(1)
    private long mustBeGreaterThanOne;
    @Max(1)
    private long mustBeLessThanOne;
    @Min(1)
    @Max(3)
    private long mustBeGreaterThanOneAndLessThanThree;
  }

  @Data
  static class ValidationOnFloat {
    @Min(1)
    private Float mustBeGreaterThanOne;
    @Max(1)
    private Float mustBeLessThanOne;
    @Min(1)
    @Max(3)
    private Float mustBeGreaterThanOneAndLessThanThree;
  }

  @Data
  static class ValidationOnFloatPrim {
    @Min(1)
    private float mustBeGreaterThanOne;
    @Max(1)
    private float mustBeLessThanOne;
    @Min(1)
    @Max(3)
    private float mustBeGreaterThanOneAndLessThanThree;
  }

  @Data
  static class ValidationOnDouble {
    @Min(1)
    private Double mustBeGreaterThanOne;
    @Max(1)
    private Double mustBeLessThanOne;
    @Min(1)
    @Max(3)
    private Double mustBeGreaterThanOneAndLessThanThree;
  }

  @Data
  static class ValidationOnDoublePrim {
    @Min(1)
    private double mustBeGreaterThanOne;
    @Max(1)
    private double mustBeLessThanOne;
    @Min(1)
    @Max(3)
    private double mustBeGreaterThanOneAndLessThanThree;
  }

  @Data
  static class ValidationOnString {
    @Min(1)
    private String mustBeGreaterThanOne;
    @Max(1)
    private String mustBeLessThanOne;
    @Min(1)
    @Max(3)
    private String mustBeGreaterThanOneAndLessThanThree;

    private List<@io.fabric8.crd.generator.victools.annotation.Min(1) String> inListMustBeGreaterThanOne;
    private List<@io.fabric8.crd.generator.victools.annotation.Max(3) String> inListMustBeLessThanThree;
    private List<@io.fabric8.crd.generator.victools.annotation.Min(1) @io.fabric8.crd.generator.victools.annotation.Max(3) String> inListMustBeGreaterThanOneAndLessThanThree;
    private List<@io.fabric8.crd.generator.victools.annotation.Pattern("(a|b)+") String> inListMustComplyPattern;

    private Map<String, @io.fabric8.crd.generator.victools.annotation.Min(1) String> inMapMustBeGreaterThanOne;
    private Map<String, @io.fabric8.crd.generator.victools.annotation.Max(3) String> inMapMustBeLessThanThree;
    private Map<String, @io.fabric8.crd.generator.victools.annotation.Min(1) @io.fabric8.crd.generator.victools.annotation.Max(3) String> inMapMustBeGreaterThanOneAndLessThanThree;
    private Map<String, @io.fabric8.crd.generator.victools.annotation.Pattern("(a|b)+") String> inMapMustComplyPattern;
  }

}
