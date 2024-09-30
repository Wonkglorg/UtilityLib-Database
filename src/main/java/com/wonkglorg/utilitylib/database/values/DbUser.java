package com.wonkglorg.utilitylib.database.values;

public record DbUser(String user) {
    @Override
    public String toString() {
        return user;
    }
}
