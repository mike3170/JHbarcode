package com.stit.jhbarcode.model;

import java.util.Objects;

public class Pair {
  int seq;
  String barcode;

  public int getSeq() {
    return seq;
  }

  public void setSeq(int seq) {
    this.seq = seq;
  }

  public String getBarcode() {
    return barcode;
  }

  public void setBarcode(String barcode) {
    this.barcode = barcode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Pair pair = (Pair) o;

    if (seq != pair.seq) return false;
    return barcode != null ? barcode.equals(pair.barcode) : pair.barcode == null;
  }

  @Override
  public int hashCode() {
    int result = seq;
    result = 31 * result + (barcode != null ? barcode.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Pair{" +
        "seq=" + seq +
        ", barcode='" + barcode + '\'' +
        '}';
  }
}
