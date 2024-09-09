package com.dasea.daph.core.classloader;

import java.util.Arrays;

public class JNodeClassLoaderByPath extends ANodeClassLoaderByPath {
    private final String classNamePrefix;

    public JNodeClassLoaderByPath(String classPath, String classNamePrefix) {
        super(classPath);
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
