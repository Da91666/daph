package com.dasea.daph.core

import com.dasea.daph.api.storage.Storage

import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._
import scala.collection.concurrent

class JVMStorage extends Storage {
	private val lineToDS: concurrent.Map[String, Any] = new ConcurrentHashMap[String, Any].asScala

	override def setDSToLine(ds: Any, line: String): Unit = {
		if (ds != null) lineToDS.put(line, ds)
	}

	override def setDSToLines(ds: Any, lines: Array[String]): Unit = {
		if (ds != null) {
			lines.foreach(key => lineToDS.put(key, ds))
		}
	}
	
	override def getDSByLine(line: String): Any = {
		if (contains(line)) lineToDS(line) else null
	}
	
	override def getLineToDSByLines(lines: Array[String]): Map[String, Any] = {
		lineToDS.filterKeys(lines.contains).map { case (line, ds) =>
			line -> ds
		}.toMap
	}
	
	override def contains(line: String): Boolean = {
		lineToDS.contains(line)
	}
	
	override def remove(line: String): Unit = {
		lineToDS.remove(line)
	}
	
	override def stop(): Unit = {
		lineToDS.clear()
	}
	
	override def clear(): Unit = {
		lineToDS.clear()
	}
}
