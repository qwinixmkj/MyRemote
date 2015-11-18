package com.qwinix.myremote;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView mPinList,mCities;
    private Button mLight, mTemp, mWhether;
    private TextView mPinText, mTempText,mHumidity,mCity,wTemp,wwind;
    private ImageView mOnOffBtn;
    private int pinnumber = 0;
    private boolean[] onoff;
    private HashMap<Integer,Boolean> mapval;
    private LinearLayout lightL1, tempL2,climatel3;
    private ProgressBar mProgress,mProgress1;
    Timer timer;
    TimerTask timerTask;
    private boolean isStart = false;
    private int tmp = 0;
    private int humidity = 0;
    private int whether = 0;
    private String city = "";
    private int wind = 0;



    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ListView listview = (ListView) findViewById(R.id.pins);
        mPinText = (TextView) findViewById(R.id.pinnum);
        mOnOffBtn = (ImageView) findViewById(R.id.btnonoff);
        lightL1 = (LinearLayout) findViewById(R.id.switchcontainer);
        tempL2 = (LinearLayout) findViewById(R.id.tempcontainer);
        climatel3 = (LinearLayout) findViewById(R.id.whethercontainer);
        mTempText = (TextView) findViewById(R.id.temptext);
        mHumidity = (TextView) findViewById(R.id.temptext1);
        wwind = (TextView) findViewById(R.id.wwind);
        mCity = (TextView) findViewById(R.id.wcity);
        wTemp = (TextView) findViewById(R.id.wtemp);
        mLight = (Button) findViewById(R.id.light);
        mTemp = (Button) findViewById(R.id.temp);
        mWhether = (Button) findViewById(R.id.climate);
        mCities = (ListView) findViewById(R.id.city);
        mProgress = (ProgressBar) findViewById(R.id.circularProgressbar);
        mProgress1 = (ProgressBar) findViewById(R.id.circularProgressbar1);
        mLight.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.custom_btn_black_orange));
        mLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lightL1.setVisibility(View.VISIBLE);
                tempL2.setVisibility(View.GONE);
                climatel3.setVisibility(View.GONE);
                stoptimertask(lightL1);
                isStart = false;
                mLight.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.custom_btn_black_orange));
                mTemp.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.custom_btn_black_pearl));
                mWhether.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.custom_btn_black_pearl));
            }
        });
        mTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lightL1.setVisibility(View.GONE);
                tempL2.setVisibility(View.VISIBLE);
                climatel3.setVisibility(View.GONE);
                mTemp.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.custom_btn_black_orange));
                mLight.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.custom_btn_black_pearl));
                mWhether.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.custom_btn_black_pearl));
                new GetTemp().execute();
            }
        });
        mWhether.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lightL1.setVisibility(View.GONE);
                tempL2.setVisibility(View.GONE);
                climatel3.setVisibility(View.VISIBLE);
                stoptimertask(climatel3);
                mWhether.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.custom_btn_black_orange));
                mTemp.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.custom_btn_black_pearl));
                mLight.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.custom_btn_black_pearl));
                new GetWhether().execute();
            }
        });
        mOnOffBtn.setOnClickListener(this);
        final String[] values = new String[] { "Pin 17", "Pin 27", "Pin 22",
                "Pin 5", "Pin 6", "Pin 13", "Pin 19", "Pin 26"};
        final String[] cities = new String[] { "Mysore", "Dubai", "Denver",
                "CostoRica"};
        final int[] mids = new int[] { 17, 27, 22,
                5, 6, 13, 19, 26};
        onoff = new boolean[]{false,false,false,false,false,false,false,false};
        mapval = new HashMap<Integer,Boolean>();
        mapval.put(17,false);
        mapval.put(27,false);
        mapval.put(22,false);
        mapval.put(5,false);
        mapval.put(6,false);
        mapval.put(13,false);
        mapval.put(19,false);
        mapval.put(26,false);

        final ArrayList<String> list = new ArrayList<String>(Arrays.asList(values));
        final ArrayList<String> city = new ArrayList<String>(Arrays.asList(cities));
       /* for (int i = 0; i < values.length; ++i) {
            list.add(values[i]);
        }*/



        final ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, R.layout.mytextview, list){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the current item from ListView
                View view = super.getView(position,convertView,parent);

                // Get the Layout Parameters for ListView Current Item View
                ViewGroup.LayoutParams params = view.getLayoutParams();

                // Set the height of the Item View
                params.height = 200;
                view.setLayoutParams(params);

                return view;
            }
        };

        final ArrayAdapter<String> adapter1 = new ArrayAdapter<String>
                (this, R.layout.mytextview, city){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the current item from ListView
                View view = super.getView(position,convertView,parent);

                // Get the Layout Parameters for ListView Current Item View
                ViewGroup.LayoutParams params = view.getLayoutParams();

                // Set the height of the Item View
                params.height = 200;
                view.setLayoutParams(params);

                return view;
            }
        };
      //  final StableArrayAdapter adapter = new StableArrayAdapter(this,
      //          android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);
        mCities.setAdapter(adapter1);

        mCities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, final int i, long l) {
                view.animate().setDuration(500).alpha(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {

                                view.setAlpha(1);
                                listview.setSelection(i);
                                // view.setBackgroundColor(ContextCompat.getColor(MainActivity.this,R.color.colorAccent));
                            }
                        });
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    final int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);


                    pinnumber = mids[position];
                    // adapter.notifyDataSetChanged();
                    view.animate().setDuration(500).alpha(0)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {

                                    view.setAlpha(1);
                                    mPinText.setText(item);
                                    if (mapval.get(pinnumber)) {
                                        mOnOffBtn.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_btn_active));
                                    } else
                                        mOnOffBtn.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_btn_deactive));
                                    listview.setSelection(position);
                                    // view.setBackgroundColor(ContextCompat.getColor(MainActivity.this,R.color.colorAccent));
                                }
                            });




            }

        });
    }

    @Override
    public void onClick(View view) {

        if (pinnumber <=0) {

            Toast.makeText(MainActivity.this," Please select a pin",Toast.LENGTH_LONG).show();

        }
        else {
            if (mapval.get(pinnumber)) {
                mapval.put(pinnumber, false);
                mOnOffBtn.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_btn_deactive));
                new SetControl().execute();
            } else {
                mapval.put(pinnumber, true);
                mOnOffBtn.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_btn_active));
                new SetControl().execute();
            }
        }
    }

    private class SetControl extends AsyncTask<String, Void, Void> {

        /* to show progress dialog while task is executing */
        private final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        protected void onPreExecute() {
        /*    progressDialog.setCancelable(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setProgress(0);
            progressDialog.show();*/
        }

        protected Void doInBackground(final String... args) {


            try {

                boolean status = mapval.get(pinnumber);

                JSONObject jObj = new JSONObject();
                jObj.put("Pin_number", pinnumber);
                jObj.put("Status",status);

                URL url = new URL("http://192.168.2.112:3000/lights");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
             //   urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type",
                       "application/json");

                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setDoOutput(true); // Set Http method to POST
                urlConnection.setRequestMethod("POST");
               // urlConnection.setChunkedStreamingMode(0); // Use default chunk size



                // Write serialized JSON data to output stream.
                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(jObj.toString());

                // Close streams and disconnect.
                writer.close();
                out.close();


                InputStream is = urlConnection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                urlConnection.disconnect();
                Log.d("mathew",response.toString());

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(final Void unused) {
           /* if(this.dialog.isShowing()) {
                this.dialog.dismiss();
                Toast.makeText(LoginActivity.this, mResetMsg, Toast.LENGTH_LONG).show();
            }*/
        }
    }

    private class GetTemp extends AsyncTask<String,Void,Void>{


        @Override
        protected Void doInBackground(String... strings) {
            try {



                URL url = new URL("http://192.168.2.112:3000/monitor_temp_humidity");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                //   urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type",
                        "application/json");

                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("GET");
                // urlConnection.setChunkedStreamingMode(0); // Use default chunk size



                // Write serialized JSON data to output stream.
              /*  OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(jObj.toString());

                // Close streams and disconnect.
                writer.close();
                out.close();*/

                Log.d("mathew", "temp=1 ");
                InputStream is = urlConnection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                Log.d("mathew", "temp=2 ");
                StringBuffer response = new StringBuffer();
                while((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                urlConnection.disconnect();
                Log.d("mathew", "temp= " + response.toString());
                JSONObject jsonResponse = new JSONObject(response.toString());
                if(jsonResponse.has("Success")) {

                    if (jsonResponse.getBoolean("Success")) {
                        tmp = jsonResponse.getInt("Temperature");
                        humidity = jsonResponse.getInt("Humidity");
                    }
                }


               /* if (tmp >= 100) {
                    tmp = 0;
                }
                tmp += 20;*/



            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isStart) {
                startTimer();
                isStart = true;
            }

            mTempText.setText(""+tmp+ (char) 0x00B0+" C");
            mProgress.setProgress(tmp);
            mHumidity.setText(""+humidity+"%");
            mProgress1.setProgress(humidity);

        }
    }



    private class GetWhether extends AsyncTask<String,Void,Void>{


        @Override
        protected Void doInBackground(String... strings) {
            try {



                URL url = new URL("http://192.168.2.112:3000/get_weather_information");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                //   urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type",
                        "application/json");

                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("GET");
                // urlConnection.setChunkedStreamingMode(0); // Use default chunk size



                // Write serialized JSON data to output stream.
              /*  OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(jObj.toString());

                // Close streams and disconnect.
                writer.close();
                out.close();*/

                InputStream is = urlConnection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                urlConnection.disconnect();
                Log.d("mathew", "temp= " + response.toString());
                JSONObject jsonResponse = new JSONObject(response.toString());
                if(jsonResponse.has("Success")) {

                    if (jsonResponse.getBoolean("Success")) {
                        whether = jsonResponse.getInt("Temperature");
                        city = jsonResponse.getString("City");
                        wind = jsonResponse.getInt("Windspeed");
                    }
                }


               /* if (tmp >= 100) {
                    tmp = 0;
                }
                tmp += 20;*/



            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            /*if (!isStart) {
                startTimer();
                isStart = true;
            }*/

            wTemp.setText("" + whether + (char) 0x00B0 + " C");
           // mProgress.setProgress(tmp);
            mCity.setText(city);
            wwind.setText(""+wind+" km/h");
            //  mProgress1.setProgress(humidity);

        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        //get the current timeStamp
                       /* Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
                        final String strDate = simpleDateFormat.format(calendar.getTime());

                        //show the toast
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(getApplicationContext(), strDate, duration);
                        toast.show();*/

                        new GetTemp().execute();
                    }
                });
            }
        };
    }

    public void stoptimertask(View v) {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 25000, 25000); //
    }
}
