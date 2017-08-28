

package com.ogma.restrohub.activity;

import android.app.ProgressDialog;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.ogma.restrohub.R;
import com.ogma.restrohub.application.App;
import com.ogma.restrohub.application.AppSettings;
import com.ogma.restrohub.bean.CategoryBean;
import com.ogma.restrohub.bean.OrderBean;
import com.ogma.restrohub.bean.OrderDetailBean;
import com.ogma.restrohub.database.DatabaseHandler;
import com.ogma.restrohub.enums.URL;
import com.ogma.restrohub.network.HttpClient;
import com.ogma.restrohub.network.NetworkConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Cart extends AppCompatActivity {

    private static final int KEY_TAG = 17;
    private static final String TAG_PLACE_ORDER = "place_order";
    private static final String TAG_PAY_NOW = "pay_now";
    private static final String TAG = Cart.class.getName();

    private App app;
    private NetworkConnection connection;
    private CoordinatorLayout coordinatorLayout;
    private ExpandableListView expandableListView;
    private ExpandableListAdapter expandableListAdapter;
    private ArrayList<CategoryBean> list = new ArrayList<>();
    private ViewSwitcher viewSwitcher;
    private Button button;
    private JSONArray jArrSpinner = new JSONArray();
    private String tableId;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        app = (App) getApplication();
        app.setAppSettings(new AppSettings(this));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        String[] tables;
        try {
            jArrSpinner = new JSONArray(app.getAppSettings().__tables);
            tables = new String[jArrSpinner.length()];
            for (int i = 0; i < jArrSpinner.length(); i++) {
                tables[i] = jArrSpinner.getJSONObject(i).getString("name");
                if (i == 0)
                    tableId = jArrSpinner.getJSONObject(i).getString("id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Internal error: Tables not found", Toast.LENGTH_SHORT).show();
            return;
        }

        app = (App) getApplication();
        app.setAppSettings(new AppSettings(this));
        connection = new NetworkConnection(this);

        viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        button = (Button) findViewById(R.id.btn_place_order);
        button.setTag(TAG_PLACE_ORDER);

        spinner = (Spinner) findViewById(R.id.sp_tables);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_item, tables);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                try {
                    tableId = jArrSpinner.getJSONObject(position).getString("id");
                    notifyAdapter();
                    Log.e(TAG, "onItemSelected: " + jArrSpinner.getJSONObject(position).getString("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        expandableListView = (ExpandableListView) findViewById(R.id.elv_cart);
        expandableListAdapter = new ExpandableListAdapter();
        expandableListView.setAdapter(expandableListAdapter);

//        notifyAdapter();
        expandAll();
        notifyButton();
    }

    private void notifyAdapter() {
        Log.e(TAG, "notifyAdapter: tableId " + tableId);
        DatabaseHandler databaseHandler = new DatabaseHandler(this);
        int orderId = databaseHandler.getOrderIdIfExists(tableId);
        Log.e(TAG, "notifyAdapter: orderId " + orderId);
        if (orderId > 0) {
            String grandTotal = "$" + databaseHandler.getOrder(orderId).getTotalAmount();
            list.clear();
            list.addAll(databaseHandler.getOrderItemsNested(orderId));
            Log.e(TAG, "notifyAdapter: list menu items " + list.get(0).getMenuItems().size());
            databaseHandler.closeDB();
            TextView tvGrandTotal = (TextView) findViewById(R.id.tv_grand_total);
            tvGrandTotal.setText(grandTotal);
            viewSwitcher.setDisplayedChild(1);//showNext();
        } else {
            list.clear();// = new ArrayList<>();
            viewSwitcher.setDisplayedChild(0);
        }
        expandableListAdapter.notifyDataSetChanged();
        collapseAll();
        expandAll();
        notifyButton();
        Log.e(TAG, "notifyAdapter: " + list.size());
    }

    private void collapseAll() {
        for (int i = 0; i < list.size(); i++) {
            expandableListView.collapseGroup(i);
        }
    }

    private void expandAll() {
        for (int i = 0; i < list.size(); i++) {
            expandableListView.expandGroup(i);
        }
    }

    private void notifyButton() {
        boolean isPending = false;
        outerLoop:
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).getMenuItems().size(); j++) {
                if (list.get(i).getMenuItems().get(j).getStatus().equals(DatabaseHandler.Tables.OrderDetails.OrderStatus.PENDING.getStatus())) {
                    isPending = true;
                    break outerLoop;
                }
            }
        }
        if (isPending) {
            button.setText("PLACE ORDER");
            button.setTag(TAG_PLACE_ORDER);
        } else {
            button.setText("PAY NOW");
            button.setTag(TAG_PAY_NOW);
        }
    }

    private class ExpandableListAdapter extends BaseExpandableListAdapter {
        private GroupViewHolder groupViewHolder;
        private ChildViewHolder childViewHolder;

        class GroupViewHolder {
            TextView tvTitle;
        }

        class ChildViewHolder {
            TextView tvTitle, tvQuantity, tvOrderStatus, tvTotalPrice, tvPrice;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {
            super.registerDataSetObserver(dataSetObserver);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
            super.unregisterDataSetObserver(dataSetObserver);
        }

        @Override
        public int getGroupCount() {
            return list.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return list.get(groupPosition).getMenuItems().size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return list.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return list.get(groupPosition).getMenuItems().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.cart_group_item, parent, false);
                groupViewHolder = new GroupViewHolder();
                groupViewHolder.tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
                itemView.setTag(groupViewHolder);
            } else {
                groupViewHolder = (GroupViewHolder) itemView.getTag();
            }

            groupViewHolder.tvTitle.setText(list.get(groupPosition).getName());

            return itemView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.cart_child_item, parent, false);
                childViewHolder = new ChildViewHolder();
                childViewHolder.tvTitle = (TextView) itemView.findViewById(R.id.tv_child_item_name);
                childViewHolder.tvQuantity = (TextView) itemView.findViewById(R.id.tv_child_item_quantity);
                childViewHolder.tvOrderStatus = (TextView) itemView.findViewById(R.id.tv_child_item_order_status);
                childViewHolder.tvTotalPrice = (TextView) itemView.findViewById(R.id.tv_child_item_total_price);
                childViewHolder.tvPrice = (TextView) itemView.findViewById(R.id.tv_child_item_price);
                itemView.setTag(childViewHolder);
            } else {
                childViewHolder = (ChildViewHolder) itemView.getTag();
            }

            childViewHolder.tvTitle.setText(list.get(groupPosition).getMenuItems()
                    .get(childPosition).getName());
            int quantity = Integer.parseInt(list.get(groupPosition).getMenuItems()
                    .get(childPosition).getQuantity());
            childViewHolder.tvQuantity.setText("Quantity: " + quantity);
            if (list.get(groupPosition).getMenuItems().get(childPosition).getStatus().equals(DatabaseHandler.Tables.OrderDetails.OrderStatus.PENDING.getStatus())) {
                childViewHolder.tvOrderStatus.setVisibility(View.INVISIBLE);
            } else {
                childViewHolder.tvOrderStatus.setVisibility(View.VISIBLE);
            }
            float offerPrice = Float.parseFloat(list.get(groupPosition)
                    .getMenuItems().get(childPosition)
                    .getOfferPrice());
            childViewHolder.tvPrice.setText("$" + offerPrice);
            childViewHolder.tvTotalPrice.setText("$" + offerPrice * quantity);

            return itemView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return list.get(groupPosition).getMenuItems().get(childPosition).getStatus().equals(DatabaseHandler.Tables.OrderDetails.OrderStatus.PENDING.getStatus());
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public void onGroupExpanded(int groupPosition) {

        }

        @Override
        public void onGroupCollapsed(int groupPosition) {

        }

        @Override
        public long getCombinedChildId(long groupId, long childId) {
            return 0;
        }

        @Override
        public long getCombinedGroupId(long groupId) {
            return 0;
        }
    }

    private boolean prepareExecuteAsync() {
        if (connection.isNetworkConnected()) {
            return true;
        } else if (connection.isNetworkConnectingOrConnected()) {
            Snackbar.make(coordinatorLayout, "Connection temporarily unavailable", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(coordinatorLayout, "You're offline", Snackbar.LENGTH_SHORT).show();
        }
        return false;
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btn_place_order) {
            if (button.getTag().equals(TAG_PLACE_ORDER)) {
                DatabaseHandler databaseHandler = new DatabaseHandler(this);
                int orderId = databaseHandler.getOrderIdIfExists(tableId);

                if (orderId > 0 && prepareExecuteAsync()) {
                    OrderBean orderBean = databaseHandler.getOrder(orderId);

                    ArrayList<OrderDetailBean> list = databaseHandler.getOrderItems(orderId);

                    databaseHandler.closeDB();
                    if (list.size() > 0)
                        new PlaceOrderTask(list).execute(String.valueOf(orderId),
                                orderBean.getServerOrderId(),
                                orderBean.getOrderStatus(),
                                orderBean.getTotalAmount());
                }
            } else if (button.getTag().equals(TAG_PAY_NOW)) {
                DatabaseHandler databaseHandler = new DatabaseHandler(this);
                int orderId = databaseHandler.getOrderIdIfExists(tableId);

                if (orderId > 0 && prepareExecuteAsync()) {
                    OrderBean orderBean = databaseHandler.getOrder(orderId);
                    databaseHandler.closeDB();
                    new PayNowTask().execute(String.valueOf(orderId), orderBean.getServerOrderId());
                }
            } else {
                Log.e("onClick: ", "unknown tag");
                Snackbar.make(coordinatorLayout, "Something went wrong", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void emptyCart(String tableId) {
        DatabaseHandler databaseHandler = new DatabaseHandler(this);
        int orderId = databaseHandler.getOrderIdIfExists(tableId);
        if (orderId > 0) {
            databaseHandler.deleteOrder(orderId);
            list = databaseHandler.getOrderItemsNested(orderId);
            databaseHandler.closeDB();
            expandableListAdapter.notifyDataSetChanged();
            collapseAll();
            expandAll();
            viewSwitcher.setDisplayedChild(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if (item.getItemId() == R.id.menu_action_empty_cart) {
            //Todo: Empty cart
            emptyCart(tableId);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class PlaceOrderTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private ProgressDialog mDialog = new ProgressDialog(Cart.this);
        private JSONObject response;
        private ArrayList<OrderDetailBean> list = new ArrayList<>();
        private String orderId = "", serverOrderId = "";

        public PlaceOrderTask(ArrayList<OrderDetailBean> list) {
            this.list = list;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Placing order...");
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                orderId = params[0];

                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("restaurant_id", app.getAppSettings().__uRestaurantId);
                mJsonObject.put("table_id", tableId);
                mJsonObject.put("order_id", params[0]);
                mJsonObject.put("server_order_id", params[1]);
                mJsonObject.put("order_status", params[2]);
                mJsonObject.put("total_amount", params[3]);

                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < list.size(); i++) {
                    JSONObject object = new JSONObject();
                    object.put("order_id", list.get(i).getOrderId());
                    object.put("status", list.get(i).getStatus());
                    object.put("category_id", list.get(i).getCategoryId());
                    object.put("category_name", list.get(i).getCategoryName());
                    object.put("menu_id", list.get(i).getMenuId());
                    object.put("menu_name", list.get(i).getMenuName());
                    object.put("menu_price", list.get(i).getMenuPrice());
                    object.put("menu_offer_price", list.get(i).getMenuOfferPrice());
                    object.put("menu_quantity", list.get(i).getQuantity());

                    jsonArray.put(object);
                }

                mJsonObject.put("cart_list", jsonArray);

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.PLACE_ORDER.getURL(), mJsonObject);

                boolean status = response != null && response.getInt("is_error") == 0;
                if (status) {
                    serverOrderId = response.getString("order_id");
                }
                return status;
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
                mDialog.dismiss();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean status) {
            mDialog.dismiss();
            if (status) {
                DatabaseHandler databaseHandler = new DatabaseHandler(Cart.this);
                databaseHandler.updateServerOrderId(Integer.parseInt(orderId), Integer.parseInt(serverOrderId));
                databaseHandler.updateOrderItemStatus(Integer.parseInt(orderId), DatabaseHandler.Tables.OrderDetails.OrderStatus.PENDING.getStatus(), DatabaseHandler.Tables.OrderDetails.OrderStatus.PROCESSING.getStatus());
                Cart.this.list = databaseHandler.getOrderItemsNested(Integer.parseInt(orderId));
                expandableListAdapter.notifyDataSetChanged();
                databaseHandler.closeDB();
                collapseAll();
                expandAll();
                notifyButton();
            } else {
                try {
                    Snackbar.make(coordinatorLayout, response.getString("err_msg"), Snackbar.LENGTH_LONG).show();
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                    Snackbar.make(coordinatorLayout, error_msg, Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class PayNowTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private ProgressDialog mDialog = new ProgressDialog(Cart.this);
        private JSONObject response;
        private String orderId = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("PLease wait...");
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                orderId = params[0];

                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("restaurant_id", app.getAppSettings().__uRestaurantId);
                mJsonObject.put("table_id", "1");
                mJsonObject.put("order_id", params[0]);
                mJsonObject.put("server_order_id", params[1]);

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.PAY_NOW.getURL(), mJsonObject);

                return response != null && response.getInt("is_error") == 0;
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
                mDialog.dismiss();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean status) {
            mDialog.dismiss();
            if (status) {
                Snackbar.make(coordinatorLayout, "Payment successful", Snackbar.LENGTH_SHORT).show();
                emptyCart(tableId);
            } else {
                try {
                    Snackbar.make(coordinatorLayout, response.getString("err_msg"), Snackbar.LENGTH_LONG).show();
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                    Snackbar.make(coordinatorLayout, error_msg, Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }
}
