package io.fabric8.crd.generator.victools;

import io.fabric8.crd.generator.victools.spi.CRDGeneratorContext;
import io.fabric8.kubernetes.client.utils.KubernetesSerialization;

/**
 * Internal Context, intended to allow access to some internals.
 */
public interface CRDGeneratorContextInternal extends CRDGeneratorContext {
  KubernetesSerialization getKubernetesSerialization();
  boolean isEnabled(CRDGeneratorSchemaOption option);
}
