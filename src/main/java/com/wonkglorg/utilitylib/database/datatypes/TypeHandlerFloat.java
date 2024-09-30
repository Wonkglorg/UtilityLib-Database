package com.wonkglorg.utilitylib.database.datatypes;

import com.wonkglorg.util.interfaces.functional.database.DataTypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TypeHandlerFloat implements DataTypeHandler<Float> {
    @Override
    public void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
        statement.setFloat(index, (Float) value);
    }

    @Override
    public Float getParameter(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getFloat(index);
    }

    @Override
    public Float getParameter(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getFloat(columnName);
    }
}
