package com.kens.earthquakewatcher.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.VoiceInteractor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kens.earthquakewatcher.Model.Earthquake;
import com.kens.earthquakewatcher.R;
import com.kens.earthquakewatcher.Util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuakesListActivity extends AppCompatActivity {
    private ArrayList<String> arrayList;
    private ListView listView;
    private RequestQueue queue;
    private ArrayAdapter arrayAdapter;

    private List<Earthquake> quakeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quakes_list);

        quakeList = new ArrayList<>();
        listView = (ListView) findViewById(R.id.listview);

        queue = Volley.newRequestQueue(this);

        arrayList = new ArrayList<>();

        getAllQuakes(Constants.week_10_URL);
    }

    public void getAllQuakes(String url){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Earthquake earthquake = new Earthquake();

                try {
                    //JSONArray features = response.getJSONArray()
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


                        arrayList.add(earthquake.getPlace());
                    }

                    arrayAdapter = new ArrayAdapter<>(QuakesListActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, arrayList);
                    listView.setAdapter(arrayAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Toast.makeText(getApplicationContext(), "Clicked on "+ i, Toast.LENGTH_SHORT).show();
                        }
                    });
                    arrayAdapter.notifyDataSetChanged();


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
}
