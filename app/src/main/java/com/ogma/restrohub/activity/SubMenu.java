package com.ogma.restrohub.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.DataSetObserver;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.ogma.restrohub.R;
import com.ogma.restrohub.application.App;
import com.ogma.restrohub.application.AppSettings;
import com.ogma.restrohub.bean.OrderBean;
import com.ogma.restrohub.bean.OrderDetailBean;
import com.ogma.restrohub.database.DatabaseHandler;
import com.ogma.restrohub.enums.URL;
import com.ogma.restrohub.network.HttpClient;
import com.ogma.restrohub.network.NetworkConnection;
import com.ogma.restrohub.utility.UniversalImageLoaderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SubMenu extends AppCompatActivity {

    public static final String EXTRA_ID = "category_id";
    public static final String EXTRA_NAME = "category_name";
    private static final String TAG = Menu.class.getName();

    private android.view.Menu menu;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerAdapter recyclerAdapter;
    private CoordinatorLayout coordinatorLayout;
    private ActionMode mActionMode;
    private NetworkConnection connection;
    private JSONArray jArr = new JSONArray();
    private ImageLoader imageLoader;
    private String categoryId = "", categoryName = "";
    private View bottomSheet;
    private TextView tvSheetItemName, tvSheetQuantity, tvSheetPrice;
    private SeekBar seekBarSheet;
    private BottomSheetBehavior sheetBehavior;
    private int itemOfferPrice = 0;
    private FloatingActionButton fab;
    private JSONObject jSelectedItem;
    private App app;
    private JSONArray jArrSpinner = new JSONArray();
    private String tableId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_menu);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        app = (App) getApplication();
        app.setAppSettings(new AppSettings(this));
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

        connection = new NetworkConnection(this);
        imageLoader = new UniversalImageLoaderFactory.Builder(this).getInstance().initForAdapter().build();

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        recyclerView = (RecyclerView) findViewById(R.id.rv_sub_menu);

        recyclerView.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerAdapter = new RecyclerAdapter();
        recyclerView.setAdapter(recyclerAdapter);

        fab = (FloatingActionButton) findViewById(R.id.fab_sheet);
        fab.hide();

        bottomSheet = findViewById(R.id.bottom_sheet);
//        bottomSheet.setVisibility(View.GONE);

        sheetBehavior = BottomSheetBehavior.from(bottomSheet);
        sheetBehavior.setHideable(true);
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        fab.show();
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        fab.hide();
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        tvSheetItemName = (TextView) findViewById(R.id.tv_sheet_item_name);
        tvSheetQuantity = (TextView) findViewById(R.id.tv_sheet_quantity);
        tvSheetPrice = (TextView) findViewById(R.id.tv_sheet_price);
        seekBarSheet = (SeekBar) findViewById(R.id.seek_bar_sheet);
        seekBarSheet.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String quantity = "Quantity: " + progress;
                tvSheetQuantity.setText(quantity);
                int price = itemOfferPrice * progress;
                String priceString = "Price: $" + price;
                tvSheetPrice.setText(priceString);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        Spinner spinner = (Spinner) findViewById(R.id.sp_sheet_tables);
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tables));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                try {
                    tableId = jArrSpinner.getJSONObject(position).getString("id");
                    Log.e(TAG, "onItemSelected: " + jArrSpinner.getJSONObject(position).getString("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (getIntent().getStringExtra(EXTRA_ID) != null && getIntent().getStringExtra(EXTRA_NAME) != null) {
            categoryId = getIntent().getStringExtra(EXTRA_ID);
            categoryName = getIntent().getStringExtra(EXTRA_NAME);
            if (prepareExecuteAsync())
                new FetchMenuTask().execute();
        } else {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, "An internal error occurred", Snackbar.LENGTH_SHORT);
            snackbar.setCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    super.onDismissed(snackbar, event);
                    finish();
                }

                @Override
                public void onShown(Snackbar snackbar) {
                    super.onShown(snackbar);
                }
            });
            snackbar.show();
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

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sub_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if (item.getItemId() == R.id.menu_action_cart) {
            startActivity(new Intent(this, Cart.class));
//            Snackbar.make(coordinatorLayout, "Empty cart", Snackbar.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sheet_toggle:
                if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else if (sheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                break;
            case R.id.fab_sheet:
                try {
                    addToCart();
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    tvSheetItemName.setText(tvSheetItemName.getText() + " added to cart");
                    fab.hide();
                    setPendingState(sheetBehavior, BottomSheetBehavior.STATE_HIDDEN, 2000);
                } catch (JSONException | SQLException | NullPointerException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    private void setPendingState(final BottomSheetBehavior sheetBehavior, final int state, int delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sheetBehavior.setState(state);
            }
        }, delay);
    }

    private void addToCart() throws SQLException, JSONException, NullPointerException {
        DatabaseHandler databaseHandler = new DatabaseHandler(this);
        if (databaseHandler.getOrderIdIfExists(tableId) == 0) {
            databaseHandler.addOrder(new OrderBean(app.getAppSettings().__uRestaurantId,
                    tableId,
                    "0",
                    String.valueOf(itemOfferPrice * seekBarSheet.getProgress()),
                    DatabaseHandler.Tables.Orders.OrderStatus.UNPAID.getStatus(),
                    "0",
                    "20-09-2016 18:58:00"));
            databaseHandler.addOrderDetails(new OrderDetailBean(String.valueOf(databaseHandler.getOrderIdIfExists(tableId)),
                    DatabaseHandler.Tables.OrderDetails.OrderStatus.PENDING.getStatus(),
                    categoryId,
                    categoryName,
                    jSelectedItem.getString("id"),
                    jSelectedItem.getString("name"),
                    jSelectedItem.getString("price"),
                    jSelectedItem.getString("offer_price"),
                    String.valueOf(seekBarSheet.getProgress())));
        } else {
            int orderId = databaseHandler.getOrderIdIfExists(tableId);
            databaseHandler.addOrderDetails(new OrderDetailBean(String.valueOf(orderId),
                    DatabaseHandler.Tables.OrderDetails.OrderStatus.PENDING.getStatus(),
                    categoryId,
                    categoryName,
                    jSelectedItem.getString("id"),
                    jSelectedItem.getString("name"),
                    jSelectedItem.getString("price"),
                    jSelectedItem.getString("offer_price"),
                    String.valueOf(seekBarSheet.getProgress())));
            float totalAmount = databaseHandler.getTotalAmount(orderId);
            databaseHandler.updateTotalAmount(orderId, totalAmount);
        }
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        private int count = 0;

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, android.view.Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_sub_menu_action, menu);
            mode.setTitle(String.valueOf(count));
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, android.view.Menu menu) {
            return false;// Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_action_add_quantity:
                    count++;
                    mode.setTitle(String.valueOf(count));
                    return true;
                case R.id.menu_action_decrease_quantity:
                    count--;
                    mode.setTitle(String.valueOf(count));
                    return true;
                case R.id.menu_action_add_to_cart:
                    CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
                    Snackbar.make(coordinatorLayout, "Added to cart", Snackbar.LENGTH_SHORT).show();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    mode.finish(); // Action picked, so close the CAB
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            count = 0;
            mActionMode = null;
        }
    };

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sub_menu_item, parent, false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = recyclerView.getChildAdapterPosition(view);
                    Log.e(TAG, "onClick at position: " + position);

//                    if (mActionMode == null) {
//                        //TODO: do your stuff
//                    }


                    try {
                        jSelectedItem = jArr.getJSONObject(position);
                        String itemName = jArr.getJSONObject(position).getString("name");
                        tvSheetItemName.setText(itemName);
                        String quantity = "Quantity: 1";
                        tvSheetQuantity.setText(quantity);
                        itemOfferPrice = Integer.parseInt(jArr.getJSONObject(position).getString("offer_price"));
                        String offerPrice = "Price: $" + itemOfferPrice;
                        tvSheetPrice.setText(offerPrice);
                        seekBarSheet.setProgress(1);
                        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//                        if (bottomSheet.getVisibility() == View.GONE && !fab.isShown()) {
//                            bottomSheet.setVisibility(View.VISIBLE);
//                            fab.show();
//                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    }
                }
            });
//            view.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    int position = recyclerView.getChildAdapterPosition(view);
//                    Log.e(TAG, "onLongClick at position: " + position);
//
//                    if (mActionMode != null) {
//                        return false;
//                    }
//
//                    // Start the CAB using the ActionMode.Callback defined above
//                    mActionMode = startSupportActionMode(mActionModeCallback);
//                    view.setSelected(true);
//                    return true;
//                }
//            });
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            try {
                holder.tvName.setText(jArr.getJSONObject(position).getString("name"));
                holder.tvDetail.setText(jArr.getJSONObject(position).getString("description"));
                holder.tvPrice.setText("$" + jArr.getJSONObject(position).getString("price"));
                holder.tvOfferPrice.setText("$" + jArr.getJSONObject(position).getString("offer_price"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        @Override
        public int getItemCount() {
            return jArr.length();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView tvName, tvDetail, tvPrice, tvOfferPrice;

            public ViewHolder(View itemView) {
                super(itemView);
                tvName = (TextView) itemView.findViewById(R.id.tv_sub_menu_item_name);
                tvDetail = (TextView) itemView.findViewById(R.id.tv_sub_menu_item_detail);
                tvPrice = (TextView) itemView.findViewById(R.id.tv_sub_menu_item_price);
                tvOfferPrice = (TextView) itemView.findViewById(R.id.tv_sub_menu_item_offer_price);
            }
        }
    }

    private class FetchMenuTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private ProgressDialog mDialog = new ProgressDialog(SubMenu.this);
        private JSONObject response;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("category_id", categoryId);

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.MENU_LIST.getURL(), mJsonObject);
                boolean status = response != null && response.getInt("is_error") == 0;
                if (status) {
                    jArr = response.getJSONArray("menu");
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
                recyclerAdapter.notifyDataSetChanged();
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
