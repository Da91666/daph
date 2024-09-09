package com.dasea.daph.core.classloader;

import java.net.URL;
import java.util.Arrays;

public class JNodeClassLoaderByURL extends ANodeClassLoaderByURLs {
    private final String classNamePrefix;

    public JNodeClassLoaderByURL(URL[] urls, String classNamePrefix) {
        super(urls);
        this.classNamePrefix = classNamePrefix;
    }

    public JNodeClassLoaderByURL(URL url, String classNamePrefix) {
        super(new URL[]{url});
        this.classNamePrefix = classNamePrefix;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                String[] ps = classNamePrefix.split(";");
                boolean flag = Arrays.stream(ps)
                        .map(name::startsWith)
                        .reduce((a, b) -> a | b).get();
                if (flag) {
                    c = findClass(name);
                } else {
                    c = this.getParent().loadClass(name);
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }
}
