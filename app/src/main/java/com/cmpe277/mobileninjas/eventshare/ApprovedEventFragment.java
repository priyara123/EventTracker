package com.cmpe277.mobileninjas.eventshare;


import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cmpe277.mobileninjas.eventshare.model.Constants;
import com.cmpe277.mobileninjas.eventshare.model.Event;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by BhuvanTeja on 5/5/2016.
 */
public class ApprovedEventFragment extends Fragment {


    Context context;
    private JSONArray eventsJSONArray;
    private List<Event> myApprovedEvents = new ArrayList<Event>();
    public String userId;
    public String userName;
    public String url;
    View rootView;



    public  ApprovedEventFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getActivity().getApplicationContext();

        userId = MainActivity.getUserID();
        userName = MainActivity.getUserName();

        //userId = "fasfsd"; // for simulation
        //userName = "Bhuvan"; // for simulation
        url = Constants.url;

        rootView = inflater.inflate(R.layout.accepted_requests, container, false);

        //populateEventsList();
       // populateListView();
        getAcceptedRequests(context);

        return rootView;
    }


    public void getAcceptedRequests(final Context context) {

        AsyncHttpClient client = new AsyncHttpClient();
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("key", "value");
        RequestParams params = new RequestParams(paramMap);

        params.put("userId", userId);


        try {


            client.get(context, url + "/getApprovedUserEvents/", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                    String message = new String(responseBody);
                    Log.d("eventresponse:", message);


                    try {

                        LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout);
                        JSONArray jsonArray = new JSONArray(message);

                        int count = jsonArray.length();
                        if (count > 0) {
                            Toast.makeText(context, "Approved events fetched", Toast.LENGTH_LONG).show();
                            for (int i = 0; i < jsonArray.length(); i++) {

                                String data = "";
                                JSONObject jsonObject = jsonArray.getJSONObject(i);


                                final String id = jsonObject.optString("event_id").toString();
                                final String name = jsonObject.optString("event_name").toString();
                                String address = jsonObject.optString("address").toString();
                                final String startTime = jsonObject.optString("start_date_time").toString();
                                final String endTime = jsonObject.optString("end_date_time").toString();


                                data += "Event Name : " + name + " \nLocation   : " + address + " \nStart Time : " + startTime + " \nEnd Time : " + endTime;

                                Log.d("eventloop", String.valueOf(i));
                                Log.d("eventloopvalues", data);


                                LinearLayout linearLayout1 = new LinearLayout(context);
                                LinearLayout.LayoutParams layParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                layParams.setMargins(10, 10, 10, 10);
                                linearLayout1.setLayoutParams(layParams);
                                linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
                                linearLayout1.setBackgroundColor(Color.parseColor("#D3D3D3"));
                                linearLayout1.setId(i);
                                TextView newText = new TextView(context);
                                newText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT));
                                newText.setTypeface(null, Typeface.BOLD);
                                newText.setTextColor(Color.parseColor("#000000"));
                                newText.setText(data);
                                newText.setPadding(20, 10, 10, 20);// in pixels (left, top, right, bottom)
                                linearLayout1.addView(newText);
                                linearLayout.addView(linearLayout1);
                            }
                        } else {
                            TextView newText = new TextView(context);
                            newText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            newText.setTypeface(null, Typeface.BOLD);
                            newText.setTextColor(Color.parseColor("#FF0000"));
                            newText.setText("No Accepted events to dispaly");
                            newText.setPadding(60, 60, 10, 20);// in pixels (left, top, right, bottom)
                            linearLayout.addView(newText);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.d("Failed", "On failure called");
                }
            });


        } catch (Exception e) {

        }

    }

}
