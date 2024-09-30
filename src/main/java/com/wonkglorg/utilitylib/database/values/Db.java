package com.wonkglorg.utilitylib.database.values;

public class Db {

    private Db() {
    }

    /**
     * Represents a database url
     */

    public static DbUrl url(String url) {
        return new DbUrl(url);
    }

    /**
     * Represents a database user
     */
    public static DbUser user(String user) {
        return new DbUser(user);
    }

    /**
     * Represents a database password
     */
    public static DbPassword password(String password) {
        return new DbPassword(password);
    }

    /**
     * Represents a database name
     */
    public static DbName name(String name) {
        return new DbName(name);
    }

    /**
     * Represents a database port
     */
    public static DbPort port(String port) {
        return new DbPort(port);
    }
}
