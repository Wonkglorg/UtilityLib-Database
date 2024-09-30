package com.wonkglorg.utilitylib.database;


/**
 * IMPORTANT! Please add the mysql Jconnector to the project if you want to use MySql, I did not include this myself to not inflate the libraries
 * size. groupId : mysql artifactId : mysql-connector-java
 */
@SuppressWarnings("unused")
public class MySqlDatabase extends GenericServerDatabase {


    public MySqlDatabase(ConnectionBuilder builder) {
        super(builder, DatabaseType.MYSQL);
    }
}
