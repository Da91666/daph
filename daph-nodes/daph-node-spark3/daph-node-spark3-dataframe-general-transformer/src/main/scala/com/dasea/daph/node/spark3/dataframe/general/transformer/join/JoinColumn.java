package com.dasea.daph.node.spark3.dataframe.general.transformer.join;

public class JoinColumn {
  private String left;
  private String right;
  
  public JoinColumn(String left, String right) {
    this.left = left;
    this.right = right;
  }
  
  public JoinColumn() {
  }
  
  public String getLeft() {
    return left;
  }
  
  public void setLeft(String left) {
    this.left = left;
  }
  
  public String getRight() {
    return right;
  }
  
  public void setRight(String right) {
    this.right = right;
  }
}
