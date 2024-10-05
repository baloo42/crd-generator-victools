package io.fabric8.crd.generator.victools;

import io.fabric8.kubernetes.client.utils.Utils;
import lombok.Builder;
import lombok.NonNull;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface CRDOutput<T extends OutputStream> extends Closeable {

  T getOutputStream(CRDOutputContext details) throws IOException;

  default URI getURI(CRDOutputContext details) {
    return URI.create("file:///" + getName(details));
  }

  default String getName(CRDOutputContext details) {
    return Utils.interpolateString(details.filenameFormat(), details.getInterpolationContext());
  }

  interface CRDOutputContext {
    String crdName();

    String crdSpecVersion();

    String filenameFormat();

    Map<String, String> getInterpolationContext();
  }

  static CRDOutputContextImpl.CRDOutputContextImplBuilder contextBuilder() {
    return CRDOutputContextImpl.builder();
  }

  @Builder
  record CRDOutputContextImpl(
      @NonNull String crdName,
      @NonNull String crdSpecVersion,
      @NonNull String resourceGroup,
      @NonNull String resourceKind,
      @NonNull String resourceSingular,
      @NonNull String resourcePlural,
      @NonNull String filenameFormat) implements CRDOutputContext {

    @Override
    public Map<String, String> getInterpolationContext() {
      var map = new HashMap<String, String>();
      map.put("crdName", crdName);
      map.put("crdSpecVersion", crdSpecVersion);
      map.put("resourceGroup", resourceGroup.toLowerCase());
      map.put("resourceKind", resourceKind.toLowerCase());
      map.put("resourceSingular", resourceSingular.toLowerCase());
      map.put("resourcePlural", resourcePlural.toLowerCase());
      return map;
    }
  }

  static CRDSchemaOutputContextImpl.CRDSchemaOutputContextImplBuilder schemaContextBuilder(
      CRDOutputContextImpl crdOutContext) {

    return CRDSchemaOutputContextImpl.builder()
        .crdName(crdOutContext.crdName())
        .crdSpecVersion(crdOutContext.crdSpecVersion())
        .resourceGroup(crdOutContext.resourceGroup())
        .resourceKind(crdOutContext.resourceKind())
        .resourceSingular(crdOutContext.resourceSingular())
        .resourcePlural(crdOutContext.resourcePlural());
  }

  @Builder
  record CRDSchemaOutputContextImpl(
      @NonNull String crdName,
      @NonNull String crdSpecVersion,
      @NonNull String resourceGroup,
      @NonNull String resourceKind,
      @NonNull String resourceSingular,
      @NonNull String resourcePlural,
      @NonNull String resourceAPIVersion,
      @NonNull String filenameFormat) implements CRDOutputContext {

    @Override
    public Map<String, String> getInterpolationContext() {
      var map = new HashMap<String, String>();
      map.put("crdName", crdName);
      map.put("crdSpecVersion", crdSpecVersion);
      map.put("resourceGroup", resourceGroup.toLowerCase());
      map.put("resourceKind", resourceKind.toLowerCase());
      map.put("resourceSingular", resourceSingular.toLowerCase());
      map.put("resourcePlural", resourcePlural.toLowerCase());
      map.put("resourceAPIVersion", resourceAPIVersion.toLowerCase());
      return map;
    }
  }

  abstract class AbstractCRDOutput<T extends OutputStream> implements CRDOutput<T> {
    private final Set<T> streams = new HashSet<>();

    @Override
    public T getOutputStream(CRDOutputContext details) throws IOException {
      final T outputStream = createOutputStream(details);
      streams.add(outputStream);
      return outputStream;
    }

    protected abstract T createOutputStream(CRDOutputContext details) throws IOException;

    @Override
    public void close() throws IOException {
      for (T stream : streams) {
        stream.close();
      }
    }
  }

  class DirCRDOutput extends AbstractCRDOutput<FileOutputStream> {

    private final File dir;

    public DirCRDOutput(File dir) {
      if (!dir.isDirectory() || !dir.canWrite() || !dir.exists()) {
        throw new IllegalArgumentException(dir + " must exist, be a writeable output directory");
      }
      this.dir = dir;
    }

    @Override
    protected FileOutputStream createOutputStream(CRDOutputContext details) throws IOException {
      final File file = getFile(details);
      Files.createDirectories(file.toPath().getParent());
      return new FileOutputStream(file);
    }

    private File getFile(CRDOutputContext details) {
      return new File(dir, getName(details));
    }

    @Override
    public URI getURI(CRDOutputContext details) {
      return getFile(details).toURI();
    }
  }

}
