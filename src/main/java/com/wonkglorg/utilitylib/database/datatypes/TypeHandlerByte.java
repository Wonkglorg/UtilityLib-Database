package com.wonkglorg.utilitylib.database.datatypes;

import com.wonkglorg.util.interfaces.functional.database.DataTypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TypeHandlerByte implements DataTypeHandler<Byte> {
    @Override
    public void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
        statement.setByte(index, (Byte) value);
    }

    @Override
    public Byte getParameter(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getByte(index);
    }

    @Override
    public Byte getParameter(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getByte(columnName);
    }
}
