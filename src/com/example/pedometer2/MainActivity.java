package com.example.pedometer2;


import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends Activity implements SensorEventListener{

	private SensorManager sensorManager;
	int x_pos = 5;
	int y_pos = 5;
	
	int[][] game_world = new int[10][10];
	Sensor stepSensor;
	Sensor magnetometer;
	Sensor accelerometer;
	
	float azimut;
	float[] mGravity;
	float[] mGeomagnetic;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		//Creating sensors and checking for support
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
		magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	      
	      if (stepSensor == null){
            Toast.makeText(this, "Step sensor aint supported bro!", Toast.LENGTH_LONG).show();		
		}else{
            Toast.makeText(this, "Step sensor ais totally supported bro!", Toast.LENGTH_LONG).show();		
		}
		if (magnetometer == null || accelerometer == null){
            Toast.makeText(this, "Compass aint supported bro!", Toast.LENGTH_LONG).show();		
		}else{
            Toast.makeText(this, "Compass is totally supported bro! Alright!", Toast.LENGTH_LONG).show();		
		}
		
		//Register us as the event handler for the compass
		sensorManager.registerListener(this, magnetometer,SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_UI);

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		      mGravity = event.values;
		    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
		      mGeomagnetic = event.values;
		   
		    if (mGravity != null && mGeomagnetic != null) {
		      float R[] = new float[9];
		      float I[] = new float[9];
		      boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
		      if (success) {
		        float orientation[] = new float[3];
		        SensorManager.getOrientation(R, orientation);
		        azimut = orientation[0]; // orientation contains: azimut, pitch and roll
	            Toast.makeText(this, Float.toString(azimut), Toast.LENGTH_LONG).show();		
		      }
		    }		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}


}
