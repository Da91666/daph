package com.dasea.daph.tools.jsonloader

import com.dasea.daph.utils.JdbcUtil

import java.sql.{Connection, DriverManager}

class MySQLJsonLoader(config: Map[String, String]) extends JsonLoader {
  override def load(path: String): String = {
    val tableName = config("tableName")
    val id = config("id")

    JdbcUtil.usingConnection(conn) { conn =>
      conn.createStatement.executeQuery(s"SELECT $path FROM $tableName WHERE $id").getString(1)
    }
  }

  override def loadAll(jsonPath: DJson): DJson = {
    val tableName = config("tableName")
    val searchExpression = config("searchExpression")
    val path = s"${jsonPath.job},${jsonPath.computer},${jsonPath.storage},${jsonPath.executor}"

    val resultset = conn.createStatement.executeQuery(s"SELECT $path FROM $tableName WHERE $searchExpression")
    var jobJson = ""
    var computerJson = ""
    var storageJson = ""
    var executorJson = ""

    if (resultset.next()) {
      jobJson = resultset.getString(1)
      computerJson = resultset.getString(2)
      storageJson = resultset.getString(3)
      executorJson = resultset.getString(4)
    } else {
      throw new RuntimeException("It does not query the job json at db, the tableName is " + tableName)
    }

    // close resultset
    resultset.close()
    // close conn
    conn.close()

    DJson(jobJson, computerJson, storageJson, executorJson)
  }

  private def conn: Connection = {
    val url = config("url")
    val user = config("user")
    val password = config("password")

    val conn = DriverManager.getConnection(url, user, password)
    conn
  }
}
