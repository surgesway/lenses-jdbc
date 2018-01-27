package com.landoop.jdbc4

import com.landoop.rest.RestClient
import com.landoop.rest.domain.Credentials
import java.sql.Blob
import java.sql.CallableStatement
import java.sql.Clob
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.NClob
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.SQLFeatureNotSupportedException
import java.sql.SQLWarning
import java.sql.SQLXML
import java.sql.Savepoint
import java.sql.Statement
import java.sql.Struct
import java.util.*
import java.util.concurrent.Executor

class LsqlConnection(private val uri: String,
                     props: Properties) : Connection, AutoCloseable, Logging {

  private val user = props.getProperty("user") ?: throw SQLException("URI must specify username")
  private val password = props.getProperty("password", null) ?: throw SQLException("URI must specify password")
  private val weakSSL = props.getProperty("weakssl", "false").toBoolean()

  private val urls = uri.replace(Constants.JdbcPrefix, "").split(',').apply {
    if (this.isEmpty())
      throw SQLException("URI must specify at least one REST endpoint")
    if (!this.all { it.startsWith("http") || it.startsWith("https") })
      throw SQLException("Endpoints must use http or https")
    logger.debug("Connection will use urls $this")
  }

  private val client = RestClient(urls, Credentials(user, password), weakSSL)

  override fun rollback() = throw SQLFeatureNotSupportedException()
  override fun rollback(savepoint: Savepoint?) = throw SQLFeatureNotSupportedException()

  override fun getHoldability(): Int = ResultSet.CLOSE_CURSORS_AT_COMMIT

  override fun setNetworkTimeout(executor: Executor?, milliseconds: Int) {
  }

  override fun commit() = throw SQLFeatureNotSupportedException()

  override fun <T : Any?> unwrap(iface: Class<T>): T {
    try {
      return iface.cast(this)
    } catch (cce: ClassCastException) {
      throw SQLException("Unable to unwrap instance as " + iface.toString())
    }
  }

  override fun setTransactionIsolation(level: Int) {
    // noop in a  read only driver
  }

  override fun setAutoCommit(autoCommit: Boolean) {
    // noop in a  read only driver
  }

  override fun abort(executor: Executor?) {
    close()
  }

  override fun prepareCall(sql: String?): CallableStatement = throw SQLFeatureNotSupportedException()
  override fun prepareCall(sql: String?, resultSetType: Int, resultSetConcurrency: Int): CallableStatement = throw SQLFeatureNotSupportedException()
  override fun prepareCall(sql: String?, resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): CallableStatement = throw SQLFeatureNotSupportedException()

  override fun getClientInfo(name: String?): String? = null

  override fun getClientInfo(): Properties = Properties()

  override fun getAutoCommit(): Boolean = false

  override fun setCatalog(catalog: String?) {
    // javadoc requires no-op if not supported
  }

  override fun getWarnings(): SQLWarning? = null

  override fun getCatalog(): String? = null

  override fun setHoldability(holdability: Int) = throw SQLFeatureNotSupportedException()

  override fun getSchema(): String? = null

  // timeout is ignored, and the default timeout of the client is used
  override fun isValid(timeout: Int): Boolean = client.isValid()

  override fun close() {
    client.close()
  }

  override fun isClosed(): Boolean = client.isClosed

  override fun createArrayOf(typeName: String?, elements: Array<out Any>?): java.sql.Array =
      throw SQLFeatureNotSupportedException()

  override fun setReadOnly(readOnly: Boolean) {
    // always read only
  }

  override fun isWrapperFor(iface: Class<*>?): Boolean = metaData.isWrapperFor(iface)

  override fun nativeSQL(sql: String?): String = throw SQLFeatureNotSupportedException()

  override fun createStruct(typeName: String?, attributes: Array<out Any>?): Struct = throw SQLFeatureNotSupportedException()

  override fun setClientInfo(name: String?, value: String?) = throw SQLFeatureNotSupportedException()
  override fun setClientInfo(properties: Properties?) = throw SQLFeatureNotSupportedException()

  override fun releaseSavepoint(savepoint: Savepoint?) = throw SQLFeatureNotSupportedException()

  override fun createClob(): Clob = throw   SQLFeatureNotSupportedException()

  override fun isReadOnly(): Boolean = true

  override fun createStatement(): Statement {
    return LsqlStatement(this, client)
  }

  override fun createStatement(resultSetType: Int, resultSetConcurrency: Int): Statement =
      throw SQLFeatureNotSupportedException("ResultSet type and ResultSet concurrency are not supported, use the createStatement() function")


  override fun createStatement(resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): Statement =
      throw SQLFeatureNotSupportedException("ResultSet type and ResultSet concurrency are not supported, use the createStatement() function")

  override fun setSavepoint(): Savepoint = throw SQLFeatureNotSupportedException()
  override fun setSavepoint(name: String?): Savepoint = throw SQLFeatureNotSupportedException()

  override fun getTypeMap(): MutableMap<String, Class<*>> = throw SQLFeatureNotSupportedException()

  override fun clearWarnings() {
  }

  override fun getMetaData(): DatabaseMetaData {
    return LsqlDatabaseMetaData(this, client, uri, user)
  }

  override fun getTransactionIsolation(): Int = Connection.TRANSACTION_NONE

  override fun setSchema(schema: String?) {
    // javadoc requests noop for non-supported
  }

  override fun getNetworkTimeout(): Int = client.connectionRequestTimeout()

  override fun setTypeMap(map: MutableMap<String, Class<*>>?) = throw SQLFeatureNotSupportedException()

  // todo I think preparement statements could be supported
  // the ? syntax + auto escaping is very useful
  override fun prepareStatement(sql: String?): PreparedStatement = throw SQLFeatureNotSupportedException()

  override fun prepareStatement(sql: String?, resultSetType: Int, resultSetConcurrency: Int): PreparedStatement = throw SQLFeatureNotSupportedException()
  override fun prepareStatement(sql: String?, resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): PreparedStatement = throw SQLFeatureNotSupportedException()
  override fun prepareStatement(sql: String?, autoGeneratedKeys: Int): PreparedStatement = throw SQLFeatureNotSupportedException()
  override fun prepareStatement(sql: String?, columnIndexes: IntArray?): PreparedStatement = throw SQLFeatureNotSupportedException()
  override fun prepareStatement(sql: String?, columnNames: Array<out String>?): PreparedStatement = throw SQLFeatureNotSupportedException()

  override fun createNClob(): NClob = throw SQLFeatureNotSupportedException()
  override fun createBlob(): Blob = throw SQLFeatureNotSupportedException()
  override fun createSQLXML(): SQLXML = throw SQLFeatureNotSupportedException()
}