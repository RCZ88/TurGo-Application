package com.example.turgo;

public enum TaskItemMode {
    RECYCLER("recycler"),
    VIEW_PAGER("viewPager");
    private final String node;
    TaskItemMode(String node){
        this.node = node;
    }

    public String getNode() {
        return node;
    }

}
