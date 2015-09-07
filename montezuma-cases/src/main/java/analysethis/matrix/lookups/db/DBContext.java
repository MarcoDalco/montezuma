package analysethis.matrix.lookups.db;

//
//import java.sql.PreparedStatement;
//
//public class DBContext {
//	private String	value;
//
//	public DBContext(String s) {
//		this(s, "ON");
//		setValue(this.value + " AND OFF");
//	}
//
//	public DBContext setValue(String string) {
//		this.value = string;
//		return this;
//	}
//
//	public DBContext(String s, String string) {
//		this.value = s + string;
//	}
//
//	public static String getCompiledSql(@SuppressWarnings("unused") PreparedStatement preparedStatement) {
//		return "This is the compiled statement";
//	}
//
//	public static String fromStaticToInstance(PreparedStatement preparedStatement) {
//		return new DBContext("TURN ").value;
//	}
//}
//package analysethis.matrix.lookups.db;

import com.mdsol.ctms.dao.AuditsDAO;
import com.mdsol.ctms.maudit.MAuditCreator;
import com.mdsol.ctms.maudit.MAuditPreprocessor;
import com.mdsol.ctms.maudit.MAuditState;
import com.mdsol.ctms.rest.BaseRestlet;

import uhuru.bizx.dyna.crud.BaseCrud;
import uhuru.bizx.security.SVUser;
import uhuru.logging.ILoglet;
import uhuru.logging.LogFactory;
import uhuru.matrix.LoggingObject;
import uhuru.matrix.Matrix;
import uhuru.matrix.lookups.LookupProperty;
import uhuru.matrix.lookups.db.CachedRows;
import uhuru.matrix.lookups.db.ChangeType;
import uhuru.matrix.lookups.db.DB2XMLAdapterDefault;
import uhuru.matrix.lookups.db.FieldAudit;
import uhuru.matrix.lookups.db.IResultSetAdapter;
import uhuru.matrix.lookups.db.LookupDB;
import uhuru.matrix.lookups.db.LookupSqlCode;
import uhuru.matrix.lookups.db.ResultSet2JSONAdapter;
import uhuru.matrix.lookups.db.Row;
import uhuru.matrix.lookups.email.LookupEmail;
import uhuru.matrix.services.Form;
import uhuru.matrix.services.ServiceContext;
import uhuru.matrix.services.ServiceException;
import uhuru.matrix.services.VmContext;
import uhuru.matrix.services.security.ServiceUser;
import uhuru.matrix.utils.IntrospectUtil;
import uhuru.matrix.utils.helpers.DateFormatter;
import uhuru.matrix.utils.pagination.db.AbstractDbPaginator;
import uhuru.matrix.utils.pagination.db.DbPaginatorFactory;
import uhuru.matrix.utils.pagination.db.DbPagingData;
import uhuru.utils.Dom4JUtil;
import uhuru.utils.StringUtil;

import org.apache.velocity.VelocityContext;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class gives access to the underlying database
 */
public class DBContext extends LoggingObject implements Cloneable {

	private String							_id											= "";
	private String							_sql										= "";
	private String							_tableName							= "";

	private String							_placeHolder						= "?";
	private String							_outPutformat						= null;

	private CachedRows					_rows										= null;
	private ResultSetMetaData		_metaData								= null;
	private int									totalRowCount						= -1;
	private ServiceContext			_context								= null;
	private AbstractDbPaginator	_paginator							= null;

	private boolean							_logHistory							= true;

	private IResultSetAdapter		_adapter								= null;
	private static final String	ORDER_BY								= "order by";

	private List<String>				fieldNames;
	private List<String>				fieldValues;
	private Map<String, String>	fieldsPreviousValues;
	private Element							tableElement;

	private static String				NO_CONNECTION_AVAILABLE	= "DBC-9901: No connection available. Review database server status for app: " + Matrix.getAppName();

	public DBContext() {
		super();
		_logHistory = LookupDB.isLogHistory();
		setLogName("dbContext");
	}

	public DBContext(String sql) {
		this();
		setSql(sql);
	}

	public DBContext(String sql, List<String> fieldNames, List<String> fieldValues, Map<String, String> fieldsPreviousValues, Element tableElement) {
		this(sql);
		this.fieldValues = fieldValues;
		this.fieldNames = fieldNames;
		this.fieldsPreviousValues = fieldsPreviousValues;
		this.tableElement = tableElement;
	}

	public DBContext append(String s) {
		setSql(getSql() + " " + s);
		return this;
	}

	/**
	 * will replace the ? with the respective item in the list in the sql
	 *
	 * @param params
	 */
	public DBContext applyParams(List<String> params) throws Exception {
		String sql = getSql();
		for (int i = 0; i < params.size(); i++) {
			sql = StringUtil.replaceOnce(sql, _placeHolder, StringUtil.encodeForSql((String) params.get(i), Matrix.getProperty("escapeChar", "\\")));
		}
		return setSql(sql);
	}

	/**
	 * will replace the {key} with the respective item in the list in the sql The key is the key to the map entry Note ...
	 * change this algorithm? to parse for {} place holders
	 *
	 * @param params
	 */
	public DBContext applyParams(Map<String, String> params) throws Exception {
		String sql = getSql();
		for (Iterator<String> iter = params.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();

			Object val = params.get(key);
			if (val == null) {
				continue;
			}

			sql = StringUtil.replace(sql, "{" + key + "}", StringUtil.encodeForSql(val.toString(), Matrix.getProperty("escapeChar", "\\")));
		}
		return setSql(sql);
	}

	/**
	 * will replace the next ? placeholder with the param value
	 *
	 * @param param
	 */
	public DBContext applyParam(String param) {
		return setSql(StringUtil.replaceOnce(getSql(), _placeHolder, StringUtil.encodeForSql(param, Matrix.getProperty("escapeChar", "\\"))));
	}

	/**
	 * will replace the all {key} placeholder with the param value
	 *
	 * @param param
	 */
	public DBContext applyParam(String key, String param) {
		return setSql(getSql().replaceAll("\\{" + key + "\\}", StringUtil.encodeForSql(param, Matrix.getProperty("escapeChar", "\\"))));
	}

	public String getSql() {
		return _sql;
	}

	public DBContext setSql(String string) {
		_sql = string;
		return this;
	}

	public CachedRows getRows() {
		return _rows;
	}

	public DBContext setRows(CachedRows rows) {
		_rows = rows;
		return this;
	}

	public int getTotalRowCount() {
		return totalRowCount;
	}

	public void setTotalRowCount(int totalRowCount) {
		this.totalRowCount = totalRowCount;
	}

	public int getRowCount() {
		try {
			return getRows().getRows().size();
		}
		catch (Throwable e) {
			return 0;
		}
	}

	public List<Row> getRowsList() {
		try {
			return getRows().getRows();
		}
		catch (Throwable e) {
			return new ArrayList<Row>();
		}
	}

	public Object clone() throws CloneNotSupportedException {
		DBContext result = new DBContext();
		result.setSql(getSql());
		return result;
	}

	protected String getId() {
		return _id;
	}

	public DBContext setId(String id) {
		_id = id;
		return this;
	}

	/**
	 * executes insert/update query
	 */
	public int executeUpdate(ServiceContext context, String tableName, Map<String, String> map) throws SQLException {
		return executeUpdate(context, tableName, null, map);
	}

	public int executeUpdate(ServiceContext context, String tableName, byte[] blobContent, Map<String, String> map) throws SQLException {
		return this.executeUpdate(context, tableName, blobContent, map, null, 0, true);
	}

	/**
	 * @param whether or not to explicitly commit the transaction. This is to bridge towards the Spring DAO mechanisms,
	 *          which automatically commit at the exit of a method decorated with @Transactional
	 */
	public int executeUpdate(ServiceContext context, String tableName, byte[] blobContent, Map<String, String> map, List<String> values, int blobIndex, boolean doCommit) throws SQLException {
		setContext(context);
		setTableName(tableName);
		return executeUpdate(blobContent, map, values, blobIndex, doCommit);
	}

	public int executeUpdate(Map<String, String> map) throws SQLException {
		return executeUpdate(null, map);
	}

	public int executeUpdate() throws SQLException {
		return executeUpdate(null);
	}

	public int executeUpdate(byte[] blobContent, Map<String, String> map) throws SQLException {
		return this.executeUpdate(blobContent, map, null, 0, true);
	}

	/**
	 * @param whether or not to explicitly commit the transaction. This is to bridge towards the Spring DAO mechanisms,
	 *          which automatically commit at the exit of a method decorated with @Transactional
	 */
	public int executeUpdate(byte[] blobContent, Map<String, String> map, List<String> values, int blobIndex, boolean doCommit) throws SQLException {
		int result = -1;
		Connection con = getConnection();
		if (null == con) {
			error(NO_CONNECTION_AVAILABLE + " ::: Trying to run SQL: " + getSql());
			return -99;
		}

		try {
			PreparedStatement stmnt = con.prepareStatement(getSql());

			if (values != null) {
				for (int i = 0; i < values.size(); i++) {
					if (blobIndex == i + 1) {
						continue;
					}
					String value = values.get(i);
					stmnt.setString(i + 1, value);
				}
			}

			String partiallySetSql = getCompiledSql(stmnt);

			if (blobContent != null) {
				stmnt.setBytes(blobIndex, blobContent);
			}
			result = stmnt.executeUpdate();

			stmnt.close();

			if (result > 0) {
				this.setSql(partiallySetSql);
				logHistory(con, map);
				if (MAuditState.isMAuditSystemPropertyEnabled() && MAuditState.isTableTrackedByMAudit(tableElement)) {

					MAuditPreprocessor mAuditPreprocessor = new MAuditPreprocessor(fieldNames, fieldValues);

					String userUUID = ((SVUser) getContext().getUser()).getUserRow().get("UUID");
					Map<String, FieldAudit> mAuditFieldsMap = mAuditPreprocessor.getMAuditFields(tableElement, fieldsPreviousValues, partiallySetSql);
					ChangeType changeType = ChangeType.fromQuery(partiallySetSql);

					new MAuditCreator().audit(con, userUUID, mAuditPreprocessor.getValue("UUID"), mAuditFieldsMap, changeType, tableElement);
				}
			}

			if (doCommit && !con.getAutoCommit()) {
				con.commit();
			}

			return result;
		}
		catch (Exception e) {
			try {
				if (con != null) {
					con.rollback();
				}
			}
			catch (Exception e1) {}

			if (e instanceof SQLException) {
				int errorCode = ((SQLException) e).getErrorCode();
				if (LookupSqlCode.isDuplicateError(e)) {
					error(" : " + getId() + " executeUpdate FAILED :SQL  code - " + errorCode + " Sql: " + getSql());
				} else {
					error(" : " + getId() + " executeUpdate FAILED :SQL  code - " + errorCode + " Sql: " + getSql(), e);
				}

				throw (SQLException) e;
			}

			error(" : " + getId() + " executeUpdate FAILED :sql: " + getSql(), e);

			return -1;
		}
		finally {
			releaseConnection(con);
		}
	}

	public static String getCompiledSql(PreparedStatement stmnt) {
		String statementWithImplInfo = stmnt.toString();
		String sqlPart = statementWithImplInfo.substring(1 + statementWithImplInfo.indexOf(":"));
		String partiallySetSql = sqlPart.trim();
		return partiallySetSql;
	}

	/**
	 * returns a db connection from pool
	 */
	protected Connection getConnection() {
		return LookupDB.getConnection();
	}

	/**
	 * returns a db connection to pool
	 */
	protected void releaseConnection(Connection con) {
		LookupDB.releaseConnection(con);
	}

	public List<Row> executeQuery() {
		return executeQuery(null);
	}

	private DBContext logQueryStart() {
		String uid = null;
		try {
			uid = ((ServiceUser) getContext().getUser()).get("resourceId");
		}
		catch (Exception e) {}

		info("executeQuery: " + (uid == null ? "" : ("UID:" + uid)) + " SQL-B4-QUERY: ID: " + getId() + " \nSQL: " + getSql());
		return this;
	}

	private int logQueryEnd(int rowCount) {
		try {
			info("END-SQL: ID: " + getId() + " Count: " + rowCount);
		}
		catch (Exception e) {}

		return rowCount;
	}

	private boolean isPaginationEnabled(String srvcVer) {
		if ("V1".equals(srvcVer)) {
			// A global switch exists to turn on database level pagination for V1 services.
			// If this property is not true (or undefined) then we won't apply DB level pagination for V1
			// services
			// and the system will fallback to the old legacy pagination implementation
			String dbPaginationEnabledForV1Services = LookupProperty.get("pagination.dbPaginateV1Services", "false");
			if (!"true".equals(dbPaginationEnabledForV1Services)) {
				return false;
			}

			// We determine if pagination is enabled first by looking at a flag set by velocity macros
			// that wish to implement pagination (v_paginationInContext),
			// and then by looking at a specific user determined flag that might indicate if pagination is
			// enabled or not for a specific instance of the macro (v_paginationPaginate).
			// The purpose of v_paginationInContext is to avoid processing sql statements that don't mean
			// to be processed at all
			String paginationInContextStr = null;
			String paginationEnabledStr = null;

			if (this.getContext() != null && this.getContext().getVelocityContext() != null) {
				paginationInContextStr = ((String) this.getContext().getVelocityContext().get("v_paginationInContext"));
				paginationEnabledStr = ((String) this.getContext().getVelocityContext().get("v_paginationPaginate"));
			}

			boolean paginationInContext = paginationInContextStr != null && "true".equals(paginationInContextStr.toLowerCase().trim());
			boolean paginationEnabled = paginationEnabledStr != null && "true".equals(paginationEnabledStr.toLowerCase().trim());
			return (paginationInContext && paginationEnabled);
		} else if ("V2".equals(srvcVer)) {
			String jtPageSizeStr = null;
			String jtStartIndexStr = null;
			if (this.getContext() != null && this.getContext().getForm() != null) {
				jtPageSizeStr = ((String) this.getContext().getForm().get("jtPageSize"));
				jtStartIndexStr = ((String) this.getContext().getForm().get("jtStartIndex"));
			}

			return ((jtPageSizeStr != null && jtStartIndexStr != null) && (!jtPageSizeStr.trim().isEmpty() && !jtStartIndexStr.trim().isEmpty()));
		}

		return false;
	}

	private String getFormParamStartWith(String prefix) {
		String result = null;

		if (getContext() != null && getContext().getForm() != null) {
			Form form = getContext().getForm();
			for (Object key : form.keySet()) {
				if (key instanceof String) {
					if (((String) key).startsWith(prefix)) {
						result = (String) form.get(key);
						break;
					}
				}
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Row> executeQuery(Element crossTab) {
		logQueryStart();

		Connection con = getConnection();
		if (null == con) {
			error(NO_CONNECTION_AVAILABLE + " ::: Trying to run SQL: " + getSql());
			return new ArrayList<Row>();
		}

		String offsetStr = null;
		String sizeStr = null;

		/*
		 * This block checks if the pagination is enabled for V1 services first and then for V2 services.
		 */
		boolean paginationEnabled = isPaginationEnabled("V1");
		if (paginationEnabled) {
			offsetStr = getFormParamStartWith("pagFrom");
			sizeStr = getFormParamStartWith("pagSize");
		} else {
			paginationEnabled = isPaginationEnabled("V2");
			if (paginationEnabled) {
				offsetStr = getFormParamStartWith("jtStartIndex");
				sizeStr = getFormParamStartWith("jtPageSize");
			}
		}

		if (paginationEnabled) {
			setPaginator(DbPaginatorFactory.getPaginatorInstance());

			try {
				_paginator.setConnection(con);

				int offset = 0;
				if (offsetStr != null && !offsetStr.trim().isEmpty()) {
					offset = Integer.parseInt(offsetStr);
				}

				int size = 10;
				if (sizeStr != null && !sizeStr.trim().isEmpty()) {
					size = Integer.parseInt(sizeStr);
				}

				DbPagingData pagingData = (DbPagingData) _paginator.getData(getSql(), offset, size);
				setTotalRowCount(pagingData.getTotalRowCount());

				List<Row> rows = pagingData.getData();
				CachedRows cachedRows = new CachedRows();
				cachedRows.setRows(rows);
				setRows(cachedRows);
				setMetaData(pagingData.getMetaData());

				this.getContext().getForm().put("dbPagination", "true");
				this.getContext().getForm().put("totalRowCount", getTotalRowCount());

				logQueryEnd(getRows().size());
				return getRows().getRows();
			}
			finally {
				_paginator.removeConnection();
			}
		}

		Statement stmnt = null;
		try {
			stmnt = con.createStatement();
			ResultSet rs = stmnt.executeQuery(getSql());
			setRows(new CachedRows(rs, crossTab));
			setMetaData(rs.getMetaData());
			rs.close();
			stmnt.close();
			logQueryEnd(getRows().size());
			return getRows().getRows();
		}
		catch (Throwable e) {
			error("Error occurred executing SQL statement: " + e);
			if (stmnt != null) {
				try {
					stmnt.close();
				}
				catch (Exception er) {}
			}
			return new ArrayList<Row>();
		}
		finally {
			releaseConnection(con);
		}
	}

	public byte[] getFileContent(String blobContentColumnName) throws Exception, SQLException {
		Connection con = null;
		try {
			con = getConnection();
			Statement stmnt = con.createStatement();
			ResultSet rs = stmnt.executeQuery(getSql());

			rs.next();
			byte[] bytes = null;
			Blob blobData = rs.getBlob(blobContentColumnName);
			if (blobData != null) {
				InputStream is = blobData.getBinaryStream();
				bytes = new byte[(int) blobData.length()];
				is.read(bytes);
			}

			rs.close();
			stmnt.close();
			return bytes;
		}
		catch (Throwable e) {
			sendErrorAlert(e);
			return null;
		}
		finally {
			releaseConnection(con);
		}
	}

	public Map<String, byte[]> getFileContentMap(String key, String blobContentColumnName) throws Exception, SQLException {
		Connection con = null;
		Map<String, byte[]> attachmentsInDB = new HashMap<String, byte[]>();
		try {
			con = getConnection();
			Statement stmnt = con.createStatement();
			ResultSet rs = stmnt.executeQuery(getSql());

			Blob blobData;
			InputStream is;
			byte[] fileDataBytes;

			while (rs.next()) {
				blobData = rs.getBlob(blobContentColumnName);
				if (blobData != null) {
					is = blobData.getBinaryStream();
					fileDataBytes = new byte[(int) blobData.length()];
					is.read(fileDataBytes);
					attachmentsInDB.put(rs.getString(key), fileDataBytes);
				}
			}

			rs.close();
			stmnt.close();
			return attachmentsInDB;
		}
		catch (Throwable e) {
			sendErrorAlert(e);
			return null;
		}
		finally {
			releaseConnection(con);
		}
	}

	/**
	 * This method expects a list of strings it then turns them into 1 string ready for an sql in clause so a list of pete
	 * tom john results in a string "'pete','tom','john'"
	 *
	 * @param list of strings
	 * @param the delimiter to use
	 * @return a list of Strings representing the values of each CustomerRef/BuyerID/IDCode where the buyerID,IDType =
	 *         IDType
	 */
	public String toInClause(List<String> list, String delimiter) throws ServiceException {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			String s = (String) list.get(i);
			if (i > 0) {
				buffer.append(delimiter);
			}
			buffer.append("'").append(s.trim()).append("'");
		}
		return buffer.toString();
	}

	/**
	 * must be called after executeQuery()
	 */
	public boolean isRowFound() {
		try {
			return (getRows().getRow(0) != null);
		}
		catch (Exception e) {
			return false;
		}
	}

	public String getPlaceHolder() {
		return _placeHolder;
	}

	public DBContext setPlaceHolder(String placeHolder) {
		_placeHolder = placeHolder;
		return this;
	}

	/**
	 * @param caption table name
	 * @return a DOM Document
	 * @throws Exception
	 */
	public Document query2Doc(String caption) throws Exception {
		return query2Doc(caption, true);
	}

	/**
	 * @param caption table name
	 * @return a DOM Document
	 * @throws Exception
	 */
	public Document query2Doc(String caption, boolean allowEmpty) throws Exception {
		return Dom4JUtil.unMarshall(new StringReader(query2Xml(caption, allowEmpty)));
	}

	public String query2Xml(String caption) throws Exception {
		return query2Xml(caption, true);
	}

	public String query2Xml(String caption, boolean allowEmpty) throws Exception {
		return query2Xml(caption, allowEmpty, false);
	}

	public String query2Xml(String caption, boolean allowEmpty, boolean setType) throws Exception {
		StringWriter w = new StringWriter();
		query2Xml(w, caption, allowEmpty, setType);
		return w.toString();
	}

	public void query2Xml(Writer w, String caption, boolean allowEmpty, boolean setType) throws Exception {
		Connection con = getConnection();
		if (null == con) {
			error(NO_CONNECTION_AVAILABLE + " ::: Trying to run SQL: " + getSql());
			return;
		}

		Statement stmnt = null;

		try {
			stmnt = con.createStatement();
			ResultSet rs = stmnt.executeQuery(getSql());
			rs2Xml(w, rs, caption, allowEmpty, setType);
		}
		catch (Exception e) {
			error("query2Xml failed: sql: " + getSql());
			error(e);
			throw e;
		}
		finally {
			releaseConnection(con);
		}
	}

	/**
	 * this method takes a result set and returns an xml representation
	 */
	public String rs2Xml(ResultSet rs, String caption) throws Exception {
		return rs2Xml(rs, caption, true);
	}

	/**
	 * this method takes a result set and returns an xml representation
	 */
	public String rs2Xml(ResultSet rs, String caption, boolean allowEmptyRows) throws Exception {
		return rs2Xml(rs, caption, allowEmptyRows, true);
	}

	/**
	 * this method takes a result set and returns an xml representation
	 */
	public String rs2Xml(ResultSet rs, String caption, boolean allowEmptyRows, boolean setType) throws Exception {
		StringWriter w = new StringWriter();
		rs2Xml(w, rs, caption, allowEmptyRows, setType);
		return w.toString();
	}

	/**
	 * this method takes a result set and writes an xml representation to the writer. null values should stay null, empty
	 * string are set to "null"
	 */
	public void rs2Xml(Writer w, ResultSet rs, String caption, boolean allowEmptyRows, boolean setType) throws Exception {
		logQueryStart();
		if (null == getAdapter()) {
			setAdapter(new DB2XMLAdapterDefault());
		}

		IResultSetAdapter adapter = getAdapter();
		adapter.adapt(w, rs, caption, allowEmptyRows, setType);
		logQueryEnd(adapter.getRowCount());
	}

	public void query2JSON(Writer w) throws Exception {
		query2JSON(w, null);
	}

	public int query2JSON(Writer w, String fields) throws Exception {

		info("DBC-Q2JSON-1001: writer=" + w + " :fields=" + fields);
		info("DBC-Q2JSON-1002: adapter=" + getAdapter());
		if (getAdapter() == null) {
			setAdapter(new ResultSet2JSONAdapter(w, fields));
		}

		IResultSetAdapter adapter = getAdapter();

		return doQuery(adapter);
	}

	@SuppressWarnings("unchecked")
	public int doQuery(IResultSetAdapter adapter) throws Exception {
		Connection con = getConnection();
		if (null == con) {
			error(NO_CONNECTION_AVAILABLE + " , SQL: " + getSql());
			return -99;
		}

		boolean paginationEnabled = isPaginationEnabled("V2");

		if (paginationEnabled) {
			setPaginator(DbPaginatorFactory.getPaginatorInstance());

			try {
				_paginator.setConnection(con);

				setTotalRowCount(_paginator.getRowCount(getSql()));

				String offsetStr = getFormParamStartWith("jtStartIndex");
				String sizeStr = getFormParamStartWith("jtPageSize");

				int offset = 0;
				if (offsetStr != null && !offsetStr.trim().isEmpty()) {
					offset = Integer.parseInt(offsetStr);
				}

				int size = 10;
				if (sizeStr != null && !sizeStr.trim().isEmpty()) {
					size = Integer.parseInt(sizeStr);
				}

				String sql = ((AbstractDbPaginator) _paginator).getWrappedSqlQueryForPagination(getSql(), offset, size);
				((AbstractDbPaginator) _paginator).runQuery(sql, adapter);

				this.getContext().getForm().put("totalRowCount", getTotalRowCount());

			}
			finally {
				_paginator.removeConnection();
			}

			return getTotalRowCount();
		}

		Statement stmnt = null;
		ResultSet rs = null;
		try {
			// support https://medidata.atlassian.net/browse/MCC-81366
			{
				ServiceContext context = getContext();
				if (context != null && BaseRestlet.ACTION_EDIT.equalsIgnoreCase(context.getParam(BaseRestlet.ACTION_MODE))) {
					context.setParam(BaseCrud.PARAM_RECORD_EDIT_DATE, new DateFormatter().getSqlDateTime());
				}
			}

			logQueryStart();
			stmnt = con.createStatement();
			rs = stmnt.executeQuery(getSql());
			return logQueryEnd(adapter.adapt(rs).getRowCount());
		}
		catch (Exception e) {
			error("DBC-DQ-9001: " + e + "\n" + getSql(), e);
			throw e;
		}
		finally {
			if (rs != null) {
				rs.close();
			}
			if (stmnt != null) {
				stmnt.close();
			}

			releaseConnection(con);
		}
	}

	public boolean isSqlSet() {
		return StringUtil.isNotEmpty(_sql);
	}

	protected DBContext logHistory(Connection con, Map<String, String> map) {
		if (!isLogHistory()) {
			return this;
		}

		audit(getContext(), getSql(), getTableName(), con, map);

		return this;
	}

	public ServiceContext getContext() {
		return _context;
	}

	public void setContext(ServiceContext context) {
		_context = context;
	}

	public String getTableName() {
		return _tableName;
	}

	public DBContext setTableName(String tableName) {
		_tableName = tableName;
		return this;
	}

	private DBContext audit(ServiceContext context, String sql, String table, Connection con, Map<String, String> map) {
		if ("history".equalsIgnoreCase(table)) {
			return this;
		}
		if ("delete_log".equalsIgnoreCase(table)) {
			return this;
		}

		try {
			if (LookupDB.isAuditToLog()) {
				writeAuditLog(context, sql, table);
			}
			if (isLogHistory()) {
				writeAuditSql(context, sql, table, con, map);
			}
		}
		catch (RuntimeException e) {
			getAuditLog().error("audit", e);
		}

		return this;
	}

	public DBContext writeAuditSql(ServiceContext context, String sql, String table, Map<String, String> map) {
		Connection con = getConnection();
		if (null == con) {
			error(NO_CONNECTION_AVAILABLE + " ::: writeAuditSql: " + table + " : " + sql);
			return this;
		}

		try {
			writeAuditSql(context, sql, table, con, map);
		}
		catch (Exception e) {
			error(" : " + getId() + " executeUpdate FAILED :sql: " + getSql(), e);
		}
		finally {
			releaseConnection(con);
		}

		return this;
	}

	public DBContext writeAuditSql(ServiceContext context, String sql, String table, final Connection con, Map<String, String> map) {
		final String userId = context == null ? "0" : context.getUserId();
		try {
			AuditsDAO dbAuditor = Matrix.getSpringBeanByName(AuditsDAO.NAME, AuditsDAO.class);

			String recordId = null;
			if (map != null) {
				recordId = map.get("id");
				if (StringUtil.isEmpty(recordId)) {
					recordId = map.get("ID");
				}
			}

			dbAuditor.auditSql(con, userId, sql, table, (recordId == null ? null : Long.valueOf(recordId)));
		}
		catch (Throwable e) {
			getAuditLog().error("log-history: history-sql: " + sql);
			getAuditLog().error("log-history: insert-sql: " + getSql());
			getAuditLog().error("log-history: err: ", e);
		}

		return this;
	}

	protected static void writeAuditLog(ServiceContext context, String sql, String table) {
		try {
			StringBuffer buf = new StringBuffer();
			buf.append(" [User] ").append(context == null ? "Unknown" : IntrospectUtil.get(context.getUser(), "userId"));
			buf.append(" [ResourceId] ").append(context == null ? "Unknown" : IntrospectUtil.get(context.getUser(), "id"));
			buf.append(" [Service] ").append(context == null ? "Unknown" : context.getServiceId());
			buf.append(" [Table] ").append(table);
			buf.append(" [Sql] ").append(sql);
			getAuditLog().info(buf.toString());
		}
		catch (RuntimeException e) {
			LogFactory.error("writeAuditLog", e);
		}
	}

	public static ILoglet getAuditLog() {
		return LogFactory.getLoglet(LookupDB.getAuditLogName());
	}

	public boolean isLogHistory() {
		return _logHistory;
	}

	public DBContext setLogHistory(boolean history) {
		_logHistory = history;
		return this;
	}

	public IResultSetAdapter getAdapter() {
		return _adapter;
	}

	public DBContext setAdapter(IResultSetAdapter adapter) {
		_adapter = adapter;
		if (_adapter != null) {
			_adapter.setServiceContext(_context);
		}
		return this;
	}

	public void toMap(Map<String, String> map) {
		for (int i = 0; i < getRows().size(); i++) {
			Row row = getRows().getRow(i);
			map.put(row.get("name"), row.get("value"));
		}
	}

	public void sendErrorAlert(Throwable e) {
		if ("email_content_rec".equals(_id)) // avoid looping on sql error
		{
			return;
		}

		if (e.getMessage().indexOf("Duplicate key") > -1) {
			return;
		}

		int eCode = -1;
		if (e instanceof SQLException) {
			eCode = ((SQLException) e).getErrorCode();
		}

		StringBuilder sb = new StringBuilder(wrap(getId()));
		sb.append(" FAILED: ");
		if (eCode != -1) {
			sb.append(" ErrorCode: ").append(eCode);
		}

		sb.append(" Error: ").append(e);

		sb.append(" \n<br/>Sql: ").append(getSql());

		error(sb.toString());

		if (eCode != 1062) {
			VmContext vc = new VmContext();
			vc.put("errorMsg", sb.toString());
			vc.put("appName", Matrix.getAppName());
		}
	}

	public void sendErrorAlert(String errMsg) {
		if ("email_content_rec".equals(_id)) // / avoid looping on sql error
		{
			try {
				VmContext vc = new VmContext();
				vc.put("errorMsg", errMsg);
				vc.put("appName", Matrix.getAppName());
				LookupEmail.sendEmail("sql_error", vc);
			}
			catch (Exception e1) {
				error(getFriendlyExceptionMsg(e1), e1);
			}
			return;
		}

	}

	public String getOutPutformat() {
		return _outPutformat;
	}

	public void setOutPutformat(String outPutformat) {
		_outPutformat = outPutformat;
	}

	public static String simpleFetch(String sql) {
		return new DBContext(sql).simpleFetch();
	}

	public String simpleFetch() {
		try {
			return simpleFetchNoCatchThrowable();
		}
		catch (Throwable e) {
			error(e);
		}
		return "";
	}

	public static String simpleFetchNoCatchThrowable(String sql) {
		return new DBContext(sql).simpleFetchNoCatchThrowable();
	}

	private String simpleFetchNoCatchThrowable() {
		List<Row> list = executeQuery();

		if ((list != null) && (list.size() > 0)) {
			return list.get(0).get(0);
		}

		return "";
	}

	/**
	 * method to run sqlId and return first col in first row
	 */
	public String simpleFetch(String sqlId, VelocityContext vc) {
		try {
			List<uhuru.matrix.lookups.db.Row> list = LookupDB.getSql(sqlId, vc).executeQuery();
			if (list.size() == 0) {
				return null;
			}
			return list.get(0).get(0);
		}
		catch (Exception e) {
			error("Database lookup error: " + sqlId + " " + e);
			return null;
		}
	}

	/**
	 * method to run sqlId and return first col in first row
	 */
	public Row simpleRowFetch(String sqlId, VelocityContext vc) {
		try {
			List<uhuru.matrix.lookups.db.Row> list = LookupDB.getSql(sqlId, vc).executeQuery();
			if (list.size() == 0) {
				return null;
			}
			return list.get(0);
		}
		catch (Exception e) {
			error("Database lookup error: " + sqlId, e);
			return null;
		}
	}

	public static Row simpleRow(String sql) {
		try {
			List<Row> list = new DBContext(sql).executeQuery();
			return list.get(0);
		}
		catch (Exception e) {
			return null;
		}
	}

	public void setMetaData(ResultSetMetaData _metaData) {
		this._metaData = _metaData;
	}

	public ResultSetMetaData getMetaData() {
		return _metaData;
	}

	public String[] getColNames() {
		ResultSetMetaData rm = getMetaData();
		if (rm == null) {
			return null;
		}
		try {
			int cc = rm.getColumnCount();
			if (cc < 1) {
				return null;
			}
			String[] cn = new String[cc];
			for (int i = 1; i <= cc; i++) {
				cn[i - 1] = rm.getColumnLabel(i);
			}
			return cn;
		}
		catch (SQLException e) {}
		return null;
	}

	public String getColNamesAsCsv(String delimiter) {
		ResultSetMetaData rm = getMetaData();
		if (rm == null) {
			return null;
		}
		try {
			int cc = rm.getColumnCount();
			if (cc < 1) {
				return null;
			}
			if (isEmpty(delimiter)) {
				delimiter = ",";
			}
			StringBuffer cn = new StringBuffer();
			for (int i = 1; i <= cc; i++) {
				if (i > 1) {
					cn.append(delimiter);
				}
				cn.append(rm.getColumnLabel((i)));
			}
			return cn.toString();
		}
		catch (SQLException e) {}
		return null;
	}

	/**
	 * helper method to override the order by clause
	 *
	 * @param sort_fields if empty the method returns without override.
	 * @return the DBContext instance
	 */
	public DBContext overrideOrderBy(String sort_fields) {
		if (isEmpty(sort_fields)) {
			return this;
		}

		try {
			String sql = _sql.toLowerCase();
			int n = sql.indexOf(ORDER_BY);
			if (n > -1) {
				_sql = _sql.substring(0, n - 1);
			}

			_sql += " ORDER BY " + sort_fields;
		}
		catch (Throwable e) {
			error(e);
		}

		return this;
	}

	/**
	 * helper method to change the order by clause
	 *
	 * @param sort_fields if empty the method returns without override.
	 * @return the DBContext instance
	 */
	public DBContext changeOrderBy(String sort_fields) {
		if (isEmpty(sort_fields)) {
			return this;
		}

		try {
			String sql = _sql.toLowerCase();
			int n = sql.indexOf(ORDER_BY);
			String user_chosen_sort_field = sort_fields.split(" ")[0].toLowerCase();

			String[] existing_order_by = {};

			if (n > -1) {
				existing_order_by = _sql.substring(n + ORDER_BY.length()).toLowerCase().trim().split(",");
				_sql = _sql.substring(0, n - 1);
			}

			StringBuilder orderBy = new StringBuilder(200);
			orderBy.append(sort_fields);
			for (String existing_sort : existing_order_by) {
				if (!existing_sort.trim().startsWith(user_chosen_sort_field)) {
					orderBy.append(",");
					orderBy.append(existing_sort);
				}
			}

			_sql += " " + ORDER_BY + " " + orderBy.toString();
		}
		catch (Throwable e) {
			error(e);
		}

		return this;
	}

	public void setPaginator(AbstractDbPaginator paginator) {
		this._paginator = paginator;
	}
}
