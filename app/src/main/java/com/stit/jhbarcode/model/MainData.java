package com.stit.jhbarcode.model;

import java.util.Date;

public class MainData {
  private long id;
  private String procEmp;       // empno
  private String kind;          // 類型
  private String barCode;
  private String locate;        // 儲區
  private String scwJobNo;      // 螺絲批號(工令
  private Integer itemNo;        // 項次
  private String isrtType;      // 退回品質為 Y-良品 N-不良品
  private String reasonCode;    // 退回原因(當退回類型為不良品時，必須退回輸入原因，代碼檔：AMRS)
  private String classNo;       // 班別
  private String passYn;        // 判定 Y-合格 N-不合格
  private String scanDate;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getProcEmp() {
    return procEmp;
  }

  public void setProcEmp(String procEmp) {
    this.procEmp = procEmp;
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String kind) {
    this.kind = kind;
  }

  public String getBarCode() {
    return barCode;
  }

  public void setBarCode(String barCode) {
    this.barCode = barCode;
  }

  public String getLocate() {
    return locate;
  }

  public void setLocate(String locate) {
    this.locate = locate;
  }

  public String getScwJobNo() {
    return scwJobNo;
  }

  public void setScwJobNo(String scwJobNo) {
    this.scwJobNo = scwJobNo;
  }

  public Integer getItemNo() {
    return itemNo;
  }

  public void setItemNo(Integer itemNo) {
    this.itemNo = itemNo;
  }

  public String getIsrtType() {
    return isrtType;
  }

  public void setIsrtType(String isrtType) {
    this.isrtType = isrtType;
  }

  public String getReasonCode() {
    return reasonCode;
  }

  public void setReasonCode(String reasonCode) {
    this.reasonCode = reasonCode;
  }

  public String getClassNo() {
    return classNo;
  }

  public void setClassNo(String classNo) {
    this.classNo = classNo;
  }

  public String getPassYn() {
    return passYn;
  }

  public void setPassYn(String passYn) {
    this.passYn = passYn;
  }

  public String  getScanDate() {
    return scanDate;
  }

  public void setScanDate(String  scanDate) {
    this.scanDate = scanDate;
  }


  @Override
  public String toString() {
    return "MainData{" +
            "id=" + id +
            ", procEmp='" + procEmp + '\'' +
            ", kind='" + kind + '\'' +
            ", barCode='" + barCode + '\'' +
            ", locate='" + locate + '\'' +
            ", scwJobNo='" + scwJobNo + '\'' +
            ", itemNo=" + itemNo +
            ", isrtType='" + isrtType + '\'' +
            ", reasonCode='" + reasonCode + '\'' +
            ", classNo='" + classNo + '\'' +
            ", passYn='" + passYn + '\'' +
            ", scanDate='" + scanDate + '\'' +
            '}';
  }
}
