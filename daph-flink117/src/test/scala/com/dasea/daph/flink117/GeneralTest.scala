package com.dasea.daph.flink117

import org.junit.Test

class GeneralTest extends BaseTest {
  val dbName = "mysql"
  val wayName = "cg cs cc sc cg_cc cc_cg cdc edb edb_cdc"
  val job = rootPath + "/jobs/pg/job-cg-edb_cdc-s.json"

  @Test
  def test(): Unit = {
    executeJob(job, 10)
  }
}
