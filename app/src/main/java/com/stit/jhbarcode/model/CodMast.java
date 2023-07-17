package com.stit.jhbarcode.model;

public class CodMast {
  private String kind;
  private String codeNo;
  private String codeName;

  public CodMast() {
  }

  public CodMast(String kind, String codeNo, String codeName) {
    this.kind = kind;
    this.codeNo = codeNo;
    this.codeName = codeName;
  }

  public CodMast(String kind) {
    this.kind = kind;
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String kind) {
    this.kind = kind;
  }

  public String getCodeNo() {
    return codeNo;
  }

  public void setCodeNo(String codeNo) {
    this.codeNo = codeNo;
  }

  public String getCodeName() {
    return codeName;
  }

  public void setCodeName(String codeName) {
    this.codeName = codeName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CodMast codMast = (CodMast) o;

    return codeNo.equals(codMast.codeNo);
  }

  @Override
  public int hashCode() {
    return codeNo.hashCode();
  }

  @Override
  public String toString() {
    return "CodMast{" +
            "kind='" + kind + '\'' +
            ", codeNo='" + codeNo + '\'' +
            ", codeName='" + codeName + '\'' +
            '}';
  }
}
