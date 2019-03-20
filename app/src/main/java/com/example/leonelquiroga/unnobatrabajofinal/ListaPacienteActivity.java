package com.example.leonelquiroga.unnobatrabajofinal;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.leonelquiroga.unnobatrabajofinal.adapter.AdapterRecycler;
import com.example.leonelquiroga.unnobatrabajofinal.adapter.Paciente;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListaPacienteActivity extends AppCompatActivity implements View.OnClickListener {

    private FusedLocationProviderClient fusedLocationClient;
    private RecyclerView listaRecycler;
    private AdapterRecycler adapter;
    private FloatingActionButton btnCerrarSesion;

    private static String URL = "http://ppc.edit.com.ar:8080/resources/datos/pacientes/";

    private Gson gson;
    private RequestQueue colaRequest;
    private List<Paciente> listaPaciente;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private AlertDialog alertaGPS = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_paciente);

        getSupportActionBar().hide();

        preferences = getSharedPreferences("preferencias", Context.MODE_PRIVATE);

        btnCerrarSesion = findViewById(R.id.cerrarSesion);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        listaRecycler = findViewById(R.id.recyclerView);
        listaRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        btnCerrarSesion.setOnClickListener(this);

        localizacion();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.cerrarSesion:
                editor = preferences.edit();
                editor.clear();
                editor.commit();
                setIntentWithFinish(this, LoginActivity.class);
                break;
        }
    }

    public void setIntentWithFinish(Context c1, Class c2) {
        Intent intent = new Intent(c1, c2);
        startActivity(intent);
        finish();
    }

    private void completarLista(String latitud, String longitud) {
        this.colaRequest = Volley.newRequestQueue(this);
        gson = new Gson();
        listaPaciente = new ArrayList<>();

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL + latitud + "/" + longitud, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        listaPaciente = Arrays.asList(gson.fromJson(response.toString(), Paciente[].class));
                        if(listaPaciente != null && !listaPaciente.isEmpty()) {
                            adapter = new AdapterRecycler(listaPaciente);
                            listaRecycler.setAdapter(adapter);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        colaRequest.add(request);
    }

    private void localizacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        completarLista(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1000: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    localizacion();
                }
                break;
            }
        }
    }

    /*private void activarGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("El GPS está desactivado, ¿Desea activarlo?")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        alertaGPS = builder.create();
        alertaGPS.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        localizacion();
    }

    @Override
    protected void onPause() {
        super.onPause();
        return;
    }*/
}
