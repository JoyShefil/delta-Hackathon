package com.example.deltahackathon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {
    LocationManager locationManager;
    private TextToSpeech TTS;
    String cityName,forError;
    String countryName;
    Button usingLocation,enteredManually,enteredGetWeather;
    EditText cityNameEntered;
    EditText countryNameEntered;
    TextView mDescription,mSpeed,mHumidity,mPressure,mTemp,MinMax,mLocationDisplay,LatLon;
    LinearLayout l1;
    RelativeLayout r1;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    FloatingActionButton  fAB;


    float pressure;
    int humidity;
    String description,wind,cityNameOf;
    double temp,temp_min,temp_max,lon,lat;


    private final String url = "https://api.openweathermap.org/data/2.5/weather";
    private final String appId = "4057e7c698982a67fcf5fbdd067f1cbe";
    DecimalFormat decimalFormat = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // cityName = "hi";

        grantPermission();

        l1 = findViewById(R.id.l1);
        r1 = findViewById(R.id.r1);
        usingLocation = findViewById(R.id.usingLocation);
        enteredManually = findViewById(R.id.enterManually);
        cityNameEntered = findViewById(R.id.cityNameEntered);
        countryNameEntered = findViewById(R.id.countryNameEntered);
        enteredGetWeather = findViewById(R.id.enteredGetWeather);
        mDescription = findViewById(R.id.description);
        mSpeed = findViewById(R.id.speed);
        mHumidity = findViewById(R.id.humidity);
        mPressure = findViewById(R.id.pressure);
        mTemp = findViewById(R.id.temp);
        MinMax = findViewById(R.id.MinMax);
        mLocationDisplay = findViewById(R.id.locationDisplay);
        LatLon = findViewById(R.id.LatLon);
        fAB = findViewById(R.id.floatingActionButton);

        TTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                 int result = TTS.setLanguage(Locale.ENGLISH);
                 if (result == TextToSpeech.LANG_MISSING_DATA
                     || result == TextToSpeech.LANG_NOT_SUPPORTED){
                     Log.d("TTS","Language Not Supported");
                 } else {
                     fAB.setEnabled(true);
                 }

                } else {
                    Log.d("TTS","Initialisation Failed");
                }
            }
        });


        //cityName = "hi";

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        cityName = sharedPreferences.getString("cityName",null);
        Log.d("responce1",cityName);


      /*  if (cityName.equals("hi")){
            cityName = sharedPreferences.getString("cityName",null);
        } */






        //declare
        checkLocationIsEnabled();
        getLocation();
        //getWeatherDetails();


        enteredManually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enteredGetWeather.getVisibility() == View.INVISIBLE){
                    enteredGetWeather.setVisibility(View.VISIBLE);
                    cityNameEntered.setVisibility(View.VISIBLE);
                    countryNameEntered.setVisibility(View.VISIBLE);
                }
            }
        });
        usingLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //grantPermission();
                //checkLocationIsEnabled();
                //getLocation();
                    getWeatherDetails();
                    // setTextsToView();
                    l1.setVisibility(View.INVISIBLE);
                    r1.setVisibility(View.VISIBLE);


            }
        });

        enteredGetWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cityName = cityNameEntered.getText().toString().trim();
                countryName = countryNameEntered.getText().toString().trim();
                getWeatherDetails();
                //setTextsToView();
                l1.setVisibility(View.INVISIBLE);
                r1.setVisibility(View.VISIBLE);
                enteredGetWeather.setVisibility(View.INVISIBLE);
                cityNameEntered.setVisibility(View.INVISIBLE);
                countryNameEntered.setVisibility(View.INVISIBLE);
            }
        });

        fAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });

       //checkLocationIsEnabled();
        //getLocation();
    }

    public void getWeatherDetails() {
        String tempUrl = "";
        if (cityName.equals("")){
            Toast.makeText(this, "Enter City Name", Toast.LENGTH_SHORT).show();
        } else {
            if (!countryName.equals("")){
                tempUrl = url + "?q=" + cityName + "," +countryName+ "&appid=" + appId;
            } else {
                tempUrl = url + "?q=" + cityName + "&appid=" + appId;
            }
        }
        StringRequest stringRequest =new StringRequest(Request.Method.POST, tempUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("responce",response);
                String output = "";
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                    JSONObject jsonWeather = jsonArray.getJSONObject(0);
                    description = jsonWeather.getString("description");
                    JSONObject jsonMain = jsonResponse.getJSONObject("main");
                    temp = jsonMain.getDouble("temp") - 273.15;
                    //double feelsLike = jsonMain.getDouble("feels_like") - 273.15;
                    pressure = jsonMain.getInt("pressure");
                    humidity = jsonMain.getInt("humidity");
                    JSONObject jsonWind = jsonResponse.getJSONObject("wind");
                    wind = jsonWind.getString("speed");
                    cityNameOf = jsonResponse.getString("name");
                    temp_min = jsonMain.getDouble("temp_min") - 273.15;
                    temp_max = jsonMain.getDouble("temp_max") - 273.15;
                    JSONObject jsonCoord = jsonResponse.getJSONObject("coord");
                    lon = jsonCoord.getDouble("lon");
                    lat = jsonCoord.getDouble("lat");
                      Log.d("responce",description);

                    mDescription.setText(description);
                    mSpeed.setText(wind+"m/s");
                    mHumidity.setText(humidity + "%");
                    mPressure.setText(pressure + "hPa");
                    mTemp.setText(decimalFormat.format(temp) + " c");
                    MinMax.setText(decimalFormat.format(temp_min) + " c/"+ decimalFormat.format(temp_max)+" c");
                    mLocationDisplay.setText(cityNameOf);
                    LatLon.setText(decimalFormat.format(lat) + "/"+ decimalFormat.format(lon));


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString().trim(), Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);

    }

    private void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5,(LocationListener) this);

        } catch (SecurityException e){
            e.printStackTrace();
        }
    }

    private void checkLocationIsEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnable = false;
        boolean networkEnabled = false;


        try {
            gpsEnable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch (Exception e){
            e.printStackTrace();
        }

        if (!gpsEnable && !networkEnabled){
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Enable GPS Service")
                    .setCancelable(false)
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    }).setNegativeButton("Cancel",null)
                    .show();
        }




    }

    private void grantPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION},250);
        }
    }

    public void setTextsToView(){
        mDescription.setText(description);
        mSpeed.setText(wind+"m/s");
        mHumidity.setText(humidity + "%");
        mPressure.setText(pressure + "hPa");
        mTemp.setText(decimalFormat.format(temp) + " c");
        MinMax.setText(decimalFormat.format(temp_min) + " c/"+ decimalFormat.format(temp_max)+" c");
        mLocationDisplay.setText(cityNameOf);
        LatLon.setText(decimalFormat.format(lat) + "/"+ decimalFormat.format(lon));
       // Log.d("responce",description);




    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            cityName = addresses.get(0).getLocality();
            countryName = addresses.get(0).getCountryName();
            Log.d("responce",cityName);
            editor = sharedPreferences.edit();
            editor.putString("cityName",cityName);
            editor.apply();
            Log.d("responce",sharedPreferences.getString("cityName",null));


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onBackPressed(){
        cityName = sharedPreferences.getString("cityName",null);
        if (r1.getVisibility() == View.VISIBLE){
            r1.setVisibility(View.INVISIBLE);
            l1.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }

    }
    private void speak(){

        String text = "Weather at " + cityName + "is" + description + "and Temperature is" + temp + "degree celsius"  ;
        TTS.setPitch(2.0f);
        TTS.setSpeechRate(1.1f);

        TTS.speak(text,TextToSpeech.QUEUE_FLUSH,null);
    }

    @Override
    protected void onDestroy() {
        cityName = sharedPreferences.getString("cityName",null);
        if (TTS != null){
            TTS.stop();
            TTS.shutdown();
        }
        super.onDestroy();
    }
}