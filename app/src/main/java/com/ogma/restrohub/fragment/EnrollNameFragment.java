package com.ogma.restrohub.fragment;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ogma.restrohub.R;
import com.ogma.restrohub.application.App;
import com.ogma.restrohub.application.AppSettings;
import com.ogma.restrohub.enums.URL;
import com.ogma.restrohub.network.HttpClient;
import com.ogma.restrohub.network.NetworkConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class EnrollNameFragment extends Fragment implements View.OnClickListener {

    private TextInputLayout tilName;
    private TextInputLayout tilPersons;
    private TextInputLayout tilPhone;
    private TextInputLayout tilSpecialRequest;
    private TextInputLayout tilBookTime;

    private TextInputEditText etName;
    private TextInputEditText etPersons;
    private TextInputEditText etPhone;
    private TextInputEditText etSpecialRequest;
    private TextInputEditText etBookTime;
    // private TextView etBookTime;

    private App app;


    public EnrollNameFragment() {
        // Required empty public constructor
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) getActivity().getApplication();
        app.setAppSettings(new AppSettings(getActivity()));

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_enroll_name, container, false);

        tilName = (TextInputLayout) view.findViewById(R.id.til_name);
        tilPersons = (TextInputLayout) view.findViewById(R.id.til_persons);
        tilPhone = (TextInputLayout) view.findViewById(R.id.til_phone);
        tilSpecialRequest = (TextInputLayout) view.findViewById(R.id.til_spcl_request);
        tilBookTime = (TextInputLayout) view.findViewById(R.id.til_book_time);

        etName = (TextInputEditText) view.findViewById(R.id.et_name);
        etPersons = (TextInputEditText) view.findViewById(R.id.et_persons);
        etPhone = (TextInputEditText) view.findViewById(R.id.et_phone);
        etSpecialRequest = (TextInputEditText) view.findViewById(R.id.et_spcl_request);
        etBookTime = (TextInputEditText) view.findViewById(R.id.et_book_time);

        etBookTime.setOnClickListener(this);

        Button btnEnroll = (Button) view.findViewById(R.id.btn_reserve);
        btnEnroll.setOnClickListener(this);

        return view;
    }

    private boolean validate() {
        if (etName.getText().toString().isEmpty()) {
            tilName.setError("Please enter your name");
            return false;
        }
        if (etPhone.getText().toString().isEmpty()) {
            tilPhone.setError("Please enter your phone number");
            return false;
        }
        if (etBookTime.getText().toString().isEmpty()) {
            tilBookTime.setError("Please select a book time");
            return false;
        }

        return true;
    }

    private boolean prepareExecuteAsync() {
        NetworkConnection connection = new NetworkConnection(getActivity());
        if (connection.isNetworkConnected()) {
            return true;
        } else if (connection.isNetworkConnectingOrConnected()) {
            Toast.makeText(getActivity(), "Connection temporarily unavailable", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "You're offline", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void setEnroll() {
        String name = etName.getText().toString();
        String persons = etPersons.getText().toString().trim();
        String phone = etPhone.getText().toString();
        String special_request = etSpecialRequest.getText().toString();
        String book_time = etBookTime.getText().toString();

        EnrollTask enroll = new EnrollTask();
        enroll.execute(name, persons, phone, special_request, book_time);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.et_book_time:
                showDatePicker(etBookTime);
                break;
            case R.id.btn_reserve:
                if (validate() && prepareExecuteAsync())
                    setEnroll();
                break;
            default:
                break;
        }
    }

    private void showDatePicker(final TextInputEditText etBookTime) {
        Calendar calendar = Calendar.getInstance();
        int cYear = calendar.get(Calendar.YEAR);
        int cMonth = calendar.get(Calendar.MONTH);
        int cDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Log.e(TAG, "year:" + year + " month:" + monthOfYear + " day:" + dayOfMonth);

                showTimePicker(etBookTime, year, monthOfYear, dayOfMonth);
            }
        }, cYear, cMonth, cDay);
        datePickerDialog.getDatePicker().setMinDate(new Date().getTime() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker(final TextInputEditText etBookTime, final int year, final int monthOfYear, final int dayOfMonth) {
        final Calendar calendar = Calendar.getInstance();
        int cHour = calendar.get(Calendar.HOUR_OF_DAY);
        int cMinute = calendar.get(Calendar.MINUTE);
        int cSecond = calendar.get(Calendar.SECOND);
        new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Log.e(TAG, "hour:" + hourOfDay + " minute:" + minute);

                Calendar c = Calendar.getInstance();
                c.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);

                SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
                String time = dateFormatter.format(c.getTime());
                etBookTime.setText(time);
            }
        }, cHour, cMinute, false).show();
    }

    private class EnrollTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private String success_msg = "Enrolled successfully";
        private ProgressDialog mDialog = new ProgressDialog(getActivity());
        private JSONObject response;
        private String __uMsg = "";


        //called before doInBackground
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please wait...");
            mDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("restaurant_id", app.getAppSettings().__uRestaurantId);
                mJsonObject.put("customer_name", params[0]);
                mJsonObject.put("total_person", params[1]);
                mJsonObject.put("phone_no", params[2]);
                mJsonObject.put("special_request", params[3]);
                mJsonObject.put("expected_arrival_time", params[4]);

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.ENROLL.getURL(), mJsonObject);

                boolean status = response != null && response.getInt("is_error") == 0;
                if (status) {
                    __uMsg = response.getString("success_msg");
                }
                return status;
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
                mDialog.dismiss();
                return false;
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        //called after doInBackground is completed
        @Override
        protected void onPostExecute(Boolean status) {
            mDialog.dismiss();
            if (status) {
                etName.setText("");
                etPersons.setText("");
                etPhone.setText("");
                etSpecialRequest.setText("");
                etBookTime.setText("");
                Toast.makeText(getActivity(), __uMsg, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Enroll Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
