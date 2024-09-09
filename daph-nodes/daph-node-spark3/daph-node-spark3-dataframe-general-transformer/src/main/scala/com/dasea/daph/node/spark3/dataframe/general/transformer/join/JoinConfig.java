package com.dasea.daph.node.spark3.dataframe.general.transformer.join;

import com.dasea.daph.api.config.NodeConfig;

public class JoinConfig extends NodeConfig {
  private String joinType;
  private String leftLine;
  private String rightLine;
  private JoinColumn[] joinColumns;
  private OutputColumn[] outputColumns;
  
  public String getJoinType() {
    return joinType;
  }
  
  public void setJoinType(String joinType) {
    this.joinType = joinType;
  }
  
  public String getLeftLine() {
    return leftLine;
  }
  
  public void setLeftLine(String leftLine) {
    this.leftLine = leftLine;
  }
  
  public String getRightLine() {
    return rightLine;
  }
  
  public void setRightLine(String rightLine) {
    this.rightLine = rightLine;
  }
  
  public JoinColumn[] getJoinColumns() {
    return joinColumns;
  }
  
  public void setJoinColumns(JoinColumn[] joinColumns) {
    this.joinColumns = joinColumns;
  }
  
  public OutputColumn[] getOutputColumns() {
    return outputColumns;
  }
  
  public void setOutputColumns(OutputColumn[] outputColumns) {
    this.outputColumns = outputColumns;
  }
  
  enum LaneSelector {
    left,
    right;
  }
}
