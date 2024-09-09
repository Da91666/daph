package com.dasea.daph.core.classloader

import java.net.{URL, URLClassLoader}

abstract class ANodeClassLoaderByURLs(urls: Array[URL])
  extends URLClassLoader(urls) with INodeClassLoader
