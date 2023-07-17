package com.stit.jhbarcode.model;

public enum CodMastKind {
    AMRS("AMRS"),
    CLAS("CLAS");

    private String value;

    CodMastKind(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

