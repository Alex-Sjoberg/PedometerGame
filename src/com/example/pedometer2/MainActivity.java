package com.example.pedometer2;


import java.util.Date;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
implements SensorEventListener{
 
SensorManager sensorManager;
private Sensor sensorAccelerometer;
private Sensor sensorGyroscope;
 
private float[] valuesAccelerometer;
private float[] valuesGyroscope;
double player_heading = 0;

long last_sensor_time;
 

private float[] gravity;
private float[] linear_acceleration;

TextView gyro_x, gyro_y, gyro_z, azimuth, heading;
 
/** Called when the activity is first created. */
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    this.gyro_x = (TextView)findViewById(R.id.gyro_x);
    this.gyro_x = (TextView)findViewById(R.id.gyro_y);
    this.gyro_x = (TextView)findViewById(R.id.gyro_z);
    this.azimuth = (TextView)findViewById(R.id.azimuth);
    this.heading = (TextView)findViewById(R.id.heading);

    gravity = new float[3];
    linear_acceleration = new float[3];

    //readingPitch = (TextView)findViewById(R.id.pitch);
    //readingRoll = (TextView)findViewById(R.id.roll);
    
    
    sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
    sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    if (sensorGyroscope == null){
        Toast.makeText(this, "Aint got no gyroscope", Toast.LENGTH_LONG).show();

    }else{
        Toast.makeText(this, "You got gyroscope!", Toast.LENGTH_LONG).show();
    }
    
 valuesAccelerometer = new float[3];
 valuesGyroscope = new float[3];

}

@Override
protected void onResume() {

sensorManager.registerListener(this,
  sensorAccelerometer,
  SensorManager.SENSOR_DELAY_UI);
sensorManager.registerListener(this,
  sensorGyroscope,
  SensorManager.SENSOR_DELAY_UI);

last_sensor_time = System.currentTimeMillis();

super.onResume();
}

@Override
protected void onPause() {

sensorManager.unregisterListener(this,
  sensorAccelerometer);
sensorManager.unregisterListener(this,
  sensorGyroscope);
super.onPause();
}

@Override
public void onAccuracyChanged(Sensor arg0, int arg1) {
// TODO Auto-generated method stub
  
}

@Override
public void onSensorChanged(SensorEvent event) {
// TODO Auto-generated method stub
  
switch(event.sensor.getType()){
case Sensor.TYPE_ACCELEROMETER:

	
 final float alpha = (float) 0.8;

 gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
 gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
 gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

 linear_acceleration[0] = event.values[0] - gravity[0];
 linear_acceleration[1] = event.values[1] - gravity[1];
 linear_acceleration[2] = event.values[2] - gravity[2];

 this.azimuth.setText("Azimuth:" + String.valueOf(linear_acceleration[0]));

 break;
case Sensor.TYPE_GYROSCOPE:
 //get time interval between readings
 long now = System.currentTimeMillis();
 float time_since_update_milli = now - last_sensor_time;



 
 for(int i =0; i < 3; i++){
  valuesGyroscope[i] = event.values[i];
 }
 
 double deg_rot = (double) (valuesGyroscope[2] * (180/Math.PI));
 deg_rot /= 1000;
 if(time_since_update_milli != 0){
	 player_heading += (deg_rot * time_since_update_milli);
	 player_heading %= 360;
 }else{
	 player_heading = -800;
 }
 
 this.gyro_x.setText("Gyroscope X:" + String.valueOf(deg_rot));
 this.heading.setText("Player Heading:" + String.valueOf(player_heading));
 last_sensor_time = System.currentTimeMillis();


 break;
}
}
  
}