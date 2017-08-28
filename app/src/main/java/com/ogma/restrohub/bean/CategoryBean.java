package com.ogma.restrohub.bean;

import java.util.ArrayList;

/**
 * Created by User on 20-09-2016.
 */
public class CategoryBean {

    private String id = "";
    private String name = "";
    private ArrayList<MenuBean> menuItems = new ArrayList<>();

    public CategoryBean(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public CategoryBean(String id, String name, ArrayList<MenuBean> menuItems) {
        this.id = id;
        this.name = name;
        this.menuItems = menuItems;
    }

    public ArrayList<MenuBean> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(ArrayList<MenuBean> menuItems) {
        this.menuItems = menuItems;
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
