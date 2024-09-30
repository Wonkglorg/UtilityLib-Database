package com.wonkglorg.utilitylib.database.datatypes;

import com.wonkglorg.util.interfaces.functional.database.DataTypeHandler;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TypeHandlerBlob implements DataTypeHandler<Blob> {
    @Override
    public void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
        statement.setBlob(index, (Blob) value);
    }

    @Override
    public Blob getParameter(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getBlob(index);
    }

    @Override
    public Blob getParameter(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getBlob(columnName);
    }
}
