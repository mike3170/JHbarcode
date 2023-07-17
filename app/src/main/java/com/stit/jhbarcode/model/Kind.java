package com.stit.jhbarcode.model;

public enum  Kind {
    JobOrderPickup("1"),
    LocationMove("2"),
    MatReturn("3"),
    PicklingFinished("4"),
    PlatingReception("5");

    private String value;

    Kind(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

