package com.wonkglorg.utilitylib.database.values;

public record DbPassword(String password) {
    @Override
    public String toString() {
        return password;
    }
}
