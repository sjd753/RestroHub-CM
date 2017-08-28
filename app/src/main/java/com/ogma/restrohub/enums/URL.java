package com.ogma.restrohub.enums;

/**
 * Created by alokdas on 11/08/15.
 */
public enum URL {

    LOGIN("login"),
    TABLE_LIST("table_list"),
    CATEGORY_LIST("category_list"),
    MENU_LIST("menu_list"),
    PLACE_ORDER("place_order"),
    PAY_NOW("change_order_status");

    public String BASE_URL = "http://ogmaconceptions.com/demo/restaurant/restro_hubapp/";

    public String mURL;

    URL(String mURL) {
        this.mURL = this.BASE_URL + mURL;
    }

    public String getURL() {
        return mURL;
    }

}
