package com.dasea.daph.node.spark3.dataframe.general.transformer.join;

public class OutputColumn {
    private JoinConfig.LaneSelector line;
    private String in;
    private String name;

    public OutputColumn(JoinConfig.LaneSelector line, String in, String name) {
        this.line = line;
        this.in = in;
        this.name = name;
    }

    public OutputColumn(String line, String in, String name) {
        this.line = JoinConfig.LaneSelector.valueOf(line);
        this.in = in;
        this.name = name;
    }

    public OutputColumn() {
    }

    public JoinConfig.LaneSelector getLine() {
        return line;
    }

    public void setLine(JoinConfig.LaneSelector line) {
        this.line = line;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}