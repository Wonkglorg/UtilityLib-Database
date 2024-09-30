package com.wonkglorg.utilitylib.database;

import com.wonkglorg.util.database.datatypes.*;
import com.wonkglorg.util.database.exceptions.IncorrectTypeConversionException;
import com.wonkglorg.util.database.response.*;
import com.wonkglorg.util.interfaces.functional.checked.CheckedConsumer;
import com.wonkglorg.util.interfaces.functional.checked.CheckedFunction;
import com.wonkglorg.util.interfaces.functional.database.DataTypeHandler;
import com.wonkglorg.util.ip.IPv4;
import com.wonkglorg.util.ip.IPv6;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.sql.Date;
import java.sql.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.wonkglorg.util.string.StringUtils.format;

/**
 * @author Wonkglorg
 * <p>
 * Base class for databases
 */
@SuppressWarnings("unused")
public abstract class Database implements AutoCloseable {
	protected final String driver;
	protected final String classloader;
	protected final Logger logger = Logger.getLogger(Database.class.getName());
	private static final Map<Class<?>, DataTypeHandler<?>> dataTypeMapper = new HashMap<>();

	static {
		dataTypeMapper.put(Blob.class, new TypeHandlerBlob());
		dataTypeMapper.put(Boolean.class, new TypeHandlerBoolean());
		dataTypeMapper.put(boolean.class, new TypeHandlerBoolean());
		dataTypeMapper.put(Byte.class, new TypeHandlerByte());
		dataTypeMapper.put(byte.class, new TypeHandlerByte());
		dataTypeMapper.put(byte[].class, new TypeHandlerByteArray());
		dataTypeMapper.put(Character.class, new TypeHandlerChar());
		dataTypeMapper.put(char.class, new TypeHandlerChar());
		dataTypeMapper.put(Date.class, new TypeHandlerDate());
		dataTypeMapper.put(Double.class, new TypeHandlerDouble());
		dataTypeMapper.put(double.class, new TypeHandlerDouble());
		dataTypeMapper.put(Float.class, new TypeHandlerFloat());
		dataTypeMapper.put(float.class, new TypeHandlerFloat());
		dataTypeMapper.put(Image.class, new TypeHandlerImage());
		dataTypeMapper.put(Integer.class, new TypeHandlerInteger());
		dataTypeMapper.put(int.class, new TypeHandlerInteger());
		dataTypeMapper.put(Long.class, new TypeHandlerLong());
		dataTypeMapper.put(long.class, new TypeHandlerLong());
		dataTypeMapper.put(Short.class, new TypeHandlerShort());
		dataTypeMapper.put(short.class, new TypeHandlerShort());
		dataTypeMapper.put(String.class, new TypeHandlerString());
		dataTypeMapper.put(Time.class, new TypeHandlerTime());
		dataTypeMapper.put(Timestamp.class, new TypeHandlerTimeStamp());
		dataTypeMapper.put(IPv4.class, new TypeHandlerIpv4());
		dataTypeMapper.put(IPv6.class, new TypeHandlerIpv6());
	}


	protected Database(@NotNull DatabaseType databaseType) {
		this.driver = databaseType.getDriver();
		this.classloader = databaseType.getClassLoader();
	}

	protected Database(@NotNull final String driver, @NotNull final String classLoader) {
		this.driver = driver;
		this.classloader = classLoader;
	}

	/**
	 * Small helper method to sanitize input for sql only does not other sanitizations like xss or
	 * html based
	 *
	 * @param input The input to sanitize
	 * @return The sanitized output
	 */
	public String sanitize(String input) {
		return input.replaceAll("[^a-zA-Z0-9]", "");
	}

	/**
	 * @return A database connection
	 */

	public abstract Connection getConnection();

	/**
	 * Fully disconnects the database connection
	 */
	public abstract void disconnect();

	/**
	 * Close the result set and the statement
	 *
	 * @param resultSet the result set to close
	 */
	protected void closeResources(ResultSet resultSet) {
		Statement statement = null;
		if (resultSet != null) {
			try {
				statement = resultSet.getStatement();
				resultSet.close();
			} catch (SQLException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	public CheckedFunction<ResultSet, Integer> singleIntAdapter() {
		return resultSet -> resultSet.getInt(1);
	}

	public CheckedFunction<ResultSet, String> singleStringAdapter() {
		return resultSet -> resultSet.getString(1);
	}

	public CheckedFunction<ResultSet, Boolean> singleBooleanAdapter() {
		return resultSet -> resultSet.getBoolean(1);
	}

	public CheckedFunction<ResultSet, Long> singleLongAdapter() {
		return resultSet -> resultSet.getLong(1);
	}

	public CheckedFunction<ResultSet, Double> singleDoubleAdapter() {
		return resultSet -> resultSet.getDouble(1);
	}

	public CheckedFunction<ResultSet, Float> singleFloatAdapter() {
		return resultSet -> resultSet.getFloat(1);
	}

	public CheckedFunction<ResultSet, Short> singleShortAdapter() {
		return resultSet -> resultSet.getShort(1);
	}

	public CheckedFunction<ResultSet, Byte> singleByteAdapter() {
		return resultSet -> resultSet.getByte(1);
	}


	/**
	 * Naps each row to its matching record class
	 *
	 * @param resultSet the result set to map
	 * @param adapter the adapter to map the result set to a record
	 * @param <T> the type of the record
	 * @return the list of records or null if an error occurred
	 */
	public <T extends Record> List<T> mapRecords(ResultSet resultSet,
			CheckedFunction<ResultSet, T> adapter) {
		try {
			List<T> list = new ArrayList<>();
			while (resultSet.next()) {
				list.add(adapter.apply(resultSet));
			}
			return list;
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}
	}


	/**
	 * Maps each row to a placeholder in the sql prepared statement
	 *
	 * @param record the record to map
	 * @param statement the statement to map the record to
	 * @param offset the offset to start (default:0)  starts at index 1
	 */
	public void recordToDatabase(Record record, PreparedStatement statement, int offset) {
		try {
			RecordComponent[] components = record.getClass().getRecordComponents();
			for (int i = 0; i < components.length; i++) {
				Object value = components[i].getAccessor().invoke(record);
				dataTypeMapper.get(value.getClass()).setParameter(statement, i + 1 + offset, value);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}


	/**
	 * Maps a record constructor to its matching sql columns (names MUST match, or it will not work)
	 * <p/>
	 * If any of the record columns do not have an adapter mapped a custom can be added / overwritten
	 * with {@link #addDataMapper(Class, DataTypeHandler)}
	 *
	 * @param recordClass the record class to map
	 * @param <T> the type of the record
	 * @return the adapter to convert the result set to a record
	 */
	protected <T extends Record> CheckedFunction<ResultSet, T> genericRecordAdapter(
			Class<T> recordClass, boolean useIndex, int offset) {
		return resultSet -> {
			Class<?> type = null;
			String columnName = null;
			try {
				RecordComponent[] components = recordClass.getRecordComponents();
				Object[] args = new Object[components.length];

				if (resultSet == null) {
					throw new SQLException("Result set is null");
				}

				for (int i = 0; i < components.length; i++) {
					RecordComponent component = components[i];
					columnName = component.getName();
					type = component.getType();
					var mappingFunction = dataTypeMapper.get(type);
					if (mappingFunction == null) {
						throw new NullPointerException(
								format("Data type {1} does not have a " + "valid mapping function", type));
					}

					if (useIndex) {
						args[i] = mappingFunction.getParameter(resultSet, i + 1 + offset);
					} else {
						args[i] = mappingFunction.getParameter(resultSet, columnName);
					}
				}
				try {
					return recordClass.getDeclaredConstructor(Arrays.stream(components)//
							.map(RecordComponent::getType)//
							.toArray(Class<?>[]::new)).newInstance(args);
				} catch (NoSuchMethodException e) {
					throw new RuntimeException(
							"Failed to create record: " + recordClass.getName() + " with args: "
									+ Arrays.toString(args), e);
				}

			} catch (SQLException e) {
				throw new IncorrectTypeConversionException(
						"Failed to map record components: type(" + type + ") referenceName(" + columnName +
								")",
						columnName, type, e);
			}
		};
	}

	/**
	 * Maps a record constructor to its matching sql columns (names MUST match, or it will not work)
	 * <p/>
	 * If any of the record columns do not have an adapter mapped a custom can be added / overwritten
	 * with {@link #addDataMapper(Class, DataTypeHandler)}
	 *
	 * @param recordClass the record class to map
	 * @param <T> the type of the record
	 * @return the adapter to convert the result set to a record
	 */
	public <T extends Record> CheckedFunction<ResultSet, T> recordAdapter(Class<T> recordClass) {
		return genericRecordAdapter(recordClass, false, 0);
	}

	/**
	 * Maps a record constructor to its matching sql columns (in index order constructor must match
	 * the order)
	 * <p/>
	 * If any of the record columns do not have an adapter mapped a custom can be added / overwritten
	 * with {@link #addDataMapper(Class, DataTypeHandler)}
	 *
	 * @param recordClass the record class to map
	 * @param <T> the type of the record
	 * @param offset the offset to start (default:0)  starts at index 1
	 * @return the adapter to convert the result set to a record
	 */
	public <T extends Record> CheckedFunction<ResultSet, T> recordIndexAdapter(Class<T> recordClass,
			int offset) {
		return genericRecordAdapter(recordClass, true, offset);
	}


	public <T> T getSingleObject(ResultSet resultSet, CheckedFunction<ResultSet, T> adapter) {
		try {
			if (resultSet.next()) {
				return adapter.apply(resultSet);
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Central method to create a blob from a byte array
	 *
	 * @param bytes the byte array to convert
	 * @return the blob
	 */
	public Blob createBlob(byte[] bytes) {
		try {
			Blob blob = getConnection().createBlob();
			blob.setBytes(1, bytes);
			return blob;
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Checks the current database the connection is connected to
	 *
	 * @return Gets the name of the database currently connected to
	 */
	public String checkCurrentDatabase(Connection connection) {

		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT DB_NAME() AS CurrentDB")) {
			if (rs.next()) {
				return rs.getString("CurrentDB");
			}
		} catch (Exception e) {
			throw new RuntimeException("Error logging action: " + e.getMessage());
		}
		return null;
	}

	public static byte[] convertToByteArray(BufferedImage image, String formatType)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, formatType, baos);
		return baos.toByteArray();
	}


	/**
	 * Executes the given query with a connection and automatically releases the connection after the
	 * query is done
	 *
	 * @param query the query to execute
	 */
	public abstract DatabaseResponse execute(CheckedConsumer<Connection> query);


	/**
	 * Executes the given query with a connection and automatically releases the connection after the
	 * query is done
	 *
	 * @param query the query to execute
	 * @return amount of rows affected
	 */
	public abstract DatabaseUpdateResponse executeUpdate(CheckedFunction<Connection, Integer> query);


	/**
	 * Executes the given query with a connection and automatically releases the connection after the
	 * query is done
	 *
	 * @param query the query to execute
	 * @param result the result of the query
	 * @return DatabaseResponse
	 */

	public abstract DatabaseUpdateResponse executeUpdate(
			CheckedFunction<Connection, PreparedStatement> query,
			CheckedFunction<PreparedStatement, Integer> result);

	/**
	 * Executes the given query with a connection and automatically releases the connection after the
	 * query is done
	 *
	 * @param query the query to execute
	 * @return the result of the query
	 */

	public abstract DatabaseResultSetResponse executeQuery(
			CheckedFunction<Connection, ResultSet> query);

	/**
	 * Executes the given query with a connection and automatically releases the connection after the
	 * query is done
	 *
	 * @param query the query to execute
	 * @param result the result of the query
	 * @return DatabaseResponse
	 */
	public abstract DatabaseResultSetResponse executeQuery(
			CheckedFunction<Connection, PreparedStatement> query,
			CheckedFunction<PreparedStatement, ResultSet> result);


	/**
	 * Executes the given query with a connection and automatically releases the connection after the
	 * query is done
	 *
	 * @param adapter the query to execute
	 * @param <T> the type of the object to return
	 * @return the result of the query
	 */
	public abstract <T> DatabaseObjResponse<T> executeObjQuery(
			CheckedFunction<Connection, List<T>> adapter);

	/**
	 * Executes the given query with a connection and automatically releases the connection after the
	 * query is done
	 *
	 * @param query the query to execute
	 * @param result the result of the query
	 * @param <T> the type of the object to return
	 * @return DatabaseResponse
	 */

	public abstract <T> DatabaseObjResponse<T> executeObjQuery(
			CheckedFunction<Connection, ResultSet> query, CheckedFunction<ResultSet, List<T>> result);

	/**
	 * Executes the given query with a connection and automatically releases the connection after the
	 * query is done
	 *
	 * @param adapter the adapter to convert the result to a single object
	 * @param <T> the type of the object to return
	 * @return the result of the query
	 */
	public abstract <T> DatabaseSingleObjResponse<T> executeSingleObjQuery(
			CheckedFunction<Connection, T> adapter);

	/**
	 * Executes the given query with a connection and automatically releases the connection after the
	 * query is done
	 *
	 * @param query the query to execute
	 * @param adapter the adapter to convert the result to a single object
	 * @param <T> the type of the object to return
	 * @return the result of the query
	 */
	public abstract <T> DatabaseSingleObjResponse<T> executeSingleObjQuery(
			CheckedFunction<Connection, ResultSet> query, CheckedFunction<ResultSet, T> adapter);

	/**
	 * Executes the given query with a connection and automatically releases the connection after the
	 * query is done
	 *
	 * @param query the query to execute
	 * @return the result of the query
	 */

	public abstract DatabaseResponse executeUnchecked(Consumer<Connection> query);

	/**
	 * Executes the given query with a connection and automatically releases the connection after the
	 * query is done not handle exceptions automatically
	 *
	 * @param query the query to execute
	 * @return amount of rows affected
	 */
	public abstract DatabaseUpdateResponse executeUpdateUnchecked(
			Function<Connection, Integer> query);

	/**
	 * Executes the given query with a connection and automatically releases the connection after the
	 * query is done not handle exceptions automatically
	 *
	 * @param query the query to execute
	 * @param result the result of the query
	 * @return DatabaseResponse
	 */
	public abstract DatabaseUpdateResponse executeUpdateUnchecked(
			Function<Connection, PreparedStatement> query, Function<PreparedStatement, Integer> result);

	/**
	 * Executes the given query with a connection and automatically releases the connection after the
	 * query is done not handle exceptions automatically
	 *
	 * @param query the query to execute
	 * @return the result of the query
	 */
	public abstract DatabaseResultSetResponse executeQueryUnchecked(
			Function<Connection, ResultSet> query);

	/**
	 * Executes the given query with a connection and automatically releases the connection after the
	 * query is done not handle exceptions automatically
	 *
	 * @param query the query to execute
	 * @param result the result of the query
	 * @return DatabaseResponse
	 */
	public abstract DatabaseResultSetResponse executeQueryUnchecked(
			Function<Connection, PreparedStatement> query,
			Function<PreparedStatement, ResultSet> result);

	/**
	 * Executes the given query with a connection and automatically releases the connection after the
	 * query is done does not handle exceptions automatically
	 *
	 * @param query the query to execute
	 * @param <T> the type of the object to return
	 * @return the result of the query
	 */
	public abstract <T> DatabaseObjResponse<T> executeObjQueryUnchecked(
			Function<Connection, List<T>> query);

	/**
	 * Executes the given query with a connection and automatically releases the connection after the
	 * query is done does not handle exceptions automatically
	 *
	 * @param query the query to execute
	 * @param adapter the adapter to convert the result to a list of objects
	 * @param <T> the type of the object to return
	 * @return the result of the query
	 */
	public abstract <T> DatabaseObjResponse<T> executeObjQueryUnchecked(
			Function<Connection, ResultSet> query, Function<ResultSet, List<T>> adapter);

	/**
	 * Executes the given query with a connection and automatically releases the connection after the
	 * query is done does not handle exceptions automatically
	 *
	 * @param adapter the adapter to convert the result to a single object
	 * @param <T> the type of the object to return
	 * @return the result of the query
	 */
	public abstract <T> DatabaseSingleObjResponse<T> executeSingleObjQueryUnchecked(
			Function<Connection, T> adapter);

	/**
	 * Executes the given query with a connection and automatically releases the connection after the
	 * query is done does not handle exceptions automatically
	 *
	 * @param query the query to execute
	 * @param adapter the adapter to convert the result to a single object
	 * @param <T> the type of the object to return
	 * @return the result of the query
	 */
	public abstract <T> DatabaseSingleObjResponse<T> executeSingleObjQueryUnchecked(
			Function<Connection, ResultSet> query, Function<ResultSet, T> adapter);

	/**
	 * @return the classloader path
	 */
	public String getClassLoader() {
		return classloader;
	}

	/**
	 * @return The database driver
	 */
	public String getDriver() {
		return driver;
	}


	/**
	 * Adds a data mapper function used in {@link #recordAdapter(Class)}
	 * and{@link #recordIndexAdapter(Class, int)} to map records to the correct type
	 *
	 * @param type the type to map
	 * @param handler mapper function
	 * @param <T> the type of the handler
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> DataTypeHandler<T> addDataMapper(Class<T> type, DataTypeHandler<T> handler) {
		return (DataTypeHandler<T>) dataTypeMapper.put(type, handler);
	}

	/**
	 * Removes a data mapper used in  {@link #recordAdapter(Class)} and
	 * {@link #recordIndexAdapter(Class, int)} to map records to the correct type
	 *
	 * @param type the type to remove
	 * @param <T> the type of the handler
	 * @return the removed handler
	 */
	@SuppressWarnings("unchecked")
	public static <T> DataTypeHandler<T> removeDataMapper(Class<T> type) {
		return (DataTypeHandler<T>) dataTypeMapper.remove(type);
	}

	public enum DatabaseType {
		MYSQL("Mysql", "jdbc:mysql:", "com.mysql.cj.jdbc.Driver"),
		SQLITE("Sqlite", "jdbc:sqlite:", "org.sqlite.JDBC"),
		POSTGRESQL("Postgresql", "jdbc:postgresql:", "org.postgresql.Driver"),
		SQLSERVER("SqlServer", "jdbc:sqlserver:", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),
		MARIA("MariaDB", "jdbc:mariadb:", "org.mariadb.jdbc.Driver");
		private final String driver;
		private final String classLoader;
		private final String name;

		DatabaseType(String name, String driver, String classLoader) {
			this.driver = driver;
			this.classLoader = classLoader;
			this.name = name;
		}

		public String getDriver() {
			return driver;
		}

		public String getClassLoader() {
			return classLoader;
		}

		public String getName() {
			return name;
		}
	}

}
