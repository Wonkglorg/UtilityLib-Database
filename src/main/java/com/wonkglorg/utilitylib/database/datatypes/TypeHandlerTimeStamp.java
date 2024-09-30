package com.wonkglorg.utilitylib.database.datatypes;

import com.wonkglorg.util.interfaces.functional.database.DataTypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TypeHandlerTimeStamp implements DataTypeHandler<Timestamp> {
    @Override
    public void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
        statement.setTimestamp(index, (Timestamp) value);
    }

    @Override
    public Timestamp getParameter(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getTimestamp(index);
    }

    @Override
    public Timestamp getParameter(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getTimestamp(columnName);
    }
}
