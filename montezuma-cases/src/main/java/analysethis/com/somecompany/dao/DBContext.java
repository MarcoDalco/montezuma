package analysethis.com.somecompany.dao;

import java.sql.PreparedStatement;

public class DBContext {
	private String	value;

	public DBContext(String s) {
		this(s, "ON");
		setValue(this.value + " AND OFF");
	}

	public DBContext setValue(String string) {
		this.value = string;
		return this;
	}

	public DBContext(String s, String string) {
		this.value = s + string;
	}

	public static String getCompiledSql(@SuppressWarnings("unused") PreparedStatement preparedStatement) {
		return "This is the compiled statement";
	}

	public static String fromStaticToInstance(PreparedStatement preparedStatement) {
		return new DBContext("TURN ").value;
	}
}
