package com.dasea.daph.api

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}

object GR {
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
  @JsonSubTypes(
    Array(
      new JsonSubTypes.Type(value = classOf[RunSucceed], name = "RunSucceed"),
      new JsonSubTypes.Type(value = classOf[RunFailed], name = "RunFailed")))
  sealed trait GlobalResult extends Serializable

  sealed trait Succeed extends GlobalResult

  sealed trait Failed extends GlobalResult

  case class RunSucceed() extends Succeed

  final case class RunFailed(e: Throwable) extends Failed

  case object StartSucceed extends Succeed

  final case class StartFailed(e: Throwable) extends Failed

  case object StopSucceed extends Succeed

  final case class StopFailed(e: Throwable) extends Failed

  final case class Timeout(e: Throwable) extends Failed

}