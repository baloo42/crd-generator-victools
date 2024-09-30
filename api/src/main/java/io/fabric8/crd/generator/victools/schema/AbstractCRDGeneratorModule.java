package io.fabric8.crd.generator.victools.schema;

import com.github.victools.jsonschema.generator.Module;
import io.fabric8.crd.generator.victools.CRDGeneratorContextInternal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractCRDGeneratorModule implements Module {

  private final CRDGeneratorContextInternal context;

}
