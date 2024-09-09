package com.dasea.daph.api.config.option

object GlobalConfigOptions {
  /**
   * Job级
   */
  val DAPH_HOME = OptionBuilder("daph.home").default(System.getenv("DAPH_HOME"))
  val DAPH_ID = OptionBuilder("daph.id").default()
  val DAPH_INSTANCE_ID = OptionBuilder("daph.instance-id").default()
  val DAPH_NAME = OptionBuilder("daph.name").default()
  val DAPH_DESC = OptionBuilder("daph.description").default()
  val DAPH_USER = OptionBuilder("daph.user").default()
  val DAPH_LOG_LEVEL = OptionBuilder("daph.log.level").default("info")
  val DAPH_COMPUTATION_MODE = OptionBuilder("daph.computation.mode").default("batch")
  val DAPH_COMPUTER_TYPE = OptionBuilder("daph.computer.type").default("")
  val DAPH_DT_MAPPING_PATH = OptionBuilder("daph.dt.mapping-file.root-path").default(DAPH_HOME.default + "/conf/mapping")
  //  val DAPH_EXECUTOR_CONFIG_PATH = OptionBuilder("daph.executor.config-file.path").default(DAPH_HOME.default + "/executor.json")
  //  val DAPH_LOG_XML = OptionBuilder("daph.log-xml").default(DEFAULT_XML_LOG_XML)
  //  val DAPH_EXECUTION_MODE = OptionBuilder("daph.execution-mode").default("local")
  //  val DAPH_DISTRIBUTION_CONFIG_PATH = OptionBuilder("daph.distribution.config-file.path").default("")

  /**
   * DAG级
   */
  val DAPH_DAG_MODEL = OptionBuilder("daph.dag.model").default("dag")

  /**
   * 节点级
   */
  val NODE_DICTIONARY_PATH = OptionBuilder("daph.node.dictionary-file.path").default("")
  val NODE_JAR_ROOT_PATH = OptionBuilder("daph.node.jar-file.root-path").default(DAPH_HOME.default + "/jars/nodes")
}
