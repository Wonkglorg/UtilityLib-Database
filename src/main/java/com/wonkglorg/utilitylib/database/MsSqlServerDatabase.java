package com.wonkglorg.utilitylib.database;

/**
 * IMPORTANT! Please add the Microsoft SqlServer Connector to the project if you want to use SqlServer.
 */
public class MsSqlServerDatabase extends GenericServerDatabase {

    public MsSqlServerDatabase(ConnectionBuilder builder, int poolSize) {
        super(builder, DatabaseType.SQLSERVER, poolSize);
    }
}
