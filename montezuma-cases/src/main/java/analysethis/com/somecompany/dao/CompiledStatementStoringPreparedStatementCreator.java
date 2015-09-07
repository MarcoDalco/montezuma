package analysethis.com.somecompany.dao;

import org.springframework.jdbc.core.PreparedStatementCreator;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CompiledStatementStoringPreparedStatementCreator implements PreparedStatementCreator, Serializable {
	private static final long	serialVersionUID	= -968449583310298283L;
	private final String			sql;
	private String						compiledSQL;

	public String getCompiledSQL() {
		return compiledSQL;
	}

	private final Object[]	params;

	public CompiledStatementStoringPreparedStatementCreator(String sql, Object... params) {
		this.sql = sql;
		this.params = params;
	}

	@Override
	public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
		final PreparedStatement preparedStatement = connection.prepareStatement(sql);
		int i = 1;
		for (Object param : params) {
			preparedStatement.setObject(i++, param);
		}
		compiledSQL = DBContext.getCompiledSql(preparedStatement);
		return preparedStatement;
	}
}
