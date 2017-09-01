package com.ogma.restrohub.model;

/**
 * Created by TANIMA on 08-07-2017.
 */

public class Table {

    private String id;
    private String name;


    public Table(String id, String name) {
        this.id = id;
        this.name = name;


    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
