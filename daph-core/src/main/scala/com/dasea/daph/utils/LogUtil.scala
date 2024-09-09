package com.dasea.daph.utils

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.scala.Logging

object LogUtil extends Logging {
  def setLogXml(logXml: String): Unit = {
    if (!StringUtils.isEmpty(logXml)) {
      Configurator.initialize("daph-log-xml", logXml)
      logger.info(s"Already set logXml to [$logXml]")
    }
  }

  def setRootLevel(level: String): Unit = {
    if (!StringUtils.isEmpty(level)) {
      Configurator.setRootLevel(Level.valueOf(level))
      logger.info(s"Already set logLevel to [$level]")
    }
  }
}
