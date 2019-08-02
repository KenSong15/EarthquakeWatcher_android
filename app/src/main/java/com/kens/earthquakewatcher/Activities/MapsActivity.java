package com.kens.earthquakewatcher.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kens.earthquakewatcher.Model.Earthquake;
import com.kens.earthquakewatcher.R;
import com.kens.earthquakewatcher.UI.CustomInfoWindow;
import com.kens.earthquakewatcher.Util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private RequestQueue queue;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    private BitmapDescriptor[] iconColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        iconColors = new BitmapDescriptor[] {
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)

        };

        queue = Volley.newRequestQueue(this);
        getEarthquakes();   //get all earthquake and have marker ready
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {   //will be call when the map ready to use
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new CustomInfoWindow(getApplicationContext()));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(" Ken Location", location.toString());


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ask for permission here, as the first boot
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else { //this means we have the permission already and boot second times
            Log.d(" Ken boot: ", "this is the second boot.");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        }

    }

//    get all earthquake object
    public void getEarthquakes(){

        final Earthquake earthquake = new Earthquake();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.week_10_URL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray features = response.getJSONArray("features");
                    for(int i = 0; i < Constants.LIMIT; i++){
                        //get earthquake property
                        JSONObject properties = features.getJSONObject(i).getJSONObject("properties");

                        //get geometry object
                        JSONObject geometry = features.getJSONObject(i).getJSONObject("geometry");
                        //get coordinates array
                        JSONArray coordinates = geometry.getJSONArray("coordinates");

                        double lon = coordinates.getDouble(0);
                        double lat = coordinates.getDouble(1);

                        earthquake.setPlace(properties.getString("place"));
                        earthquake.setLat(lat);
                        earthquake.setLon(lon);
                        earthquake.setType(properties.getString("type"));
                        earthquake.setTime(properties.getLong("time"));
                        earthquake.setMagnitude(properties.getDouble("mag"));
                        earthquake.setDetailLink(properties.getString("detail"));

                        java.text.DateFormat dataFormat = java.text.DateFormat.getDateInstance();
                        String formattedDate = dataFormat.format(new Date(Long.valueOf(properties.getLong("time"))).getTime());

                        MarkerOptions markerOptions = new MarkerOptions();

                        //make the icon here
                        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                        if(earthquake.getMagnitude() >= 2.0){
                            markerOptions.icon(iconColors[2]);

                            //make the circle mark
                            CircleOptions circleOptions = new CircleOptions();
                            circleOptions.center(new LatLng(earthquake.getLat(), earthquake.getLon()));
                            circleOptions.radius(30000);
                            circleOptions.strokeWidth(3.6f);
                            circleOptions.fillColor(Color.RED);
                            mMap.addCircle(circleOptions);
                        }else if (earthquake.getMagnitude() >= 1.5){
                            markerOptions.icon(iconColors[1]);
                        }else{
                            markerOptions.icon(iconColors[0]);
                        }

                        markerOptions.title(earthquake.getPlace());
                        markerOptions.position(new LatLng( lat, lon));
                        markerOptions.snippet("Magnitude: " + earthquake.getMagnitude() + "\n" +
                                                "Date: " + formattedDate);

                        Marker marker = mMap.addMarker(markerOptions);  //make each could be specific
                        marker.setTag(earthquake.getDetailLink());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 1));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(jsonObjectRequest);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        //Toast.makeText(getApplicationContext(),marker.getTitle().toString(), Toast.LENGTH_SHORT).show();
        getQuakeDetails(marker.getTag().toString());
    }

    private void getQuakeDetails(String url) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String detailsUrl = "";

                try {
                    JSONObject properties = response.getJSONObject("properties");
                    JSONObject products = properties.getJSONObject("products");
                    JSONArray geoserve = products.getJSONArray("geoserve");

                    for(int i = 0; i < geoserve.length(); i++){
                        JSONObject geoserveObj = geoserve.getJSONObject(i);
                        JSONObject contentObj = geoserveObj.getJSONObject("contents");
                        JSONObject geoJsonObj = contentObj.getJSONObject("geoserve.json");

                        detailsUrl = geoJsonObj.getString("url");

                    }

                    Log.d("URL: ", detailsUrl);
                    getMoreDetails(detailsUrl);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);
    }

    public void getMoreDetails(String url){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dialogBuilder = new AlertDialog.Builder(MapsActivity.this);
                View view = getLayoutInflater().inflate(R.layout.popup, null);

                Button dismissButton = (Button) view.findViewById(R.id.dismissPop);
                Button dismissTopButton = (Button) view.findViewById(R.id.dismissPoptop);
                TextView popList = (TextView) view.findViewById(R.id.popList);
                WebView htmlPop = (WebView) view.findViewById(R.id.htmlWebview);

                StringBuilder stringBuilder = new StringBuilder();

                try {
                    //for cities list here
                    JSONArray cities = response.getJSONArray("cities");

                    //extract information from array
                    for (int i = 0; i < cities.length(); i++){
                        JSONObject citiesObj = cities.getJSONObject(i);

                        stringBuilder.append("City: "+ citiesObj.getString("name") + "\n" +
                                                "Distance: "+citiesObj.getString("distance")+ "\n" +
                                                "Population: "+citiesObj.getString("population"));
                        stringBuilder.append("\n\n");
                    }
                    popList.setText(stringBuilder.toString());

                    //for html report
                    if(response.has("tectonicSummary") && response.getString("tectonicSummary")!= null){
                        JSONObject tectonic = response.getJSONObject("tectonicSummary");
                        if(tectonic.has("text") && tectonic.getString("text")!=null){
                            String text = tectonic.getString("text");
                            htmlPop.loadDataWithBaseURL(null,text,"text/html", "UTF-8", null);
                        }
                    }


                    //setup dismiss button
                    dismissButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    dismissTopButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });

                    //show the dialog
                    dialogBuilder.setView(view);
                    dialog = dialogBuilder.create();
                    dialog.show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(jsonObjectRequest);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

}
