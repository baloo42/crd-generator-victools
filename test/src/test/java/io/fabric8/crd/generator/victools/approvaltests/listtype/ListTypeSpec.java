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

  @ListType(value = ListType.Type.MAP, mapKeys = {"id"})
  private List<NotAnnotated> listTypeMapWithOneKeyNotAnnotated;

  @ListType(value = ListType.Type.MAP, mapKeys = {"id", "name"})
  private List<NotAnnotated> listTypeMapWithTwoKeyNotAnnotated;

  @ListType(ListType.Type.ATOMIC)
  private String[] arrayListTypeAtomicString;

  @ListType(ListType.Type.SET)
  private String[] arrayListTypeSetString;

  @ListType(ListType.Type.MAP)
  private EntityWithOneKey[] arrayListTypeMapWithOneKey;

  @ListType(ListType.Type.MAP)
  private EntityWithTwoKeys[] arrayListTypeMapWithTwoKeys;

  @ListType(value = ListType.Type.MAP, mapKeys = {"id"})
  private NotAnnotated[] arrayListTypeMapWithOneKeyNotAnnotated;

  @ListType(value = ListType.Type.MAP, mapKeys = {"id", "name"})
  private NotAnnotated[] arrayListTypeMapWithTwoKeyNotAnnotated;

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

  @Data
  private static class NotAnnotated {
    private Integer id;
    private String name;
  }

}
