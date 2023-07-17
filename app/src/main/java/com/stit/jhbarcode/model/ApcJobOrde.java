package com.stit.jhbarcode.model;

import java.math.BigDecimal;

public class ApcJobOrde {
  private String jobNo;
  private Integer itemNo;
  private String wirKind;
  private BigDecimal drawDia;
  private String assmNo;
  private String assmName;
  private String luoNo;
  private BigDecimal requQty;
  private BigDecimal issuQty;
  private BigDecimal fnshQty;

  public String getJobNo() {
    return jobNo;
  }

  public void setJobNo(String jobNo) {
    this.jobNo = jobNo;
  }

  public Integer getItemNo() {
    return itemNo;
  }

  public void setItemNo(Integer itemNo) {
    this.itemNo = itemNo;
  }

  public String getWirKind() {
    return wirKind;
  }

  public void setWirKind(String wirKind) {
    this.wirKind = wirKind;
  }

  public BigDecimal getDrawDia() {
    return drawDia;
  }

  public void setDrawDia(BigDecimal drawDia) {
    this.drawDia = drawDia;
  }

  public String getAssmNo() {
    return assmNo;
  }

  public void setAssmNo(String assmNo) {
    this.assmNo = assmNo;
  }

  public String getAssmName() {
    return assmName;
  }

  public void setAssmName(String assmName) {
    this.assmName = assmName;
  }

  public String getLuoNo() {
    return luoNo;
  }

  public void setLuoNo(String luoNo) {
    this.luoNo = luoNo;
  }

  public BigDecimal getRequQty() {
    return requQty;
  }

  public void setRequQty(BigDecimal requQty) {
    this.requQty = requQty;
  }

  public BigDecimal getIssuQty() {
    return issuQty;
  }

  public void setIssuQty(BigDecimal issuQty) {
    this.issuQty = issuQty;
  }

  public BigDecimal getFnshQty() {
    return fnshQty;
  }

  public void setFnshQty(BigDecimal fnshQty) {
    this.fnshQty = fnshQty;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ApcJobOrde that = (ApcJobOrde) o;

    if (!jobNo.equals(that.jobNo)) return false;
    return itemNo.equals(that.itemNo);
  }

  @Override
  public int hashCode() {
    int result = jobNo.hashCode();
    result = 31 * result + itemNo.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ApcJobOrde{" +
            "jobNo='" + jobNo + '\'' +
            ", itemNo=" + itemNo +
            ", wirKind='" + wirKind + '\'' +
            ", drawDia=" + drawDia +
            ", assmNo='" + assmNo + '\'' +
            ", assmName='" + assmName + '\'' +
            ", luoNo='" + luoNo + '\'' +
            ", requQty=" + requQty +
            ", issuQty=" + issuQty +
            ", fnshQty=" + fnshQty +
            '}';
  }
}
