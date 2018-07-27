package com.github.divideby0.spotfire.utils;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

// needed to get kie to work in a fat jar as per here:
// https://stackoverflow.com/a/47514137
public class UberJarKieClassLoader extends ClassLoader {
  private final URL[] kieConfUrls;

  public UberJarKieClassLoader(final ClassLoader originalClassLoader, final URL[] kieConfUrls) {
    super(originalClassLoader);
    this.kieConfUrls = kieConfUrls;
  }

  @Override
  public Enumeration<URL> getResources(String name) throws IOException {
    if ("META-INF/kie.conf".equals(name)) {
      return new Enumeration<URL>() {
        int index;
        @Override
        public boolean hasMoreElements() {
          return index < kieConfUrls.length;
        }

        @Override
        public URL nextElement() {
          return kieConfUrls[index++];
        }
      };
    }
    return super.getResources(name);
  }
}
