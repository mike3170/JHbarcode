package com.stit.jhbarcode.model;

// query data form
// 1. remove db
// 2. local sqlirt
public enum  DataSourceKind {
    LOCAL("local"),
    REMOATE("remote");

    private String value;

    DataSourceKind(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}