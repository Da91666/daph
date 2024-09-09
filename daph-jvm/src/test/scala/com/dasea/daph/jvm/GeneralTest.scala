package com.dasea.daph.jvm

import org.junit.Test

class GeneralTest extends BaseTest {
  val job = rootPath + "/jobs/httpresult-s.json"

  @Test
  def test(): Unit = {
    executeJob(job)
  }
}
