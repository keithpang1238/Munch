package com.example.munch;

public class ShowModel {

    private String Name;
    private final String Overview;

    private ShowModel(String name, String overview) {
        this.Name = name;
        this.Overview = overview;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getOverview() {
        return Overview;
    }
}
