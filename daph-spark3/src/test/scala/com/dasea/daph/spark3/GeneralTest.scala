package com.dasea.daph.spark3

import org.junit.Test

class GeneralTest extends BaseTest {
  val job = rootPath + "jobs/mysql-mysql-s.json"

  @Test
  def test(): Unit = {
    executeJob(job)
  }
}
