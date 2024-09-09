package com.dasea.daph.core.classloader

import java.io.{FileInputStream, IOException}

abstract class ANodeClassLoaderByPath(classPath: String) extends INodeClassLoader {
  @throws[ClassNotFoundException]
  override protected def findClass(name: String): Class[_] = try {
    val data = loadBytes(name)
    defineClass(name, data, 0, data.length)
  } catch {
    case e: IOException =>
      throw new ClassNotFoundException(name)
  }


  @throws[IOException]
  private def loadBytes(name: String) = {
    val path = name.replace('.', '/').concat(".class")
    // 去路径下查找这个类
    var data: Array[Byte] = null
    try {
      val fileInputStream = new FileInputStream(classPath + "/" + path)
      try {
        val len = fileInputStream.available
        data = new Array[Byte](len)
        fileInputStream.read(data)
      } finally if (fileInputStream != null) fileInputStream.close()
    }
    data
  }
}
