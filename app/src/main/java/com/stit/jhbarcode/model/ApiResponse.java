package com.stit.jhbarcode.model;

public class ApiResponse<T> {
  public static enum Status {
    OK,
    ERROR
  }
  public Status status;
  public T data;
  public Error error;

  public class Error{
    public int code;
    public String desc;
  }

  @Override
  public String toString() {
    return "ApiResponse{" +
            "status=" + status +
            ", data=" + data +
            ", error=" + error +
            '}';
  }
}
