package com.wonkglorg.utilitylib.database.values;

public record DbUrl(String url) {
    @Override
    public String toString() {
        return url;
    }

}
