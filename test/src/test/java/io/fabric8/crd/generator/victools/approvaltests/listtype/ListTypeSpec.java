package io.fabric8.crd.generator.victools.approvaltests.listtype;

import io.fabric8.crd.generator.victools.annotation.ListMapKey;
import io.fabric8.crd.generator.victools.annotation.ListType;
import lombok.Data;

import java.util.List;

@Data
public class ListTypeSpec {

  @ListType(ListType.Type.ATOMIC)
  private List<String> listTypeAtomicString;

  @ListType(ListType.Type.SET)
  private List<String> listTypeSetString;

  @ListType(ListType.Type.MAP)
  private List<EntityWithOneKey> listTypeMapWithOneKey;

  @ListType(ListType.Type.MAP)
  private List<EntityWithTwoKeys> listTypeMapWithTwoKeys;

  @Data
  private static class EntityWithOneKey {
    @ListMapKey
    private Integer id;
    private String name;
  }

  @Data
  private static class EntityWithTwoKeys {
    @ListMapKey
    private Integer id;
    @ListMapKey
    private String name;
  }

}
