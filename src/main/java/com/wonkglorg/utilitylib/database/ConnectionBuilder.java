package com.wonkglorg.utilitylib.database;

import com.wonkglorg.util.database.values.*;

import java.util.Objects;

@SuppressWarnings("unused")
public class ConnectionBuilder {
	private String url;
	private String user;
	private String password;
	private String databaseName;
	private String port;
	private String driver;
	private String fullConnectionString;

	private String pattern = "%driver%://%url%:%port%/%database%";

	//todo:jmd add descriptions
	public ConnectionBuilder(String driver, String url, String user, String password,
			String databaseName, String port) {
		this.driver = driver;
		this.url = url;
		this.user = user;
		this.password = password;
		this.databaseName = databaseName;
		this.port = port;
	}

	public ConnectionBuilder(String driver, String url, String user, String password,
			String databaseName) {
		this(driver, url, user, password, databaseName, null);
	}

	public ConnectionBuilder(String driver, String url, String user, String password) {
		this(driver, url, user, password, null);
	}

	public ConnectionBuilder(String fullConnectionString) {
		this.fullConnectionString = fullConnectionString;
	}


	public ConnectionBuilder() {
	}

	public String build() {
		return Objects.requireNonNullElseGet(fullConnectionString,
				() -> pattern.replace("%driver%", driver).replace("%url%", url).replace("%port%", port)
						.replace("%database%", databaseName).replace("%user%", user)
						.replace("%password%", password));
	}

	public ConnectionBuilder url(String url) {
		this.url = url;
		return this;
	}

	/**
	 * Set User to use for connection
	 *
	 * @return {@link ConnectionBuilder}
	 */
	public ConnectionBuilder user(String user) {
		this.user = user;
		return this;
	}

	/**
	 * Set Password to use for connection
	 *
	 * @return {@link ConnectionBuilder}
	 */
	public ConnectionBuilder password(String password) {
		this.password = password;
		return this;
	}

	/**
	 * Represents Schema name
	 *
	 * @return {@link ConnectionBuilder}
	 */
	public ConnectionBuilder databaseName(String name) {
		this.databaseName = name;
		return this;
	}


	/**
	 * Sets the port
	 *
	 * @return {@link ConnectionBuilder}
	 */
	public ConnectionBuilder port(String port) {
		this.port = port;
		return this;
	}

	/**
	 * Sets the pattern used to construct the connection string
	 * default("%driver%://%url%:%port%/%database%")
	 *
	 * @return {@link ConnectionBuilder}
	 */
	public ConnectionBuilder pattern(String pattern) {
		this.pattern = pattern;
		return this;
	}

	/**
	 * Sets the driver to use
	 *
	 * @return {@link ConnectionBuilder}
	 */
	public ConnectionBuilder driver(String driver) {
		this.driver = driver;
		return this;
	}

	public DbUrl getUrl() {
		return Db.url(url);
	}

	public DbUser getUser() {
		return Db.user(user);
	}

	public DbPassword getPassword() {
		return Db.password(password);
	}

	public DbName getDatabaseName() {
		return Db.name(databaseName);
	}

	public DbPort getPort() {
		return Db.port(port);
	}

	public String getDriver() {
		return driver;
	}
}
