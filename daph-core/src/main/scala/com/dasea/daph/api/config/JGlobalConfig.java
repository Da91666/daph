package com.dasea.daph.api.config;

import com.dasea.daph.api.node.NodeDescription;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JGlobalConfig {
  private Map<String, String> options = new HashMap<String, String>();
  private List<NodeDescription> nodes;
  
  public Map<String, String> getOptions() {
    return options;
  }
  
  public void setOptions(Map<String, String> options) {
    this.options = options;
  }

  public List<NodeDescription> getNodes() {
    return nodes;
  }

  public void setNodes(List<NodeDescription> nodes) {
    this.nodes = nodes;
  }
}
