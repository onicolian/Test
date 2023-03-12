package com.onicolian.test;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class MainActivity extends Activity {
    com.google.api.services.calendar.Calendar mService;

    GoogleAccountCredential credential;
    private TextView mStatusText;
    private TextView mResultsText;
    private EditText link;
    public Button add;
    public Button parseButton;
    public CheckBox checkBox;
    public TableLayout tableLayout;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };
    public Elements contentHeader;
    public Elements contentP;
    public ArrayList<String> titleList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ListView lv;
    public ArrayList<ArrayList<String[]>> listArrayList;
    public String[] Headers;
    public String[] Place;
    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mStatusText = findViewById(R.id.mStatusText);
        mResultsText = findViewById(R.id.mResultsText);
        link = (EditText) findViewById(R.id.link);
        lv = findViewById(R.id.listView);
        checkBox = findViewById(R.id.checkBox);
        add = findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please parse", Toast.LENGTH_SHORT).show();
                } else {
                    startThread();
                }

            }
        });

        parseButton = (Button) findViewById(R.id.parse);

        parseButton.setOnClickListener(
                view -> {
                    String enteredData = link.getText().toString();
                    if (enteredData.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Please Enter the Data", Toast.LENGTH_SHORT).show();
                    } else {
                        new NewThread().execute();
                        adapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.pro_item, titleList);
                    }
                });
        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        credential = GoogleAccountCredential.usingOAuth2(
                        getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Google Calendar API Android Quickstart")
                .build();
    }

    public void startThread()
    {
        Thread backgroundthread =new Thread( new Runnable()
        {

            public void run()
            {
                for(int i = 0; i<listArrayList.size(); i++){
                    String day = "";
                    for(int j = 0; j < listArrayList.get(i).size(); j++){
                        if (listArrayList.get(i).get(j)[1] == null){
                            day = listArrayList.get(i).get(j)[0];
                        }
                        else{
                            Event event = new Event()
                                .setSummary(listArrayList.get(i).get(j)[1])
                                .setLocation(listArrayList.get(i).get(j)[2] + " " + Headers[i])
                                    .setDescription(Place[i]);

                            day = day.replace(",", "");
                            String[] sDay = day.split(" ");
                            String str = listArrayList.get(i).get(j)[0].replace(" ", "");
                            String[] time = str.split("[-–]");

                            java.util.Calendar calendar = new GregorianCalendar();
                            calendar.set(java.util.Calendar.YEAR, Integer.parseInt(sDay[3])+1);
                            calendar.set(java.util.Calendar.MONTH, 11);
                            calendar.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(sDay[2]));
                            calendar.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(time[0].split(":")[0]));
                            calendar.set(java.util.Calendar.MINUTE, Integer.parseInt(time[0].split(":")[1]));
                            Date d = calendar.getTime();

                            DateTime startDateTime = new DateTime(d);
                            EventDateTime start = new EventDateTime()
                                    .setDateTime(startDateTime)
                                    .setTimeZone("Europe/Moscow");
                            event.setStart(start);

                            if(time.length > 1) {
                                calendar = new GregorianCalendar();
                                calendar.set(java.util.Calendar.YEAR, Integer.parseInt(sDay[3]) + 1);
                                calendar.set(java.util.Calendar.MONTH, 11);
                                calendar.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(sDay[2]));
                                calendar.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(time[1].split(":")[0]));
                                calendar.set(java.util.Calendar.MINUTE, Integer.parseInt(time[1].split(":")[1]));
                                d = calendar.getTime();

                                DateTime endDateTime = new DateTime(d);
                                EventDateTime end = new EventDateTime()
                                        .setDateTime(endDateTime)
                                        .setTimeZone("Europe/Moscow");
                                event.setEnd(end);
                            }
                            else{
                                calendar = new GregorianCalendar();
                                calendar.set(java.util.Calendar.YEAR, Integer.parseInt(sDay[3]) + 1);
                                calendar.set(java.util.Calendar.MONTH, 11);
                                calendar.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(sDay[2]));
                                calendar.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(time[0].split(":")[0]));
                                calendar.set(java.util.Calendar.MINUTE, Integer.parseInt(time[0].split(":")[1]));
                                d = calendar.getTime();

                                DateTime endDateTime = new DateTime(d);
                                EventDateTime end = new EventDateTime()
                                        .setDateTime(endDateTime)
                                        .setTimeZone("Europe/Moscow");
                                event.setEnd(end);}

                            if (checkBox.isChecked()){
                                EventReminder[] reminderOverrides = new EventReminder[] {
                                        new EventReminder().setMethod("email").setMinutes(24 * 60),
                                        new EventReminder().setMethod("popup").setMinutes(10),
                                };
                                Event.Reminders reminders = new Event.Reminders()
                                        .setUseDefault(false)
                                        .setOverrides(Arrays.asList(reminderOverrides));
                                event.setReminders(reminders);

                            }

                            String calendarId = "primary";
                            try {
                                mService.events().insert(calendarId, event).execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });backgroundthread.start();
    }

    public class NewThread extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... arg){
            Document doc;
            try {
                doc = Jsoup.connect(link.getText().toString()).get();

                contentHeader = doc.select("h2");
                Headers = new String[3];
                for (int j = 0; j < contentHeader.size(); j++) {
                    Element el = contentHeader.get(j);
                    String text = String.valueOf(el);
                    Headers[j] = text.substring(text.indexOf('>') + 1, text.indexOf("</"));
                }

                contentP = doc.select("p");
                Place = new String[3];
                for (int j = 0; j < contentP.size(); j++) {
                    Element el = contentP.get(j);
                    String text = String.valueOf(el);
                    Place[j] = text.substring(text.indexOf('>') + 1, text.indexOf("</"));
                }

                listArrayList = new ArrayList<>();
                for (Element contents: doc.select(".ttable")){

                    ArrayList<String[]> singleList = new ArrayList<>();
                    Elements rows = contents.select("tr");

                    for (int i = 0; i < rows.size(); i++) {
                        Element row = rows.get(i);
                        Elements cols = row.select("td");
                        String[] arr = new String[3];
                        for (int j = 0; j < cols.size(); j++) {
                            Element el = cols.get(j);
                            String text = String.valueOf(el);
                            arr[j] = text.substring(text.indexOf('>') + 1, text.indexOf("</"));
                        }
                        singleList.add(arr);
                    }
                    listArrayList.add(singleList);
                }

                titleList.clear();
                for (ArrayList<String[]> contents: listArrayList){
                    for (String[] content: contents){
                        for (String con: content){
                        titleList.add(con);
                        }
                    }
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        protected  void onPostExecute (String result){
            lv.setAdapter(adapter);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshResults();
        } else {
            mStatusText.setText("Google Play Services required: " +
                    "after installing, close and relaunch this app.");
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK) {
                    refreshResults();
                } else {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        refreshResults();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    mStatusText.setText("Account unspecified.");
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    refreshResults();
                } else {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void refreshResults() {
        if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new ApiAsyncTask(this).execute();
            } else {
                mStatusText.setText("No network connection available.");
            }
        }
    }

    public void clearResultsText() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusText.setText("Retrieving data…");
                mResultsText.setText("");
            }
        });
    }

    public void updateResultsText(final List<String> dataStrings) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dataStrings == null) {
                    mStatusText.setText("Error retrieving data!");
                } else if (dataStrings.size() == 0) {
                    mStatusText.setText("No data found.");
                } else {
                    mStatusText.setText("Data retrieved using" +
                            " the Google Calendar API:");
                    mResultsText.setText(TextUtils.join("\n\n", dataStrings));
                }
            }
        });
    }

    public void updateStatus(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusText.setText(message);
            }
        });
    }

    private void chooseAccount() {
        startActivityForResult(
                credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode,
                        MainActivity.this,
                        REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

}