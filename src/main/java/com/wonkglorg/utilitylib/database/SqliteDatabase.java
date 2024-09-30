package com.wonkglorg.utilitylib.database;

import com.wonkglorg.util.database.response.*;
import com.wonkglorg.util.interfaces.functional.checked.CheckedConsumer;
import com.wonkglorg.util.interfaces.functional.checked.CheckedFunction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * @author Wonkglorg
 */
@SuppressWarnings("unused")
public class SqliteDatabase extends Database {
	protected final Path sourcePath;
	protected Connection connection;
	protected final Path destinationPath;
	protected final String databaseName;

	/**
	 * * Creates a Sqlite database at the specified copyToPath.
	 * * The sourcePath indicates where in the project the database file can be found, it will
	 * then be
	 * copied to the destinationPath destination.
	 * * If there is no database file it will be created at the destinationPath location.
	 * <br>
	 * !!IMPORTANT!!
	 * <br>Use <br>
	 * <pre>
	 *     {@code
	 * <plugin>
	 * 	<groupId>org.apache.maven.plugins</groupId>
	 * 	<artifactId>maven-resources-plugin</artifactId>
	 * 	<version>3.3.1</version>
	 * 	<configuration>
	 * 		<nonFilteredFileExtensions>
	 * 			<nonFilteredFileExtension>db</nonFilteredFileExtension>
	 * 		</nonFilteredFileExtensions>
	 * 	</configuration>
	 * </plugin>
	 * }
	 * </pre>
	 * otherwise sqlite database files will be filtered and become corrupted.
	 *
	 * @param sourcePath the original file to copy to a location
	 * @param destinationPath the location to copy to
	 */
	public SqliteDatabase(Path sourcePath, Path destinationPath) {
		super(DatabaseType.SQLITE);
		String name = destinationPath.getFileName().toString();
		databaseName = name.endsWith(".db") ? name : name + ".db";
		this.sourcePath = sourcePath;
		this.destinationPath = destinationPath;
		connect();
	}

	public SqliteDatabase(Path openInPath) {
		this(openInPath, openInPath);
	}

	/**
	 * Opens a new Connection to the database if non exists currently
	 */

	public void connect() {
		if (connection != null) {
			return;
		}

		try {
			Class.forName(getClassLoader());

			File databaseFile = destinationPath.toAbsolutePath().toFile();
			if (!databaseFile.exists()) {
				copyDatabaseFile(databaseFile);
			}
			String connectionString = getDriver() + destinationPath;
			connection = DriverManager.getConnection(connectionString);

		} catch (ClassNotFoundException | SQLException | IOException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	/**
	 * Copies the database file from the sourcePath to the destinationPath or creates a new file
	 * if it
	 * does not exist.
	 *
	 * @param databaseFile the file to copy to
	 */
	private void copyDatabaseFile(File databaseFile) throws IOException {
		try (InputStream resourceStream = getResource(sourcePath.toString())) {
			if (resourceStream != null) {
				Files.createDirectories(destinationPath.getParent());
				Files.copy(resourceStream, databaseFile.toPath());
			} else {
				boolean ignore = databaseFile.createNewFile();
			}
		}

	}

	private InputStream getResource(String filename) {
		if (filename == null) {
			throw new IllegalArgumentException("Filename cannot be null");
		}

		try {
			URL url = getClass().getClassLoader().getResource(filename.replace("\\\\", "/"));

			if (url == null) {
				return null;
			}

			URLConnection urlConnection = url.openConnection();
			urlConnection.setUseCaches(false);
			return urlConnection.getInputStream();
		} catch (IOException ex) {
			return null;
		}
	}


	@Override
	public void disconnect() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	@Override
	public DatabaseResponse execute(CheckedConsumer<Connection> query) {
		return executeUnchecked(query);
	}

	@Override
	public DatabaseUpdateResponse executeUpdate(CheckedFunction<Connection, Integer> query) {
		return executeUpdateUnchecked(query);
	}

	@Override
	public DatabaseUpdateResponse executeUpdate(CheckedFunction<Connection, PreparedStatement> query,
			CheckedFunction<PreparedStatement, Integer> result) {
		return executeUpdateUnchecked(query, result);
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

	@Override
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
		try {
			query.accept(getConnection());
			return new DatabaseResponse(null);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return new DatabaseResponse(e);
		}
	}

	@Override
	public DatabaseUpdateResponse executeUpdateUnchecked(Function<Connection, Integer> query) {
		try {
			return new DatabaseUpdateResponse(null, query.apply(getConnection()));
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return new DatabaseUpdateResponse(e, -1);
		}
	}

	@Override
	public DatabaseUpdateResponse executeUpdateUnchecked(
			Function<Connection, PreparedStatement> query, Function<PreparedStatement, Integer> result) {
		PreparedStatement statement = null;
		try {
			statement = query.apply(getConnection());
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
		}
	}

	@Override
	public DatabaseResultSetResponse executeQueryUnchecked(Function<Connection, ResultSet> query) {
		try {
			return new DatabaseResultSetResponse(null, query.apply(getConnection()));
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return new DatabaseResultSetResponse(e, null);
		}
	}

	@Override
	public DatabaseResultSetResponse executeQueryUnchecked(
			Function<Connection, PreparedStatement> query,
			Function<PreparedStatement, ResultSet> result) {
		try {
			PreparedStatement statement = query.apply(getConnection());
			return new DatabaseResultSetResponse(null, result.apply(statement));
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return new DatabaseResultSetResponse(e, null);
		}
	}

	@Override
	public <T> DatabaseObjResponse<T> executeObjQueryUnchecked(Function<Connection, List<T>> query) {
		try {
			return new DatabaseObjResponse<>(null, query.apply(getConnection()));
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return new DatabaseObjResponse<>(e, null);
		}
	}

	@Override
	public <T> DatabaseObjResponse<T> executeObjQueryUnchecked(Function<Connection, ResultSet> query,
			Function<ResultSet, List<T>> adapter) {
		ResultSet resultSet = null;
		try {
			resultSet = query.apply(getConnection());
			List<T> results = adapter.apply(resultSet);
			return new DatabaseObjResponse<>(null, results);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return new DatabaseObjResponse<>(e, null);
		} finally {
			closeResources(resultSet);
		}
	}

	@Override
	public <T> DatabaseSingleObjResponse<T> executeSingleObjQueryUnchecked(
			Function<Connection, T> adapter) {
		try {
			return new DatabaseSingleObjResponse<>(null, adapter.apply(getConnection()));
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return new DatabaseSingleObjResponse<>(e, null);
		}
	}

	@Override
	public <T> DatabaseSingleObjResponse<T> executeSingleObjQueryUnchecked(
			Function<Connection, ResultSet> query, Function<ResultSet, T> adapter) {
		ResultSet resultSet = null;
		try {
			resultSet = query.apply(getConnection());
			T results = adapter.apply(resultSet);
			return new DatabaseSingleObjResponse<>(null, results);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return new DatabaseSingleObjResponse<>(e, null);
		} finally {
			closeResources(resultSet);
		}
	}


	@Override
	public Connection getConnection() {
		connect();
		return connection;
	}

	@Override
	public void close() {
		disconnect();
	}
}

