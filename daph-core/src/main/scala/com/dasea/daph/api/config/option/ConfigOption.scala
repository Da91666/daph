package com.dasea.daph.api.config.option

case class ConfigOption(
  key: String,
  default: String,
  doc: String,
  isPublic: Boolean = true,
  version: String = "1.0.0"
) {
  override def toString: String = {
    s"ConfigOption(key=$key, defaultValue=$default, doc=$doc, " +
      s"public=$isPublic, version=$version)"
  }
}
