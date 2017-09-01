package com.ogma.restrohub.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.ogma.restrohub.R;
import com.ogma.restrohub.application.App;
import com.ogma.restrohub.application.AppSettings;
import com.ogma.restrohub.enums.URL;
import com.ogma.restrohub.network.HttpClient;
import com.ogma.restrohub.network.NetworkConnection;
import com.ogma.restrohub.utility.UniversalImageLoaderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Menu extends AppCompatActivity {

    private static final String TAG = Menu.class.getName();

    private App app;
    private android.view.Menu menu;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private RecyclerAdapter recyclerAdapter;
    private CoordinatorLayout coordinatorLayout;
    private NetworkConnection connection;
    private JSONArray jArr = new JSONArray();
    private ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);


        }


        app = (App) getApplication();
        app.setAppSettings(new AppSettings(this));
        connection = new NetworkConnection(this);
        imageLoader = new UniversalImageLoaderFactory.Builder(this).getInstance().initForAdapter().build();

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        recyclerView = (RecyclerView) findViewById(R.id.rv_menu);

        recyclerView.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerAdapter = new RecyclerAdapter();
        recyclerView.setAdapter(recyclerAdapter);

        if (prepareExecuteAsync())
            new FetchCategoryTask().execute();

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
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_menu, menu);
        return true;
    }

    private void promptUser() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Logout");
        adb.setMessage("Do you want to logout?");
        adb.setPositiveButton("LOGOUT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                app.getAppSettings().revokeSession();
                Intent intent = new Intent(Menu.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        adb.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        adb.setCancelable(false);
        adb.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_action_show_as_grid) {
            item.setVisible(false);
            menu.findItem(R.id.menu_action_show_as_list).setVisible(true);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setAdapter(recyclerAdapter);
            return true;
        }

        if (item.getItemId() == R.id.menu_action_show_as_list) {
            item.setVisible(false);
            menu.findItem(R.id.menu_action_show_as_grid).setVisible(true);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(recyclerAdapter);
            return true;
        }

        if (item.getItemId() == R.id.menu_action_cart) {
            startActivity(new Intent(this, Cart.class));
            return true;
        }
        if (item.getItemId() == R.id.menu_logout) {
            promptUser();
            return true;
        }

        if (item.getItemId() == R.id.menu_logout) {
            promptUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = recyclerView.getChildAdapterPosition(view);
                    Log.e(TAG, "onClick at position: " + position);
                    try {
                        startActivity(new Intent(Menu.this, SubMenu.class)
                                .putExtra(SubMenu.EXTRA_ID, jArr.getJSONObject(position).getString("id"))
                                .putExtra(SubMenu.EXTRA_NAME, jArr.getJSONObject(position).getString("name")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            try {
                imageLoader.displayImage(jArr.getJSONObject(position).getString("image"),
                        holder.ivMenu,
                        UniversalImageLoaderFactory.getDefaultOptions(R.drawable.loader));
                holder.tvMenu.setText(jArr.getJSONObject(position).getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        @Override
        public int getItemCount() {
            return jArr.length();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView ivMenu;
            public TextView tvMenu;

            public ViewHolder(View itemView) {
                super(itemView);
                ivMenu = (ImageView) itemView.findViewById(R.id.iv_item_menu);
                tvMenu = (TextView) itemView.findViewById(R.id.tv_item_menu);
            }
        }
    }

    private class FetchCategoryTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private ProgressDialog mDialog = new ProgressDialog(Menu.this);
        private JSONObject response;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("restaurant_id", app.getAppSettings().__uRestaurantId);

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.CATEGORY_LIST.getURL(), mJsonObject);
                boolean status = response != null && response.getInt("is_error") == 0;
                if (status) {
                    jArr = response.getJSONArray("category");
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
