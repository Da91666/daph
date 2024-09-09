package com.dasea.daph.api.computer

abstract class Computer {
	val entrypoint: Any

	def stop(): Unit = {}

	def clear(): Unit = {}
}