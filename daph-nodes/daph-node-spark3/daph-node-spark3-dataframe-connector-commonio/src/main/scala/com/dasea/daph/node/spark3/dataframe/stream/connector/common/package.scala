package com.dasea.daph.node.spark3.dataframe.stream.connector

import org.apache.spark.sql.streaming.Trigger

package object common {
  def getTrigger(triggerType: String, triggerTime: String): Trigger = triggerType match {
    case "ProcessingTime" => Trigger.ProcessingTime(triggerTime)
    case "Continuous" => Trigger.Continuous(triggerTime)
    case "Once" => Trigger.Once
  }
}
