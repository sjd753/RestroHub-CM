package com.ogma.restrohub.activity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ogma.restrohub.R;
import com.ogma.restrohub.application.App;
import com.ogma.restrohub.application.AppSettings;
import com.ogma.restrohub.enums.URL;
import com.ogma.restrohub.model.Table;
import com.ogma.restrohub.network.HttpClient;
import com.ogma.restrohub.network.NetworkConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TableActivity extends AppCompatActivity {

    private App app;
    private NetworkConnection connection;
    private CoordinatorLayout coordinatorLayout;
    private RecyclerAdapter recyclerAdapter;
    private ArrayList<Table> tables = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

        }

        app = (App) getApplication();
        app.setAppSettings(new AppSettings(this));
        connection = new NetworkConnection(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_table);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerAdapter = new RecyclerAdapter();
        recyclerView.setAdapter(recyclerAdapter);


        if (prepareExecuteAsync())
            new TablesTask().execute();

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

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.table_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Table table = tables.get(position);
            holder.tvTitle.setText(table.getName());
        }

        @Override
        public int getItemCount() {
            return tables.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvTitle;

            MyViewHolder(View itemView) {
                super(itemView);
                tvTitle = (TextView) itemView.findViewById(R.id.tv_title);

            }
        }
    }

    private class TablesTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private ProgressDialog mDialog = new ProgressDialog(TableActivity.this);
        private JSONObject response;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("restaurant_id", app.getAppSettings().__uRestaurantId);

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.TABLE_LIST.getURL(), mJsonObject);
                boolean status = response != null && response.getInt("is_error") == 0;
                if (status) {
                    //   jArr = response.getJSONArray("tables");
                    JSONArray jsonArrayTabList = response.getJSONArray("tables");
                    for (int i = 0; i < jsonArrayTabList.length(); i++) {
                        JSONObject tableObject = jsonArrayTabList.getJSONObject(i);
                        Table table = new Table(
                                tableObject.getString("id"),
                                tableObject.getString("name")
                        );
                        tables.add(table);
                    }
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
