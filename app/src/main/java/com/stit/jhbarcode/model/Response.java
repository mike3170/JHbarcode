package com.stit.jhbarcode.model;

public class Response {
  public static enum Status {
    OK,
    ERROR
  }
  public Status status;
  public Error error;

  public class Error {
    public int code;
    public String desc;

    @Override
    public String toString() {
      return "Info{" +
          "code=" + code +
          ", desc='" + desc + '\'' +
          '}';
    }
  }

}
