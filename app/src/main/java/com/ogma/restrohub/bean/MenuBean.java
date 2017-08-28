package com.ogma.restrohub.bean;

/**
 * Created by User on 20-09-2016.
 */
public class MenuBean {

    private String id = "";
    private String name = "";
    private String quantity = "";
    private String price = "";
    private String offerPrice = "";
    private String status = "";

    public MenuBean(String id, String name, String quantity, String price, String offerPrice, String status) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.offerPrice = offerPrice;
        this.status = status;
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

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(String offerPrice) {
        this.offerPrice = offerPrice;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
