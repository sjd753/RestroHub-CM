package com.ogma.restrohub.fragment;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ogma.restrohub.R;
import com.ogma.restrohub.application.App;
import com.ogma.restrohub.application.AppSettings;
import com.ogma.restrohub.enums.URL;
import com.ogma.restrohub.network.HttpClient;
import com.ogma.restrohub.network.NetworkConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReservationListFragment extends Fragment {

    private App app;
    private CoordinatorLayout coordinatorLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerAdapter recyclerAdapter;
    private JSONArray jArr = new JSONArray();


    public ReservationListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) getActivity().getApplication();
        app.setAppSettings(new AppSettings(getActivity()));

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reservation_list, container, false);
        coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinator_layout);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryDark, R.color.colorAccent, R.color.colorAccentLight);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (prepareExecuteAsync()) {
                    new FetchReserveTask().execute();
                }
            }
        });

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_reserve);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerAdapter = new RecyclerAdapter();
        recyclerView.setAdapter(recyclerAdapter);

        return view;
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (prepareExecuteAsync()) {
            new FetchReserveTask().execute();
        }
    }

    private boolean prepareExecuteAsync() {
        NetworkConnection connection = new NetworkConnection(getActivity());
        if (connection.isNetworkConnected()) {
            return true;
        } else if (connection.isNetworkConnectingOrConnected()) {
            Snackbar.make(coordinatorLayout, "Connection temporarily unavailable", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(coordinatorLayout, "You're offline", Snackbar.LENGTH_SHORT).show();
        }
        return false;
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reserve_detail, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            //opt means optional.so that if error occured then one text will be skipped rest will work
            holder.tvCustomerName.setText(jArr.optJSONObject(position).optString("customer_name", ""));
            holder.tvArrivalTime.setText(jArr.optJSONObject(position).optString("expected_arrival_time", ""));
            holder.tvPhone.setText(jArr.optJSONObject(position).optString("phone_no", ""));
            holder.tvTotalPerson.setText(jArr.optJSONObject(position).optString("total_person", ""));


        }


        @Override
        public int getItemCount() {
            return jArr.length();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public ImageButton ivBtnAccept, ivBtnDelete;
            private LinearLayout itemHolder;
            private TextView tvCustomerName, tvArrivalTime, tvTotalPerson, tvPhone;
            private Button btnAction, btnMembers;

            public ViewHolder(View itemView) {
                super(itemView);

                tvCustomerName = (TextView) itemView.findViewById(R.id.tv_customer_name);
                tvArrivalTime = (TextView) itemView.findViewById(R.id.tv_arrival_time);
                tvTotalPerson = (TextView) itemView.findViewById(R.id.tv_total_person);
                tvPhone = (TextView) itemView.findViewById(R.id.tv_phone);
                ivBtnAccept = (ImageButton) itemView.findViewById(R.id.iv_btn_reserve_accept);
                ivBtnDelete = (ImageButton) itemView.findViewById(R.id.iv_btn_reserve_cancel);

                ivBtnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e(TAG, "onClick: edit at position " + getAdapterPosition());
                    }
                });

                ivBtnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e(TAG, "onClick: delete at position " + getAdapterPosition());
                    }
                });

            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {

                    default:
                        break;
                }
            }
        }
    }

    private class FetchReserveTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private Snackbar snackbar;
        private JSONObject response;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
            snackbar = Snackbar.make(coordinatorLayout, "Fetching groups...", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("restaurant_id", app.getAppSettings().__uRestaurantId);


                Log.e("Send Obj:", mJsonObject.toString());
                response = HttpClient.SendHttpPost(URL.RESERVE.getURL(), mJsonObject);
                boolean status = response != null && response.getInt("is_error") == 0;
                if (status) {
                    jArr = response.getJSONArray("Reservation");
                } else if (response != null) {
                    error_msg = response.getString("err_msg");
                }
                return status;
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
                snackbar.dismiss();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean status) {
            swipeRefreshLayout.setRefreshing(false);
            snackbar.dismiss();
            if (status) {
                recyclerAdapter.notifyDataSetChanged();
                if (recyclerAdapter.getItemCount() == 0) {
                    Snackbar.make(coordinatorLayout, "No Reserves Found", Snackbar.LENGTH_INDEFINITE).show();
                }
            } else {
                Snackbar.make(coordinatorLayout, error_msg, Snackbar.LENGTH_LONG).show();
            }
        }
    }


}
