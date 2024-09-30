package com.wonkglorg.utilitylib.database.datatypes;

import com.wonkglorg.util.interfaces.functional.database.DataTypeHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class TypeHandlerImage implements DataTypeHandler<Image> {
    @Override
    public void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
        statement.setBinaryStream(index, getInputStreamFromImage((Image) value));
    }

    @Override
    public Image getParameter(ResultSet resultSet, int index) throws SQLException {
        return getImageFromBytes(resultSet.getBytes(index));
    }

    @Override
    public Image getParameter(ResultSet resultSet, String columnName) throws SQLException {
        return getImageFromBytes(resultSet.getBytes(columnName));
    }

    private Image getImageFromBytes(byte[] bytes) {
        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            Logger.getGlobal().warning("Failed to convert byte array to image: " + e.getMessage());
        }
        return null;
    }

    private InputStream getInputStreamFromImage(Image image) {
        try {
            return new ByteArrayInputStream(image.toString().getBytes());
        } catch (Exception e) {
            Logger.getGlobal().warning("Failed to convert image to byte array: " + e.getMessage());
        }
        return null;
    }
}
