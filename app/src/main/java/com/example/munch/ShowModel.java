package com.example.munch;

public class ShowModel {

    private String Name;
    private String Overview;

    private ShowModel() {

    }


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

    public void setOverview(String overview) {
        Overview = overview;
    }
}
