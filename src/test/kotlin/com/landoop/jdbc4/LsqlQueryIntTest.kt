package com.landoop.jdbc4

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.WordSpec
import java.sql.DriverManager

class LsqlQueryIntTest : WordSpec() {

  init {

    LsqlDriver()

    "JDBC Driver" should {
      "support wildcard selection" {
        val q = "SELECT * FROM `cc_payments` WHERE _vtype='AVRO' AND _ktype='STRING'"
        val conn = DriverManager.getConnection("jdbc:lsql:kafka:http://localhost:3030", "admin", "admin")
        val stmt = conn.createStatement()
        val rs = stmt.executeQuery(q)
        rs.metaData.columnCount shouldBe 6
        rs.metaData.getColumnLabel(1) shouldBe "id"
        rs.metaData.getColumnLabel(2) shouldBe "time"
        rs.metaData.getColumnLabel(3) shouldBe "amount"
        rs.metaData.getColumnLabel(4) shouldBe "currency"
        rs.metaData.getColumnLabel(5) shouldBe "creditCardId"
        rs.metaData.getColumnLabel(6) shouldBe "merchantId"
      }
//      "support projections" {
//
//      }
//      "support where clauses"  {
//        val q = "SELECT * FROM `cc_payments` WHERE _vtype='AVRO' AND _ktype='STRING'  and currency= 'USD' LIMIT 1000"
//      }
//      "support limits"  {nn
//        val q = "SELECT * FROM `cc_payments` WHERE _vtype='AVRO' AND _ktype='STRING'  and currency= 'USD' LIMIT 1000"
//      }
      "return true for results" {
        val conn = DriverManager.getConnection("jdbc:lsql:kafka:http://localhost:3030", "admin", "admin")
        conn.createStatement().execute("select * from `cc_payments` WHERE _vtype='AVRO' AND _ktype='STRING' AND currency='USD'") shouldBe true
      }
      "return false if no results" {
        val conn = DriverManager.getConnection("jdbc:lsql:kafka:http://localhost:3030", "admin", "admin")
        conn.createStatement().execute("select * from `cc_payments` WHERE _vtype='AVRO' AND _ktype='STRING' AND currency='wibble'") shouldBe false
      }
    }
  }
}