package com.cmpe277.mobileninjas.eventshare;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.cmpe277.mobileninjas.eventshare.model.Constants;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class CreateEventFragment extends Fragment {

    private EditText startDayEditText;
    private EditText endDayEditText;
    private EditText startTimeEditText;
    private EditText endTimeEditText;
    private EditText locationEditText;
    private EditText eventNameEditText;
    private EditText eventAttendeesEditText;
    final Calendar c = Calendar.getInstance();
    String dateFormat = "yyyy-MM-dd";
    SimpleDateFormat df = new SimpleDateFormat( dateFormat, Locale.US );
    String timeFormat = "HH:mm:ss";
    SimpleDateFormat tf = new SimpleDateFormat( timeFormat, Locale.US );
    private View selected;
    Context context;
    public String userId;
    public String userName;
    public String url;
	
	public CreateEventFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getActivity().getApplicationContext();

        View rootView = inflater.inflate(R.layout.content_event, container, false);

        startDayEditText = (EditText) rootView.findViewById(R.id.event_start_day);
        endDayEditText = (EditText) rootView.findViewById(R.id.event_end_day);
        startTimeEditText = (EditText) rootView.findViewById(R.id.event_start_time);
        endTimeEditText = (EditText) rootView.findViewById(R.id.event_end_time);
        locationEditText = (EditText) rootView.findViewById(R.id.event_location);
        eventNameEditText = (EditText) rootView.findViewById(R.id.event_name);
        eventAttendeesEditText = (EditText) rootView.findViewById(R.id.event_attendees);


        userId = MainActivity.getUserID();
        userName = MainActivity.getUserName();

        //userId = "fasfsd"; // for simulation
        //userName = "Bhuvan"; // for simulation
        url = Constants.url;

        intializeView();

        Button button = (Button) rootView.findViewById(R.id.eventButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEvent(v);
            }
        });

        return rootView;
    }


    private void intializeView() {



        setCurrentDateOnView();

        startDayEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected = v;
                new DatePickerDialog(startDayEditText.getContext(), date,
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        endDayEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected = v;
                new DatePickerDialog(startDayEditText.getContext(), date,
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        startTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected = v;
                new TimePickerDialog(startTimeEditText.getContext(), time,
                        c.get(Calendar.HOUR), c.get(Calendar.MINUTE), false).show();
            }
        });
        endTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected = v;
                new TimePickerDialog(endTimeEditText.getContext(), time,
                        c.get(Calendar.HOUR), c.get(Calendar.MINUTE), false).show();
            }
        });

    }


    public void setCurrentDateOnView() {
        if (selected == startDayEditText)
            startDayEditText.setText(df.format(c.getTime()));
        else if (selected == endDayEditText)
            endDayEditText.setText(df.format(c.getTime()));
        else if (selected == startTimeEditText)
            startTimeEditText.setText(tf.format(c.getTime()));
        else if (selected == endTimeEditText)
            endTimeEditText.setText(tf.format(c.getTime()));
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setCurrentDateOnView();
        }
    };

    TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            setCurrentDateOnView();
        }

    };


    public void clearFileds(){

        locationEditText.setText("");
        eventNameEditText.setText("");
        startDayEditText.setText("");
        startTimeEditText.setText("");
        endDayEditText.setText("");
        endTimeEditText.setText("");
        eventAttendeesEditText.setText("");

    }


    public void createEvent(View v) {

        String location = locationEditText.getText().toString();
       final String eventName = eventNameEditText.getText().toString();
       final String startDate = startDayEditText.getText().toString();
       final String startTime = startTimeEditText.getText().toString();
       final String endDate = endDayEditText.getText().toString();
       final String endTime = endTimeEditText.getText().toString();
        String[] attendees = eventAttendeesEditText.getText().toString().split(",");

        String[] participants = new String[attendees.length];

        for(int i=0; i<attendees.length; i++){
            participants[i] = attendees[i].trim();

        }

        Boolean inputsValid = areEmailsValid(attendees) && !location.isEmpty() && !eventName.isEmpty() && !startTime.isEmpty() && !startDate.isEmpty() && !endDate.isEmpty() && !endTime.isEmpty();

        Log.d("INFO:", inputsValid.toString());

        if (!inputsValid) {

            clearFileds();

            Toast.makeText(context, "Please enter valid inputs", Toast.LENGTH_LONG).show();

        }
        else {

            AsyncHttpClient client = new AsyncHttpClient();
            HashMap<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("key", "value");
            RequestParams params = new RequestParams(paramMap);


           final String eventID = String.valueOf(System.currentTimeMillis());

            params.put("userId", userId);
            params.put("userName", userName);
            params.put("startDate", startDate);
            params.put("startTime", startTime);
            params.put("endDate", endDate);
            params.put("endTime", endTime);
            params.put("location", location);
            params.put("eventName", eventName);
            params.put("eventId",eventID);
            params.put("attendees", participants);

            try {


                client.post(context, url + "/createEvent/", params,  new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                        // Event update in Database SQLite

                        Database db = new Database(context);

                        long retValue = db.createEvent(eventID, eventName,userId, startDate + " " + startTime, endDate + " " + endTime);

                        if(retValue>0){

                            //Toast.makeText(context, "DB Record Inserted!!", Toast.LENGTH_LONG).show();

                        }

                        db.close();

                        //Event update in Database SQLite

                        String res = response.toString();
                        Log.d("SUCCESS", "Create Event Success" + response.toString());

                        clearFileds();
                        //Toast.makeText(context, "Event creation success", Toast.LENGTH_LONG).show();

                        // Dialog --- start
                        final Dialog dialog = new Dialog(getActivity());
                        dialog.setContentView(R.layout.dialog);
                        dialog.setTitle("Event Status");


                        TextView text = (TextView) dialog.findViewById(R.id.text);
                        text.setText("Event Created!!");

                        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);

                        dialogButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                        // Dialog -- end
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.d("Failed", "On failure called");

                        clearFileds();

                        Toast.makeText(context, "Event creation Failed!!!", Toast.LENGTH_LONG).show();
                    }


                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    private boolean areEmailsValid(String[] attendees) {
        for (String s : attendees) {
            String email = s.trim();
            if (!TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
                continue;
            else
                return false;
        }
        return true;
    }

}
