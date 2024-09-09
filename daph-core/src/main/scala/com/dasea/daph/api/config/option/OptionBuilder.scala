package com.dasea.daph.api.config.option

case class OptionBuilder(key: String) {
  private var _doc = ""
  private var _public = true
  private var _version = ""

  def doc(s: String): OptionBuilder = {
    _doc = s
    this
  }

  def version(v: String): OptionBuilder = {
    _version = v
    this
  }

  def default(defaultValue: String = "Default"): ConfigOption = {
    ConfigOption(key, defaultValue, _doc, _public, _version)
  }

  def internal(): OptionBuilder = {
    _public = false
    this
  }
}
