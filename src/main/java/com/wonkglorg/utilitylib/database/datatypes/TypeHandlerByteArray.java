package com.wonkglorg.utilitylib.database.datatypes;

import com.wonkglorg.util.interfaces.functional.database.DataTypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TypeHandlerByteArray implements DataTypeHandler<byte[]> {
    @Override
    public void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
        statement.setBytes(index, (byte[]) value);
    }

    @Override
    public byte[] getParameter(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getBytes(index);
    }

    @Override
    public byte[] getParameter(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getBytes(columnName);
    }
}
