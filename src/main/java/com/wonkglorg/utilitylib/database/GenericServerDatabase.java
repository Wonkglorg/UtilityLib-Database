package com.wonkglorg.utilitylib.database;

import com.wonkglorg.util.database.response.*;
import com.wonkglorg.util.database.values.DbName;
import com.wonkglorg.util.interfaces.functional.checked.CheckedConsumer;
import com.wonkglorg.util.interfaces.functional.checked.CheckedFunction;

import java.sql.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class GenericServerDatabase extends Database {

	//todo rework database class to correctly handle connection strings from different database
	// types currently quite limited
	protected ConnectionBuilder builder;
	private final BlockingQueue<Connection> connectionPool;
	private DbName databaseName;


	public GenericServerDatabase(ConnectionBuilder builder, String driver, String classLoader,
			int poolSize) {
		super(driver, classLoader);
		this.builder = builder;
		connectionPool = new ArrayBlockingQueue<>(poolSize);
		initializeConnectionPool(poolSize);
	}

	public GenericServerDatabase(ConnectionBuilder builder, DatabaseType databaseType,
			int poolSize) {
		this(builder, databaseType.getDriver(), databaseType.getClassLoader(), poolSize);
	}

	/**
	 * Create a new GenericServerDatabase with a pool size of 3
	 *
	 * @param builder the connection builder
	 * @param databaseType the type of database
	 */
	public GenericServerDatabase(ConnectionBuilder builder, DatabaseType databaseType) {
		this(builder, databaseType, 3);
	}

	public GenericServerDatabase(ConnectionBuilder builder, String driver, String classLoader) {
		this(builder, driver, classLoader, 3);
	}

	/**
	 * @return a connection from the connection pool should be released after use manually
	 */
	@Override
	public Connection getConnection() {
		try {
			return connectionPool.take();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Release a connection back to the connection pool
	 *
	 * @param connection the connection to release
	 */
	public void releaseConnection(Connection connection) {
		connectionPool.offer(connection);
	}


	/**
	 * Resize the connection pool
	 *
	 * @param newSize the new size of the connection pool
	 */
	public void resizePool(int newSize) throws InterruptedException {
		if (newSize < 1) {
			throw new IllegalArgumentException("Pool size must be at least 1");
		}
		synchronized (connectionPool) {
			int currentSize = connectionPool.size();
			if (newSize < currentSize) {
				for (int i = newSize; i < currentSize; i++) {
					try {
						connectionPool.take().close();
					} catch (SQLException e) {
						logger.log(Level.SEVERE, e.getMessage(), e);
					}
				}
			} else if (newSize > currentSize) {
				for (int i = currentSize; i < newSize; i++) {
					connectionPool.add(createConnection());
				}
			}
		}
	}


	/**
	 * Disconnect from the database and close all connections
	 */
	@Override
	public void disconnect() {
		for (Connection connection : connectionPool) {
			try {
				connection.close();
			} catch (SQLException e) {
				System.out.println("Error closing connection: " + e.getMessage());
			}
		}
	}


	/**
	 * Initialize the connection pool
	 *
	 * @param poolSize the size of the connection pool
	 */
	private void initializeConnectionPool(int poolSize) {
		for (int i = 0; i < poolSize; i++) {
			connectionPool.add(createConnection());
		}
	}


	/**
	 * Use a specific database for a connection
	 *
	 * @param connection the connection to use the database on
	 * @param databaseName the name of the database to use
	 */
	public void useDatabase(Connection connection, DbName databaseName) {
		String name = sanitize(databaseName.toString());
		try (Statement statement = connection.createStatement()) {
			statement.execute("USE " + name);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Use a specific database for all connections
	 *
	 * @param databaseName the name of the database to use
	 */
	public void useDatabaseForAllConnections(String databaseName) {
		this.databaseName = new DbName(databaseName);
		for (Connection connection : connectionPool) {
			useDatabase(connection, this.databaseName);
		}
	}

	/**
	 * Helper Method to create a connection
	 *
	 * @return a new connection
	 */
	private Connection createConnection() {
		try {
			Class.forName(getClassLoader());
			return DriverManager.getConnection(builder.build());

		} catch (Exception e) {
			disconnect();
			throw new RuntimeException(e);
		}

	}

	/**
	 * Close all resources
	 */
	@Override
	public void close() {
		disconnect();
	}

	@Override
	public DatabaseResponse execute(CheckedConsumer<Connection> query) {
		return executeUnchecked(query);
	}

	@Override
	public DatabaseUpdateResponse executeUpdate(CheckedFunction<Connection, Integer> query) {
		return executeUpdateUnchecked(query);
	}


	public DatabaseUpdateResponse executeUpdate(CheckedFunction<Connection, PreparedStatement> query,
			CheckedFunction<PreparedStatement, Integer> result) {
		Connection connection = getConnection();
		try (PreparedStatement resultSet = query.apply(connection)) {
			return new DatabaseUpdateResponse(null, result.apply(resultSet));
		} catch (Exception e) {
			return new DatabaseUpdateResponse(e, -1);
		} finally {
			releaseConnection(connection);
		}
	}

	@Override
	public DatabaseResultSetResponse executeQuery(CheckedFunction<Connection, ResultSet> query) {
		return executeQueryUnchecked(query);
	}

	@Override
	public DatabaseResultSetResponse executeQuery(
			CheckedFunction<Connection, PreparedStatement> query,
			CheckedFunction<PreparedStatement, ResultSet> result) {
		return executeQueryUnchecked(query, result);
	}

	@Override
	public <T> DatabaseObjResponse<T> executeObjQuery(CheckedFunction<Connection, List<T>> adapter) {
		return executeObjQueryUnchecked(adapter);
	}


	@Override
	public <T> DatabaseObjResponse<T> executeObjQuery(CheckedFunction<Connection, ResultSet> query,
			CheckedFunction<ResultSet, List<T>> adapter) {
		return executeObjQueryUnchecked(query, adapter);
	}

	public <T> DatabaseSingleObjResponse<T> executeSingleObjQuery(
			CheckedFunction<Connection, T> adapter) {
		return executeSingleObjQueryUnchecked(adapter);
	}

	@Override
	public <T> DatabaseSingleObjResponse<T> executeSingleObjQuery(
			CheckedFunction<Connection, ResultSet> query, CheckedFunction<ResultSet, T> adapter) {
		return executeSingleObjQueryUnchecked(query, adapter);
	}

	@Override
	public DatabaseResponse executeUnchecked(Consumer<Connection> query) {
		Connection connection = getConnection();
		try {
			query.accept(connection);
			return new DatabaseResponse(null);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return new DatabaseResponse(e);
		} finally {
			releaseConnection(connection);
		}
	}

	@Override
	public DatabaseUpdateResponse executeUpdateUnchecked(Function<Connection, Integer> query) {
		Connection connection = getConnection();
		try {
			return new DatabaseUpdateResponse(null, query.apply(connection));
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return new DatabaseUpdateResponse(e, -1);
		} finally {
			releaseConnection(connection);
		}
	}

	@Override
	public DatabaseUpdateResponse executeUpdateUnchecked(
			Function<Connection, PreparedStatement> query, Function<PreparedStatement, Integer> result) {
		Connection connection = getConnection();
		PreparedStatement statement = null;
		try {
			statement = query.apply(connection);
			return new DatabaseUpdateResponse(null, result.apply(statement));
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return new DatabaseUpdateResponse(e, -1);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			}
			releaseConnection(connection);
		}
	}

	@Override
	public DatabaseResultSetResponse executeQueryUnchecked(Function<Connection, ResultSet> query) {
		Connection connection = getConnection();
		try {
			return new DatabaseResultSetResponse(null, query.apply(connection));
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return new DatabaseResultSetResponse(e, null);
		} finally {
			releaseConnection(connection);
		}
	}

	@Override
	public DatabaseResultSetResponse executeQueryUnchecked(
			Function<Connection, PreparedStatement> query,
			Function<PreparedStatement, ResultSet> result) {
		Connection connection = getConnection();
		try (var statement = query.apply(connection)) {
			return new DatabaseResultSetResponse(null, result.apply(statement));
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return new DatabaseResultSetResponse(e, null);
		} finally {
			releaseConnection(connection);
		}
	}

	@Override
	public <T> DatabaseObjResponse<T> executeObjQueryUnchecked(Function<Connection, List<T>> query) {
		Connection connection = getConnection();
		try {
			return new DatabaseObjResponse<>(null, query.apply(connection));
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return new DatabaseObjResponse<>(e, null);
		} finally {
			releaseConnection(connection);
		}
	}

	public <T> DatabaseObjResponse<T> executeObjQueryUnchecked(Function<Connection, ResultSet> query,
			Function<ResultSet, List<T>> adapter) {
		Connection connection = getConnection();
		ResultSet resultSet = null;
		try {
			resultSet = query.apply(connection);

			List<T> results = adapter.apply(resultSet);
			return new DatabaseObjResponse<>(null, results);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return new DatabaseObjResponse<>(e, null);
		} finally {
			closeResources(resultSet);
			releaseConnection(connection);
		}
	}

	@Override
	public <T> DatabaseSingleObjResponse<T> executeSingleObjQueryUnchecked(
			Function<Connection, T> adapter) {
		Connection connection = getConnection();
		try {
			return new DatabaseSingleObjResponse<>(null, adapter.apply(connection));
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return new DatabaseSingleObjResponse<>(e, null);
		} finally {
			releaseConnection(connection);
		}
	}

	@Override
	public <T> DatabaseSingleObjResponse<T> executeSingleObjQueryUnchecked(
			Function<Connection, ResultSet> query, Function<ResultSet, T> adapter) {
		Connection connection = getConnection();
		ResultSet resultSet = null;
		try {
			resultSet = query.apply(connection);
			T results = adapter.apply(resultSet);
			return new DatabaseSingleObjResponse<>(null, results);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return new DatabaseSingleObjResponse<>(e, null);
		} finally {
			closeResources(resultSet);
			releaseConnection(connection);
		}
	}
}
