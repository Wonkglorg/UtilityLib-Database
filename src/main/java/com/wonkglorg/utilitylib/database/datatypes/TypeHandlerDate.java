package com.wonkglorg.utilitylib.database.datatypes;

import com.wonkglorg.util.interfaces.functional.database.DataTypeHandler;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TypeHandlerDate implements DataTypeHandler<Date> {
    @Override
    public void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
        statement.setDate(index, (Date) value);
    }

    @Override
    public Date getParameter(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getDate(index);
    }

    @Override
    public Date getParameter(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getDate(columnName);
    }
}
