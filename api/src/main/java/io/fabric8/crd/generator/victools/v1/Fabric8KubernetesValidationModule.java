package io.fabric8.crd.generator.victools.v1;

import io.fabric8.crd.generator.victools.CRDGeneratorContextInternal;
import io.fabric8.crd.generator.victools.schema.AbstractKubernetesValidationModule;
import io.fabric8.generator.annotation.ValidationRule;
import io.fabric8.generator.annotation.ValidationRules;

class Fabric8KubernetesValidationModule extends AbstractKubernetesValidationModule<ValidationRule, ValidationRules> {

  public Fabric8KubernetesValidationModule(CRDGeneratorContextInternal context) {
    super(context, ValidationRule.class, ValidationRules.class, ValidationRules::value,
        CRDv1Utils::createValidationRule);
  }

}
