package io.fabric8.crd.generator.victools.approvaltests.subtype;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

@Data
public class SubTypeSpec {

  private Animal animal;

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "animalType")
  @JsonSubTypes({
      @JsonSubTypes.Type(value = Dog.class, name = "dog"),
      @JsonSubTypes.Type(value = Cat.class, name = "cat")
  })
  public static class Animal {
    public String name;
  }

  @JsonTypeName("dog")
  public static class Dog extends Animal {
    public double barkVolume;
  }

  @JsonTypeName("cat")
  public static class Cat extends Animal {
    public int lives;
    boolean likesCream;
  }

}
