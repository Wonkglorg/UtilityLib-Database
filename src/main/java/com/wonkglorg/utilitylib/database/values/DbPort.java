package com.wonkglorg.utilitylib.database.values;

public record DbPort(String port) {
    @Override
    public String toString() {
        return port;
    }
}
