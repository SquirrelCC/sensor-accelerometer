package com.scconsulting.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class SensorActivity extends Activity implements SensorEventListener {
	
	private SensorManager sensorManager;
	private long lastUpdate;
	private MainPanel mainView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Set full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Set the main View of the Activity to the custom class MainPanel,
        // rather than an xml layout View.
        mainView = new MainPanel(this); // Construct the MainPanel view.
        setContentView(mainView);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();
		
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			getAccelerometer(event);
		}
	}

	@Override
	public void onAccuracyChanged(android.hardware.Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
	private void getAccelerometer(SensorEvent event) {
		
		float[] values = event.values;
		// Movement
		float x = values[0];  // current horizontal value
		float y = values[1];  // current vertical value
		float z = values[2];  // current value on z axis
		// The z axis is perpendicular to the x,y plane, or perpendicular to the device screen.

		// Detect whether device was shaken, or moved quickly in a linear direction.
		// Movement is detected on 3 axes:
		// x axis is horizontal, on the plane of the device screen
		// y axis is vertical, on the plane of the device screen
		// z axis is perpendicular to the plane of the device screen
		float accelationSquareRoot = (x * x + y * y + z * z)
				/ (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
		long actualTime = event.timestamp;
		if (accelationSquareRoot >= 2) {
			if (actualTime - lastUpdate < 200) {
				return;
			}
			lastUpdate = actualTime;
			
			//Toast.makeText(this, "Device was shaken", Toast.LENGTH_SHORT).show();
			
			// Set up an Explosion
			//((MainPanel)mainView).explode();
			mainView.explode();

		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// register this SensorActivity class as a listener for the
		// accelerometer sensor
		Sensor sensor = sensorManager.getDefaultSensor(
				Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this,
				sensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		// unregister listener
		super.onPause();
		sensorManager.unregisterListener(this);
	}
}
