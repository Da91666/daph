package com.dasea.daph.api.config;

import com.dasea.daph.api.node.SimpleNodeDescription;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleJGlobalConfig {
  private Map<String, String> options = new HashMap<String, String>();
  private List<SimpleNodeDescription> nodes;
  
  public Map<String, String> getOptions() {
    return options;
  }
  
  public void setOptions(Map<String, String> options) {
    this.options = options;
  }

  public List<SimpleNodeDescription> getNodes() {
    return nodes;
  }

  public void setNodes(List<SimpleNodeDescription> nodes) {
    this.nodes = nodes;
  }
}
