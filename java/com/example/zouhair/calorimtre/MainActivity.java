package com.example.zouhair.calorimtre;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private GPSTracker gps;
    private double cals = -1;
    private double lastLat = -1;
    private double lastLong = -1;
    private boolean isRecording = false;
    private TextView text;
    private TextView timeText;
    private Button start;
    private Button map;
    private String MESSAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart(){
        super.onStart();

        // Récupération des données

        text = findViewById(R.id.text);
        start = findViewById(R.id.startButton);
        map = findViewById(R.id.mapButton);
        gps = new GPSTracker(this);
        cals=0;
        MESSAGE = this.getString(R.string.message);
        timeText= findViewById(R.id.timeText);

        if(gps.canGetLocation()){
            lastLat = gps.getLatitude();
            lastLong = gps.getLongitude();
        }
        else{
            Toast.makeText(MainActivity.this,MESSAGE,Toast.LENGTH_LONG).show();
        }


        // Manipulation du bouton Start

        start.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                // Définition du thread qui enregistre les calories

                Thread Calories = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        upDateCals();
                    }
                });

                // Définition du thread qui affiche le nombre de secondes écoulées

                Thread Time = new Thread(new Runnable(){
                    @Override
                    public void run() {
                        upDateTime();
                    }
                });

                if(isRecording){
                    start.setText("Start\n Recording");
                    isRecording=false;
                }
                else{
                    start.setText("Stop\n Recording");
                    isRecording=true;
                    timeText.setText("0");
                    Calories.start();
                    Time.start();
                }

            }
        });

        // Manipulation du bouton Map

        map.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(gps.canGetLocation()){
                    Intent mapIntent = new Intent(MainActivity.this,MapsActivity.class);
                    mapIntent.putExtra("latitude",lastLat);
                    mapIntent.putExtra("longitude",lastLong);
                    startActivity(mapIntent);
                }
                else{
                Toast.makeText(MainActivity.this,MESSAGE,Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    public void onDestroy() {
        super.onDestroy();
        gps.stopUsingGPS();
        return;
    }

    // Cette méthode met à jour le nombre de calories brulées à partir des données de géolocalisation

    public void upDateCals() {

        while(isRecording) {

            if (gps.canGetLocation()) {
                double d = gps.getDistance(lastLat, lastLong);
                lastLat = gps.getLatitude();
                lastLong = gps.getLongitude();
                cals += 35*d/750;
                runOnUiThread(new Thread(new Runnable(){
                    @Override
                    public void run(){
                        text.setText(Double.toString(cals));
                    }
                }));

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            else {
                Toast.makeText(MainActivity.this,MESSAGE, Toast.LENGTH_LONG).show();
                break;
            }
        }
        return;
    }

    // Cette méthode met à jour le temps écoulé depuis le début de l'enregistrement

    public void upDateTime(){

        while(isRecording){

            runOnUiThread(new Thread(new Runnable(){
                @Override
                public void run(){
                    int n = Integer.parseInt(timeText.getText().toString());
                    timeText.setText(Integer.toString(n+1));
                    return;
                }
            }));

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        return;
    }
}
