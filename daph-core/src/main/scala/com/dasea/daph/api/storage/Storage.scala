package com.dasea.daph.api.storage

trait Storage {
	def setDSToLine(ds: Any, line: String): Unit

	def setDSToLines(ds: Any, lines: Array[String]): Unit

	def getDSByLine(line: String): Any
	
	def getLineToDSByLines(lines: Array[String]): Map[String, Any]
	
	def contains(line: String): Boolean
	
	def remove(line: String): Unit
	
	def stop(): Unit = {}
	
	def clear(): Unit = {}
}
