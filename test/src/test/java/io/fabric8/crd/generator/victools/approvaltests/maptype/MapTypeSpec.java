package io.fabric8.crd.generator.victools.approvaltests.maptype;

import io.fabric8.crd.generator.victools.annotation.MapType;
import lombok.Data;

import java.util.Map;

@Data
public class MapTypeSpec {

  private ClassWithMapTypeAtomic classWithMapTypeAtomic;
  private ClassWithMapTypeGranular classWithMapTypeGranular;

  @MapType(MapType.Type.ATOMIC)
  private Map<String, String> mapWithMapTypeAtomic;

  @MapType(MapType.Type.ATOMIC)
  static class ClassWithMapTypeAtomic {
  }

  @MapType(MapType.Type.GRANULAR)
  static class ClassWithMapTypeGranular {
  }

}
