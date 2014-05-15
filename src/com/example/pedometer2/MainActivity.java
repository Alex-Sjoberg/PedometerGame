package com.example.pedometer2;


import java.util.Date;
import java.util.Random;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
implements SensorEventListener{
	 
	SensorManager sensorManager;
	private Sensor sensorAccelerometer;
	private Sensor sensorGyroscope;
    private Sensor magnetometer;

	private Sensor mStepCounterSensor;
	private Sensor mStepDetectorSensor;
	
	private TextView step_count_view;
	
	private float[] gravity;
	private float[] linear_acceleration;
	private float[] valuesGyroscope;
	
	long last_sensor_time;
	long last_step_time;
	
	final double STEP_ACCEL_THRESHOLD = 0.8;
	final long STEP_TIME_THRESHOLD = 1000; //in milliseconds
	
	int accel_step_count = 0;
	
	int player_x = 5;
	int player_y = 5;
	double player_heading = 0;
	
	int goal_x;
	int goal_y;
	
	int score;
	
	//compass info
	float azimut;
	float[] mGravity;
	float[] mGeomagnetic;
	
	private Random rand;
	
	private TextView player_x_view;
	private TextView player_y_view;
	private TextView goal_x_view;
	private TextView goal_y_view;
	private TextView score_view;
	private TextView compass_heading_view;
	
    private ImageView rotating_image;
    private LinearLayout rotating_layout;
    private TableLayout rotating_table;


	private Button move_button;
	private Button pause_button;
	private Button resume_button;
	
	private static int[][] dir_translations = {
	    {-1,1},{0,1},{1,1},
	    {-1,0},{0,0},{1,0},
	    {-1,-1},{0,-1},{1,-1}
	};
	
	private static int GRID_HEIGHT = 6;
	private static int GRID_WIDTH = 6;
	
	TextView gyro_x, gyro_y, gyro_z, azimuth, heading;
	 
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
	    gyro_x = (TextView)findViewById(R.id.gyro_z);
	    azimuth = (TextView)findViewById(R.id.azimuth);
		step_count_view = (TextView) findViewById(R.id.step_count);
		
	    heading = (TextView)findViewById(R.id.heading);
	    compass_heading_view = (TextView)findViewById(R.id.compass_heading);
		player_x_view = (TextView) findViewById(R.id.player_x);
		player_y_view = (TextView) findViewById(R.id.player_y);
		
        rotating_layout = (LinearLayout) findViewById(R.id.rotating_layout);
        rotating_table = (TableLayout) findViewById(R.id.rotating_table);
        
		goal_x_view = (TextView) findViewById(R.id.goal_x);
		goal_y_view = (TextView) findViewById(R.id.goal_y);
		score_view = (TextView) findViewById(R.id.score);
		
		move_button = (Button) findViewById(R.id.move_button);
		pause_button = (Button) findViewById(R.id.pause_button);
		resume_button = (Button) findViewById(R.id.resume_button);
		
		for (int row = 0; row < GRID_HEIGHT; row++){
			TableRow line = new TableRow(this);
			line.setId(row);
			
			for (int col = 0; col < GRID_WIDTH; col++){
				ImageView new_view = new ImageView(this);
				new_view.setTag("table[" + Integer.toString(row) + "][" + Integer.toString(col) + "]");
				if (player_y == row && player_x == col){
					new_view.setImageResource(R.drawable.character);
				}else{
					new_view.setImageResource(R.drawable.tile);
				}
				line.addView(new_view);
			}
			rotating_table.addView(line);
		}
		
		
	    gravity = new float[3];
	    linear_acceleration = new float[3];
	    
	    
	    sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	    sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		mStepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
		mStepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

	    if (sensorGyroscope == null){
	        Toast.makeText(this, "Aint got no gyroscope", Toast.LENGTH_LONG).show();
	
	    }else{
	        Toast.makeText(this, "You got gyroscope!", Toast.LENGTH_LONG).show();
	    }
	    
		 valuesGyroscope = new float[3];
		 
		 move_button.setOnClickListener(new View.OnClickListener() {
		     public void onClick(View v) {
		         move_player(player_heading);
		     }
		 });
		 pause_button.setOnClickListener(new View.OnClickListener() {
		     public void onClick(View v) {
		         pause_tracking();
		     }
		 });
		 resume_button.setOnClickListener(new View.OnClickListener() {
		     public void onClick(View v) {
		         resume_tracking();
		     }
		 });
	
	}
	
	@Override
	protected void onResume() {
		rand = new Random();
		generate_goal();
		register_sensors();
		super.onResume();
	}
	
	@Override
	protected void onPause() {
	
		super.onPause();
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	// TODO Auto-generated method stub
	  
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		
		float[] values = event.values;
		int value = -1;
		
		if (values.length > 0) {
		   value = (int) values[0];
		}
	  
		switch(event.sensor.getType()){
		case Sensor.TYPE_ACCELEROMETER:
			 process_accelerometer_data(event);
			 do_compass_calculations(event);
			 break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			do_compass_calculations(event);
			break;
		case Sensor.TYPE_GYROSCOPE:
			process_gyroscope_data(event);
			break;
			
		case Sensor.TYPE_STEP_COUNTER:
			step_count_view.setText("Step Counter Detected : " + value);
			move_player(player_heading);
			break;
		case Sensor.TYPE_STEP_DETECTOR:
			// For test only. Only allowed value is 1.0 i.e. for step taken
			step_count_view.setText("Step Detector Detected : " + value);
		}
		
		do_compass_calculations(event);
	}
	
	protected void do_compass_calculations(SensorEvent event){
    	//Do method 2: not deprecated
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
	        int deg = (int)(azimut * (float) 57.295);
            compass_heading_view.setText("Compass heading: " + Integer.toString(deg));
	      }
	    }
	}

	protected int[] get_translation_from_heading(double heading){
		if (heading >=  337.5 || heading < 22.5){
			return dir_translations[5];
		} else if(heading >= 22.5 && heading < 67.5){
			return dir_translations[2];
		} else if(heading >= 67.5 && heading < 112.5){
			return dir_translations[1];
		}else if(heading >= 112.5 && heading < 157.5){
			return dir_translations[0];
		}else if(heading >= 157.5 && heading < 202.5){
			return dir_translations[3];
		}else if(heading >= 202.5 && heading < 247.5){
			return dir_translations[5];
		}else if(heading >= 247.5 && heading < 292.5){
			return dir_translations[7];
		}else if(heading >= 292.5 && heading < 337.5){
			return dir_translations[8];
		}else{
			return dir_translations[4];
		}
	}
	protected void move_player(double heading){
		int[] translation = get_translation_from_heading(heading);
		
		int new_x = player_x - translation[0];
		int new_y = player_y - translation[1];
		
		if (in_bounds(new_x,new_y)){
			Log.d("DEBUGTAG" , "table["+ Integer.toString(player_x) + "][" + Integer.toString(player_y) + "]");
			ImageView to_tile = (ImageView) rotating_table.findViewWithTag("table["+ Integer.toString(new_x) + "][" + Integer.toString(new_y) + "]");
			ImageView from_tile = (ImageView) rotating_table.findViewWithTag("table["+ Integer.toString(player_x) + "][" + Integer.toString(player_y) + "]");
			
			to_tile.setImageResource(R.drawable.character);
			from_tile.setImageResource(R.drawable.tile);
			
			player_x = new_x;
			player_y = new_y;
			player_x_view.setText("Player x: " + player_x);
			player_y_view.setText("Player y: " + player_y);

			if (player_x == goal_x && player_y == goal_y){
				score += 100;
				score_view.setText("Score: " + score);
				generate_goal();
			}
		}


	}
	protected Boolean in_bounds(int x,int y){
		Log.d("!!!!!!!!!!!" , "Checking bounds yo!");
		if ((x < 0 || x >= GRID_WIDTH) || (y < 0 || y >= GRID_HEIGHT)){
			Log.d("!!!!!!!!!!!" , "Out of bounds yo!");
			return false;
		}
		return true;
	}
	protected void generate_goal(){
	    goal_y = rand.nextInt((100 - -100) + 1) + -100;
	    goal_x = rand.nextInt((100 - -100) + 1) + -100;
	    
	    goal_x_view.setText("Go to x: " + goal_x);
	    goal_y_view.setText("Go to y: " + goal_y);

	    
	}
	protected void update_screen(){
	}
	
	protected void pause_tracking(){
		unregister_sensors();
	}
	protected void resume_tracking(){
		register_sensors();
	}
	
	protected void register_sensors(){
		sensorManager.registerListener(this,
					  sensorAccelerometer,
					  SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(this,
					sensorGyroscope,
					SensorManager.SENSOR_DELAY_UI);
	
		sensorManager.registerListener(this, mStepCounterSensor,
		        SensorManager.SENSOR_DELAY_UI);
	
		 sensorManager.registerListener(this, mStepDetectorSensor,
		        SensorManager.SENSOR_DELAY_UI);
		 
	     sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

		last_sensor_time = System.currentTimeMillis();
		last_step_time = System.currentTimeMillis();

	}
	
	protected void unregister_sensors(){
		sensorManager.unregisterListener(this, sensorAccelerometer);
		sensorManager.unregisterListener(this, sensorGyroscope);
		sensorManager.unregisterListener(this, mStepCounterSensor);
		sensorManager.unregisterListener(this, mStepDetectorSensor);
		sensorManager.unregisterListener(this, magnetometer);	

	}
	
	protected double distance(int x1,int y1,int x2,int y2){
		return Math.sqrt( Math.pow((x1-x2),2) + Math.pow((y2-y1),2) );
	}
	
	protected void process_accelerometer_data(SensorEvent event){
		final float alpha = (float) 0.8;
		
		 gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
		 gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
		 gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
		
		 linear_acceleration[0] = event.values[0] - gravity[0];
		 linear_acceleration[1] = event.values[1] - gravity[1];
		 linear_acceleration[2] = event.values[2] - gravity[2];
		
		 double y_acceleration = linear_acceleration[1];
		 this.azimuth.setText("X acceleration:" + String.valueOf(linear_acceleration[0]));
		 long now = System.currentTimeMillis();
		 
		 if (y_acceleration > STEP_ACCEL_THRESHOLD && (now - last_step_time) > STEP_TIME_THRESHOLD){
			 last_step_time = System.currentTimeMillis();
		     accel_step_count += 1;
		     step_count_view.setText("Accel step count: " + accel_step_count);
		     move_player(player_heading);
		 }
		 
	}
	protected void process_gyroscope_data(SensorEvent event){
		 //get time interval between readings
		 long now = System.currentTimeMillis();
		 double prev_heading = this.player_heading;
		 float time_since_update_milli = now - last_sensor_time;
		 
		 for(int i =0; i < 3; i++){
		  valuesGyroscope[i] = event.values[i];
		 }
		 
		 double deg_rot = (double) (valuesGyroscope[2] * (180/Math.PI));
		 deg_rot /= 1000;
		 if(time_since_update_milli != 0){
			 player_heading += (deg_rot * time_since_update_milli);
			 if (player_heading < 0){
				player_heading = player_heading + 360; 
			 }
			 player_heading %= 360;
		 }else{
			 player_heading = -9000;
		 }
		 
		 //came from compass example
         // create a rotation animation (reverse turn degree degrees)
         RotateAnimation ra = new RotateAnimation(
                 (float)prev_heading, 
                 (float)player_heading,
                 Animation.RELATIVE_TO_SELF, 0.5f, 
                 Animation.RELATIVE_TO_SELF,
                 0.5f);

         // how long the animation will take place
         ra.setDuration(210);

         // set the animation after the end of the reservation status
         ra.setFillAfter(true);

         // Start the animation
         int width = rotating_layout.getWidth();
         int height = rotating_layout.getHeight();
         
         //rotating_layout.setPivotY(5000000);
        // rotating_layout.setPivotX(width/2);
         
         rotating_table.startAnimation(ra);
		 
		 this.gyro_x.setText("Gyroscope X:" + String.valueOf(deg_rot));
		 this.heading.setText("Player Heading:" + String.valueOf(player_heading));
		 last_sensor_time = System.currentTimeMillis();
	}
	
}
