package com.wonkglorg.utilitylib.database.response;

@SuppressWarnings("unused")
public class DatabaseSingleObjResponse<T> extends DatabaseResponse {
	private final T data;

	public DatabaseSingleObjResponse(Exception exception, T data) {
		super(exception);
		this.data = data;
	}

	public T getData() {
		return data;
	}
}
