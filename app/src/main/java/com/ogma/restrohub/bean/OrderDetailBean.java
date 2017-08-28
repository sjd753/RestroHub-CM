package com.ogma.restrohub.bean;

/**
 * Created by User on 20-09-2016.
 */
public class OrderDetailBean {

    private String orderId = "";
    private String status = "";
    private String categoryId = "";
    private String categoryName = "";
    private String menuId = "";
    private String menuName = "";
    private String menuPrice = "";
    private String menuOfferPrice = "";
    private String quantity = "";

    public OrderDetailBean(String orderId, String status, String categoryId, String categoryName, String menuId, String menuName, String menuPrice, String menuOfferPrice, String quantity) {
        this.orderId = orderId;
        this.status = status;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.menuId = menuId;
        this.menuName = menuName;
        this.menuPrice = menuPrice;
        this.menuOfferPrice = menuOfferPrice;
        this.quantity = quantity;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getMenuPrice() {
        return menuPrice;
    }

    public void setMenuPrice(String menuPrice) {
        this.menuPrice = menuPrice;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getMenuOfferPrice() {
        return menuOfferPrice;
    }

    public void setMenuOfferPrice(String menuOfferPrice) {
        this.menuOfferPrice = menuOfferPrice;
    }
}
