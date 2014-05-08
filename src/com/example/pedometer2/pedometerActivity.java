package com.example.pedometer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class pedometerActivity extends ActionBarActivity implements SensorEventListener {
	
	private TextView textView;
	private SensorManager mSensorManager;
	private Sensor mStepCounterSensor;
	private Sensor mStepDetectorSensor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textView = (TextView) findViewById(R.id.textView1);
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
		mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
		
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

	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;
		float[] values = event.values;
		int value = -1;
		
		if (values.length > 0) {
		   value = (int) values[0];
		}
		
		if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
		  textView.setText("Step Counter Detected : " + value);
		} else if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
		     // For test only. Only allowed value is 1.0 i.e. for step taken
			textView.setText("Step Detector Detected : " + value);
		}
	}
	
	protected void onResume() {

	     super.onResume();

	     mSensorManager.registerListener(this, mStepCounterSensor,

	           SensorManager.SENSOR_DELAY_FASTEST);      
	     mSensorManager.registerListener(this, mStepDetectorSensor,

	           SensorManager.SENSOR_DELAY_FASTEST);

	 }
	
	protected void onStop() {
	     super.onStop();
	     mSensorManager.unregisterListener(this, mStepCounterSensor);
	     mSensorManager.unregisterListener(this, mStepDetectorSensor);
	 }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

}
