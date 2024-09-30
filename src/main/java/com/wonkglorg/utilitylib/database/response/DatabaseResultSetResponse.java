package com.wonkglorg.utilitylib.database.response;

import java.sql.ResultSet;

@SuppressWarnings("unused")
public class DatabaseResultSetResponse extends DatabaseResponse {
	private final ResultSet resultSet;

	public DatabaseResultSetResponse(Exception exception, ResultSet resultSet) {
		super(exception);
		this.resultSet = resultSet;
	}

	public ResultSet getResultSet() {
		return resultSet;
	}


}
