package com.ogma.restrohub.database;

/**
 * Created by alokdas on 07/09/15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ogma.restrohub.bean.CategoryBean;
import com.ogma.restrohub.bean.MenuBean;
import com.ogma.restrohub.bean.OrderBean;
import com.ogma.restrohub.bean.OrderDetailBean;

import java.util.ArrayList;


public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 14;

    // Database Name
    private static final String DATABASE_NAME = "restrohub";


    public static final class Tables {

        public static final class Orders {
            // Table name
            private static final String TABLE_ORDERS = "orders";
            // Table Columns names
            private static final String KEY_ID = "id";
            private static final String KEY_RESTAURANT_ID = "restaurant_id";
            private static final String KEY_TABLE_ID = "table_id";
            private static final String KEY_SERVER_ORDER_ID = "server_order_id";
            private static final String KEY_TOTAL_AMOUNT = "total_amount";
            private static final String KEY_ORDER_STATUS = "order_status";
            private static final String KEY_TRANSACTION_ID = "transaction_id";
            private static final String KEY_CREATED = "created";

            // Create table query
            private static final String CREATE_ORDERS_TABLE = "CREATE TABLE " + TABLE_ORDERS
                    + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_RESTAURANT_ID + " INTEGER NOT NULL,"
                    + KEY_TABLE_ID + " INTEGER,"
                    + KEY_SERVER_ORDER_ID + " INTEGER,"
                    + KEY_TOTAL_AMOUNT + " INTEGER NOT NULL,"
                    + KEY_ORDER_STATUS + " TEXT NOT NULL,"
                    + KEY_TRANSACTION_ID + " TEXT,"
                    + KEY_CREATED + " TEXT NOT NULL" + ")";

            //Order statuses
            public enum OrderStatus {
                PAID("paid"),
                UNPAID("unpaid");

                String status;

                OrderStatus(String status) {
                    this.status = status;
                }

                public String getStatus() {
                    return status;
                }
            }
        }

        public static final class OrderDetails {
            // Table name
            private static final String TABLE_ORDER_DETAILS = "order_details";
            // Table Columns names
            private static final String KEY_ID = "id";
            private static final String KEY_ORDER_ID = "order_id";
            private static final String KEY_STATUS = "status";
            private static final String KEY_CATEGORY_ID = "category_id";
            private static final String KEY_CATEGORY_NAME = "category_name";
            private static final String KEY_MENU_ID = "menu_id";
            private static final String KEY_MENU_NAME = "menu_name";
            private static final String KEY_MENU_PRICE = "menu_price";
            private static final String KEY_MENU_OFFER_PRICE = "menu_offer_price";
            private static final String KEY_QUANTITY = "quantity";

            // Create table query
            private static final String CREATE_ORDER_DETAILS_TABLE = "CREATE TABLE " + TABLE_ORDER_DETAILS
                    + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_ORDER_ID + " INTEGER DEFAULT NULL,"
                    + KEY_STATUS + " TEXT DEFAULT '" + OrderStatus.PENDING.getStatus() + "',"
                    + KEY_CATEGORY_ID + " INTEGER DEFAULT NULL,"
                    + KEY_CATEGORY_NAME + " TEXT,"
                    + KEY_MENU_ID + " INTEGER,"
                    + KEY_MENU_NAME + " TEXT,"
                    + KEY_MENU_PRICE + " REAL,"
                    + KEY_MENU_OFFER_PRICE + " REAL,"
                    + KEY_QUANTITY + " INTEGER" + ")";

            public enum OrderStatus {
                PENDING("pending"),
                PROCESSING("processing");

                String status;

                OrderStatus(String status) {
                    this.status = status;
                }

                public String getStatus() {
                    return status;
                }
            }
        }

    }


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Tables.Orders.CREATE_ORDERS_TABLE);
        db.execSQL(Tables.OrderDetails.CREATE_ORDER_DETAILS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + Tables.Orders.TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.OrderDetails.TABLE_ORDER_DETAILS);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    public int getOrderIdIfExists(String tableId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Tables.Orders.TABLE_ORDERS, //table
                new String[]{Tables.Orders.KEY_ID}, //selection columns
                Tables.Orders.KEY_TABLE_ID + "=?", //selection
                new String[]{tableId}, //selection arguments
                null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            int id = cursor.getInt(cursor.getColumnIndex(Tables.Orders.KEY_ID));
            cursor.close();
            return id;
        }

        return 0;
    }

    public float getTotalAmount(int orderId) {
        float total = 0.0f;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Tables.OrderDetails.TABLE_ORDER_DETAILS, //table
                new String[]{Tables.OrderDetails.KEY_MENU_OFFER_PRICE, Tables.OrderDetails.KEY_QUANTITY}, //selection columns
                Tables.OrderDetails.KEY_ORDER_ID + "=?", //selection
                new String[]{String.valueOf(orderId)}, //selection arguments
                null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                total = total + (cursor.getFloat(cursor.getColumnIndex(Tables.OrderDetails.KEY_MENU_OFFER_PRICE)) * cursor.getInt(cursor.getColumnIndex(Tables.OrderDetails.KEY_QUANTITY)));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return total;
    }

    public int updateTotalAmount(int orderId, float totalAmount) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Tables.Orders.KEY_TOTAL_AMOUNT, (int) totalAmount);

        return db.update(Tables.Orders.TABLE_ORDERS, values, Tables.Orders.KEY_ID + " = ?",
                new String[]{String.valueOf(orderId)});
    }

    public int updateServerOrderId(int orderId, int serverOrderId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Tables.Orders.KEY_SERVER_ORDER_ID, serverOrderId);

        return db.update(Tables.Orders.TABLE_ORDERS, values, Tables.Orders.KEY_ID + " = ?",
                new String[]{String.valueOf(orderId)});
    }

    public int updateOrderItemStatus(int orderId, String oldStatus, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Tables.OrderDetails.KEY_STATUS, newStatus);

        return db.update(Tables.OrderDetails.TABLE_ORDER_DETAILS, values, Tables.OrderDetails.KEY_ORDER_ID + "=?" + " AND " + Tables.OrderDetails.KEY_STATUS + "=?",
                new String[]{String.valueOf(orderId), oldStatus});
    }

    public ArrayList<OrderDetailBean> getOrderItems(int orderId) {
        ArrayList<OrderDetailBean> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Tables.OrderDetails.TABLE_ORDER_DETAILS, //table
                null, //selection columns
                Tables.OrderDetails.KEY_ORDER_ID + "=?" + " AND " + Tables.OrderDetails.KEY_STATUS + "=?", //selection
                new String[]{String.valueOf(orderId), Tables.OrderDetails.OrderStatus.PENDING.getStatus()}, //selection arguments
                null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String id = String.valueOf(cursor.getInt(cursor.getColumnIndex(Tables.OrderDetails.KEY_ORDER_ID)));
                String status = cursor.getString(cursor.getColumnIndex(Tables.OrderDetails.KEY_STATUS));
                String categoryId = String.valueOf(cursor.getInt(cursor.getColumnIndex(Tables.OrderDetails.KEY_CATEGORY_ID)));
                String categoryName = cursor.getString(cursor.getColumnIndex(Tables.OrderDetails.KEY_CATEGORY_NAME));
                String menuId = String.valueOf(cursor.getInt(cursor.getColumnIndex(Tables.OrderDetails.KEY_MENU_ID)));
                String menuName = cursor.getString(cursor.getColumnIndex(Tables.OrderDetails.KEY_MENU_NAME));
                String menuPrice = String.valueOf(cursor.getFloat(cursor.getColumnIndex(Tables.OrderDetails.KEY_MENU_PRICE)));
                String menuOfferPrice = String.valueOf(cursor.getFloat(cursor.getColumnIndex(Tables.OrderDetails.KEY_MENU_OFFER_PRICE)));
                String quantity = String.valueOf(cursor.getInt(cursor.getColumnIndex(Tables.OrderDetails.KEY_QUANTITY)));

                items.add(new OrderDetailBean(id, status, categoryId, categoryName, menuId, menuName, menuPrice, menuOfferPrice, quantity));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }

    public ArrayList<CategoryBean> getOrderItemsNested(int orderId) {
        ArrayList<CategoryBean> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Tables.OrderDetails.TABLE_ORDER_DETAILS, //table
                null, //selection columns
                Tables.OrderDetails.KEY_ORDER_ID + "=?", //selection
                new String[]{String.valueOf(orderId)}, //selection arguments
                null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String id = String.valueOf(cursor.getInt(cursor.getColumnIndex(Tables.OrderDetails.KEY_ORDER_ID)));
                String status = cursor.getString(cursor.getColumnIndex(Tables.OrderDetails.KEY_STATUS));
                String categoryId = String.valueOf(cursor.getInt(cursor.getColumnIndex(Tables.OrderDetails.KEY_CATEGORY_ID)));
                String categoryName = cursor.getString(cursor.getColumnIndex(Tables.OrderDetails.KEY_CATEGORY_NAME));
                String menuId = String.valueOf(cursor.getInt(cursor.getColumnIndex(Tables.OrderDetails.KEY_MENU_ID)));
                String menuName = cursor.getString(cursor.getColumnIndex(Tables.OrderDetails.KEY_MENU_NAME));
                String menuPrice = String.valueOf(cursor.getFloat(cursor.getColumnIndex(Tables.OrderDetails.KEY_MENU_PRICE)));
                String menuOfferPrice = String.valueOf(cursor.getFloat(cursor.getColumnIndex(Tables.OrderDetails.KEY_MENU_OFFER_PRICE)));
                String quantity = String.valueOf(cursor.getInt(cursor.getColumnIndex(Tables.OrderDetails.KEY_QUANTITY)));

                CategoryBean categoryBean = new CategoryBean(categoryId, categoryName, getMenuItems(orderId, Integer.parseInt(categoryId)));
                if (items.size() == 0) {
                    items.add(categoryBean);
                } else {
                    boolean catExists = false;
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).getId().equals(categoryBean.getId())) {
                            catExists = true;
                            break;
                        }
                    }
                    if (!catExists)
                        items.add(categoryBean);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }

    private ArrayList<MenuBean> getMenuItems(int orderId, int _categoryId) {
        ArrayList<MenuBean> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Tables.OrderDetails.TABLE_ORDER_DETAILS, //table
                null, //selection columns
                Tables.OrderDetails.KEY_ORDER_ID + "=?" + " AND " + Tables.OrderDetails.KEY_CATEGORY_ID + "=?", //selection
                new String[]{String.valueOf(orderId), String.valueOf(_categoryId)}, //selection arguments
                null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String id = String.valueOf(cursor.getInt(cursor.getColumnIndex(Tables.OrderDetails.KEY_ORDER_ID)));
                String status = cursor.getString(cursor.getColumnIndex(Tables.OrderDetails.KEY_STATUS));
                String categoryId = String.valueOf(cursor.getInt(cursor.getColumnIndex(Tables.OrderDetails.KEY_CATEGORY_ID)));
                String categoryName = cursor.getString(cursor.getColumnIndex(Tables.OrderDetails.KEY_CATEGORY_NAME));
                String menuId = String.valueOf(cursor.getInt(cursor.getColumnIndex(Tables.OrderDetails.KEY_MENU_ID)));
                String menuName = cursor.getString(cursor.getColumnIndex(Tables.OrderDetails.KEY_MENU_NAME));
                String menuPrice = String.valueOf(cursor.getFloat(cursor.getColumnIndex(Tables.OrderDetails.KEY_MENU_PRICE)));
                String menuOfferPrice = String.valueOf(cursor.getFloat(cursor.getColumnIndex(Tables.OrderDetails.KEY_MENU_OFFER_PRICE)));
                String quantity = String.valueOf(cursor.getInt(cursor.getColumnIndex(Tables.OrderDetails.KEY_QUANTITY)));

                items.add(new MenuBean(menuId, menuName, quantity, menuPrice, menuOfferPrice, status));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }

    public OrderBean getOrder(int orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Tables.Orders.TABLE_ORDERS, //table
                null, //selection columns
                Tables.Orders.KEY_ID + "=?", //selection
                new String[]{String.valueOf(orderId)}, //selection arguments
                null, null, null, null);
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {

            int id = cursor.getInt(cursor.getColumnIndex(Tables.Orders.KEY_ID));
            int restaurantId = cursor.getInt(cursor.getColumnIndex(Tables.Orders.KEY_RESTAURANT_ID));
            int tableId = cursor.getInt(cursor.getColumnIndex(Tables.Orders.KEY_TABLE_ID));
            int serverOrderId = cursor.getInt(cursor.getColumnIndex(Tables.Orders.KEY_SERVER_ORDER_ID));
            int totalAmount = cursor.getInt(cursor.getColumnIndex(Tables.Orders.KEY_TOTAL_AMOUNT));
            String orderStatus = cursor.getString(cursor.getColumnIndex(Tables.Orders.KEY_ORDER_STATUS));
            String transactionId = cursor.getString(cursor.getColumnIndex(Tables.Orders.KEY_TRANSACTION_ID));
            String created = cursor.getString(cursor.getColumnIndex(Tables.Orders.KEY_CREATED));

            cursor.close();

            return new OrderBean(String.valueOf(restaurantId), String.valueOf(tableId), String.valueOf(serverOrderId), String.valueOf(totalAmount),
                    orderStatus, transactionId, created);
        }
        return null;
    }

    // Adding
    public void addOrder(OrderBean order) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Tables.Orders.KEY_RESTAURANT_ID, Integer.parseInt(order.getRestaurantId()));
        values.put(Tables.Orders.KEY_TABLE_ID, Integer.parseInt(order.getTableId()));
        values.put(Tables.Orders.KEY_SERVER_ORDER_ID, Integer.parseInt(order.getServerOrderId()));
        values.put(Tables.Orders.KEY_TOTAL_AMOUNT, Integer.parseInt(order.getTotalAmount()));
        values.put(Tables.Orders.KEY_ORDER_STATUS, order.getOrderStatus());
        values.put(Tables.Orders.KEY_TRANSACTION_ID, order.getTransactionId());
        values.put(Tables.Orders.KEY_CREATED, order.getCreated());

        // Inserting Row
        db.insert(Tables.Orders.TABLE_ORDERS, null, values);

        db.close(); // Closing database connection
    }

    // Adding
    public void addOrderDetails(OrderDetailBean orderDetail) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Tables.OrderDetails.KEY_ORDER_ID, Integer.parseInt(orderDetail.getOrderId()));
        values.put(Tables.OrderDetails.KEY_STATUS, orderDetail.getStatus());
        values.put(Tables.OrderDetails.KEY_CATEGORY_ID, Integer.parseInt(orderDetail.getCategoryId()));
        values.put(Tables.OrderDetails.KEY_CATEGORY_NAME, orderDetail.getCategoryName());
        values.put(Tables.OrderDetails.KEY_MENU_ID, Integer.parseInt(orderDetail.getMenuId()));
        values.put(Tables.OrderDetails.KEY_MENU_NAME, orderDetail.getMenuName());
        values.put(Tables.OrderDetails.KEY_MENU_PRICE, Float.parseFloat(orderDetail.getMenuPrice()));
        values.put(Tables.OrderDetails.KEY_MENU_OFFER_PRICE, Float.parseFloat(orderDetail.getMenuOfferPrice()));
        values.put(Tables.OrderDetails.KEY_QUANTITY, String.valueOf(orderDetail.getQuantity()));

        // Inserting Row
        db.insert(Tables.OrderDetails.TABLE_ORDER_DETAILS, null, values);

        db.close(); // Closing database connection
    }


    // Deleting order
    public int deleteOrder(int orderId) {
        int count = 0;
        if (deleteOrderItems(orderId) > 0) {
            SQLiteDatabase db = this.getWritableDatabase();
            count = db.delete(Tables.Orders.TABLE_ORDERS, Tables.Orders.KEY_ID + " = ?",
                    new String[]{String.valueOf(orderId)});
            db.close();
        }
        return count;
    }

    private int deleteOrderItems(int orderId) {
        int count = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        count = db.delete(Tables.OrderDetails.TABLE_ORDER_DETAILS, Tables.OrderDetails.KEY_ORDER_ID + " = ?",
                new String[]{String.valueOf(orderId)});
        db.close();
        return count;
    }


    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

}
