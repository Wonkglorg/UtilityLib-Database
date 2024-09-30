package com.wonkglorg.utilitylib.database.response;

import java.util.List;

@SuppressWarnings("unused")
public class DatabaseObjResponse<T> extends DatabaseResponse {
	private final List<T> data;

	public DatabaseObjResponse(Exception exception, List<T> data) {
		super(exception);
		this.data = data;
	}

	public List<T> getData() {
		return data;
	}

}
