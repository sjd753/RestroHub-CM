package com.ogma.restrohub.bean;

/**
 * Created by User on 20-09-2016.
 */
public class OrderBean {

    private String restaurantId = "";
    private String tableId = "";
    private String serverOrderId = "";
    private String totalAmount = "";
    private String orderStatus = "";
    private String transactionId = "";
    private String created = "";

    public OrderBean(String restaurantId, String tableId, String serverOrderId, String totalAmount, String orderStatus, String transactionId, String created) {
        this.restaurantId = restaurantId;
        this.tableId = tableId;
        this.serverOrderId = serverOrderId;
        this.totalAmount = totalAmount;
        this.orderStatus = orderStatus;
        this.transactionId = transactionId;
        this.created = created;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getServerOrderId() {
        return serverOrderId;
    }

    public void setServerOrderId(String serverOrderId) {
        this.serverOrderId = serverOrderId;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }
}
