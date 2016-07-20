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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import cz.msebera.android.httpclient.Header;


public class EventRequestsFragment extends Fragment {

    public String loggedUserid;
    public String loggedUserName;
    Context context;
    View rootView;

    String url;


    public EventRequestsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        context = getActivity().getApplicationContext();
        url = Constants.url;

        rootView = inflater.inflate(R.layout.pending_request, container, false);

        loggedUserid = MainActivity.getUserID();
        loggedUserName = MainActivity.getUserName();


        //loggedUserid = "fasfsd"; // for simulation
        //loggedUserName = "Bhuvan"; // for simulation


        getPendingRequests(context);

        return rootView;
    }


    public void getPendingRequests(final Context context) {

        AsyncHttpClient client = new AsyncHttpClient();
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("key", "value");
        RequestParams params = new RequestParams(paramMap);
        params.put("userid", loggedUserid);
        try {


            client.get(context, url + "/getPendingEvents/", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                    String message = new String(responseBody);
                    Log.d("eventresponse:", message);
                    //Toast.makeText(getApplicationContext(), "Pending events fetched" , Toast.LENGTH_LONG).show();


                    try {
                        //Get the instance of JSONArray that contains JSONObjects
                        LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout);
                        JSONArray jsonArray = new JSONArray(message);
                        //JSONObject jsonRootObject = new JSONObject(message);
                        //Iterate the jsonArray and print the info of JSONObjects
                        int count = jsonArray.length();
                        if (count > 0) {
                            Toast.makeText(context, "Pending events fetched", Toast.LENGTH_LONG).show();
                            for (int i = 0; i < jsonArray.length(); i++) {

                                final int counter = i;
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
                                // newText.setTextSize(Float.parseFloat("16.0"));
                                newText.setTypeface(null, Typeface.BOLD);
                                newText.setTextColor(Color.parseColor("#000000"));
                                newText.setText(data);
                                newText.setPadding(20, 10, 10, 20);// in pixels (left, top, right, bottom)
                                linearLayout1.addView(newText);
                                ImageButton btn1 = new ImageButton(context);
                                btn1.setId(100 + i);
                                btn1.setImageResource(R.drawable.accept);
                                btn1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                btn1.setPadding(20, 10, 10, 20);
                                btn1.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Toast.makeText(context, "Accept clicked!!", Toast.LENGTH_LONG).show();
                                        Log.d("eventid in onclick:", String.valueOf(id));
                                        acceptEvent(id, counter);


                                        // Event update in Database SQLite

                                        Database db = new Database(context);

                                        long retValue = db.createEvent(id, name,loggedUserid, startTime, endTime);

                                        db.close();


                                        //Event update in Database SQLite
                                    }
                                });
                                linearLayout1.addView(btn1);
                                ImageButton btn2 = new ImageButton(context);
                                btn2.setId(200 + i);
                                btn2.setImageResource(R.drawable.reject);
                                btn2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                btn2.setPadding(20, 10, 10, 20);
                                btn2.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Toast.makeText(context, "Rejected clicked!!", Toast.LENGTH_LONG).show();
                                        Log.d("eventid in onclick:", String.valueOf(id));
                                        rejectEvent(id, counter);
                                    }
                                });
                                linearLayout1.addView(btn2);
                                linearLayout.addView(linearLayout1);
                            }
                        } else {
                            TextView newText = new TextView(context);
                            newText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            // newText.setTextSize(Float.parseFloat("16.0"));
                            newText.setTypeface(null, Typeface.BOLD);
                            newText.setTextColor(Color.parseColor("#FF0000"));
                            newText.setText("No Pending events to dispaly");
                            newText.setPadding(60, 60, 10, 20);// in pixels (left, top, right, bottom)
                            linearLayout.addView(newText);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //requestView.setText(data);

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.d("Failed", "On failure called");
                }
            });


        } catch (Exception e) {

        }

    }


    void acceptEvent(final String eventid, final int counter) {


            /*

        class AcceptsEventsThread implements Runnable {

            String eventid;
            int counter;

            AcceptsEventsThread(String id, int count){

                eventid = id;
                counter = count;

            }

            @Override
            public void run() {

            */




                AsyncHttpClient client = new AsyncHttpClient();
                HashMap<String, String> paramMap = new HashMap<String, String>();
                paramMap.put("key", "value");
                RequestParams params = new RequestParams(paramMap);
                params.put("eventid", eventid);
                params.put("userid", loggedUserid);
                params.put("username", loggedUserName);

                try {
                    client.post(context, url + "/acceptRequest/", params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            String message = response.toString();
                            Toast.makeText(context, "Event accepted", Toast.LENGTH_LONG).show();
                            LinearLayout linearLayout = (LinearLayout) rootView.findViewById(counter);
                            LinearLayout linearLayoutMain = (LinearLayout) rootView.findViewById(R.id.linearLayout);
                            linearLayoutMain.removeView(linearLayout);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.d("Failed", "On failure called");
                            Toast.makeText(context, "Event Accept Failed!!", Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (Exception e) {

                }



        /*

            }
        }


        Thread t = new Thread(new AcceptsEventsThread(eventid,counter));
        t.start();

        */




    }


    void rejectEvent(final String eventid, final int counter) {


            /*

        class RejectEventsThread implements Runnable {

            String eventid;
            int counter;

            RejectEventsThread(String id, int count) {

                eventid = id;
                counter = count;

            }

            @Override
            public void run() {

            */



                AsyncHttpClient client = new AsyncHttpClient();
                HashMap<String, String> paramMap = new HashMap<String, String>();
                paramMap.put("key", "value");
                RequestParams params = new RequestParams(paramMap);
                params.put("eventid", eventid);
                params.put("userid", loggedUserid);
                try {
                    client.post(context, url + "/rejectRequest/", params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            String message = response.toString();
                            Toast.makeText(context, "Event rejected", Toast.LENGTH_LONG).show();
                            LinearLayout linearLayout = (LinearLayout) rootView.findViewById(counter);
                            LinearLayout linearLayoutMain = (LinearLayout) rootView.findViewById(R.id.linearLayout);
                            linearLayoutMain.removeView(linearLayout);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.d("Failed", "On failure called");
                            Toast.makeText(context, "Event rejected Failed!!", Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (Exception e) {

                }


        /*

            }
        }


        Thread t = new Thread(new RejectEventsThread(eventid, counter));
        t.start();

        */


    }




}
