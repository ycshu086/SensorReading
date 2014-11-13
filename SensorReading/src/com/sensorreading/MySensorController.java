package com.sensorreading;

import java.io.PrintWriter;
import java.util.Locale;
//import android.content.Context;
//import android.graphics.Shader.TileMode;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

public class MySensorController extends MainActivity implements SensorEventListener {
	TextView mTextToShow_tilt;
	TextView mTextToShow_acc;
	TextView mTextToShow_acclin;
	TextView mTextToShow_gyro;
	TextView mTextToShow_temp;
	TextView mTextToShow_mag;
	TextView mTextToShow_light;
	TextView mTextToShow_ori;
	TextView mTextToShow_prox;
	TextView mTextToShow_step;
	TextView mTextToShow_baro;
	TextView mTextToShow_humi;

	SensorManager sm = null;
	Sensor acc = null;
	Sensor gyro = null;
	Sensor gravity = null;
	Sensor light = null;
	Sensor linAcc = null;
	Sensor temp = null;
	Sensor mag = null;
	Sensor ori = null;
	Sensor prox = null;
	Sensor step = null;
	Sensor baro = null;
	Sensor humi = null;

	float gravityX;
	float gravityY;
	float gravityZ;

	float lightMag;

	double tiltX;
	double tiltY;
	double tiltZ;
	double tiltXDegree;
	double tiltYDegree;
	double tiltZDegree;

	float[] accValue = new float[3];
	float accMag;
	
	float acclinX;
	float acclinY;
	float acclinZ;
	float acclinMag;
	
	float[] magValue = new float[3];
	float magMag;

	float gyroX;
	float gyroY;
	float gyroZ;
	
	float tempMag;
	
	float[] oriValue = new float[3];
	float[] R = new float[9];
	
	float proxValue;
	
	float stepValue;
	
	float baroValue;
	
	float humiValue;

	long sysTime;
	boolean blDump;
	int countDump = 1;

	PrintWriter fileToSave;

	public MySensorController(TextView ref1, TextView ref2, TextView ref3, TextView ref4, TextView ref5, 
			TextView ref6, TextView ref7, TextView ref8, TextView ref9, TextView ref10, TextView ref11, TextView ref12, 
			SensorManager systemSensorManager, PrintWriter fileToSaveIn, boolean blDumpIn)
	{
		mTextToShow_tilt = ref1;
		mTextToShow_acc = ref2;
		mTextToShow_acclin = ref3;
		mTextToShow_gyro = ref4;
		mTextToShow_temp = ref5;
		mTextToShow_mag = ref6;
		mTextToShow_light = ref7;
		mTextToShow_ori = ref8;
		mTextToShow_prox = ref9;
		mTextToShow_step = ref10;
		mTextToShow_baro = ref11;
		mTextToShow_humi = ref12;
		blDump = blDumpIn;
		sm = systemSensorManager;

		acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		gravity = sm.getDefaultSensor(Sensor.TYPE_GRAVITY);
		linAcc = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		light = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
		temp = sm.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
		mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		ori = sm.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
		prox = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		step = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
		baro = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
		humi = sm.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
		registerSensor();
		fileToSave = fileToSaveIn;
	}

	public void dumpToFile(int index, String shotPath){
		sysTime = System.currentTimeMillis();
		String stringToSave = String.format(Locale.US, "%d\t%s\t%d\t%s\t%f:%f:%f\t%f:%f:%f\t%f:%f:%f\t%f:%f:%f\t%f:%f:%f\t%f:%f:%f\t%f\t%f" +
				"\t%f\t%f\t%f\t%f\t%f:%f:%f", index, shotPath, sysTime, refFormatNowDate(sysTime), 
				tiltX, tiltY, tiltZ, accValue[0], accValue[1], accValue[2], gravityX, gravityY, gravityZ, magValue[0], magValue[1], magValue[2], 
				gyroX, gyroY, gyroZ, acclinX, acclinY, acclinZ, lightMag, tempMag, 
				proxValue, baroValue, stepValue, humiValue, oriValue[0], oriValue[1], oriValue[2]);
		fileToSave.println(stringToSave);
		countDump++;
	}

//	public String refFormatNowDate(long sysTimeIn) {
//		Date nowTime = new Date(sysTimeIn);
//		SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault()); 
//		String retStrFormatNowDate = sdFormatter.format(nowTime);
//		return retStrFormatNowDate;
//	}

	// ================== start of sensor callback ========================
	@Override
	public void onSensorChanged(SensorEvent event){
		if(event.sensor.getType() == Sensor.TYPE_GRAVITY)
		{
			gravityX = event.values[0];
			gravityY = event.values[1];
			gravityZ = event.values[2];
		}

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			accValue = event.values;
			accValue[0] = -accValue[0]/SensorManager.GRAVITY_EARTH;
			accValue[1] = -accValue[1]/SensorManager.GRAVITY_EARTH;
			accValue[2] = accValue[2]/SensorManager.GRAVITY_EARTH; 
			accMag = (float) Math.sqrt(accValue[0]*accValue[0] + accValue[1]*accValue[1] + accValue[2]*accValue[2]);
			String textToUpdate_acc = String.format(Locale.US, "acc = (%03f, %03f, %03f, %03f)", accValue[0], accValue[1], accValue[2], accMag);
			mTextToShow_acc.setText(textToUpdate_acc);
			
			tiltX =  Math.asin(accValue[0]/accMag);
			tiltXDegree = radiusToDegree(tiltX);
			tiltY =  Math.asin(accValue[1]/accMag);
			tiltYDegree = radiusToDegree(tiltY);
			tiltZ =  Math.asin(accValue[2]/accMag);
			tiltZDegree = radiusToDegree(tiltZ);
			String textToUpdate_tilt = String.format(Locale.US, "tilt = (%03f, %03f, %03f)", tiltXDegree, tiltYDegree, tiltZDegree);
			mTextToShow_tilt.setText(textToUpdate_tilt);
		}
		
		if(event.sensor.getType() == Sensor.TYPE_LIGHT)
		{
			lightMag = event.values[0];
			String textToUpdate_light = String.format(Locale.US, "light = (%03f)", lightMag);
			mTextToShow_light.setText(textToUpdate_light);
		}
		
		if(event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE)
		{
			tempMag = event.values[0];
			String textToUpdate_temp = String.format(Locale.US, "temp = (%03f)", tempMag);
			mTextToShow_temp.setText(textToUpdate_temp);
		}
		
		if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
		{
			gyroX = event.values[0];
			gyroY = event.values[1];
			gyroZ = event.values[2];
			String textToUpdate_gyro = String.format(Locale.US, "gyro = (%03f, %03f, %03f)", gyroX, gyroY, gyroZ);
			mTextToShow_gyro.setText(textToUpdate_gyro);
		}
		
		if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION)
		{
			acclinX = event.values[0];
			acclinY = event.values[1];
			acclinZ = event.values[2];
			acclinMag = (float) Math.sqrt(acclinX*acclinX + acclinY*acclinY + acclinZ*acclinZ);
			String textToUpdate_acclin = String.format(Locale.US, "acl = (%03f, %03f, %03f, %03f)", acclinX, acclinY, acclinZ, acclinMag);
			mTextToShow_acclin.setText(textToUpdate_acclin);
		}

		if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
		{
			magValue = event.values;
			magMag = (float) Math.sqrt(magValue[0]*magValue[0] + magValue[1]*magValue[1] + magValue[2]*magValue[2]);
			String textToUpdate_mag = String.format(Locale.US, "mag = (%03f, %03f, %03f, %03f)", magValue[0], magValue[1], magValue[2], magMag);
			mTextToShow_mag.setText(textToUpdate_mag);
		}
		
		if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD || event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			SensorManager.getRotationMatrix(R, null, accValue, magValue);  
	        SensorManager.getOrientation(R, oriValue);
	        oriValue[0]=(float)Math.toDegrees(oriValue[0]);
	        oriValue[1]=(float)Math.toDegrees(oriValue[1]);
	        oriValue[2]=(float)Math.toDegrees(oriValue[2]);
	        String textToUpdate_ori = String.format(Locale.US, "ori = (%03f, %03f, %03f)", oriValue[0], oriValue[1], oriValue[2]);
			mTextToShow_ori.setText(textToUpdate_ori);
		}
		
		if(event.sensor.getType() == Sensor.TYPE_PROXIMITY)
		{
			proxValue = event.values[0];
			String textToUpdate_prox = String.format(Locale.US, "prox = %03f", proxValue);
			mTextToShow_prox.setText(textToUpdate_prox);
		}
		
		if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER)
		{
			stepValue = event.values[0];
			String textToUpdate_step = String.format(Locale.US, "step = %03f", stepValue);
			mTextToShow_step.setText(textToUpdate_step);
		}
		
		if(event.sensor.getType() == Sensor.TYPE_PRESSURE)
		{
			baroValue = event.values[0];
			String textToUpdate_baro = String.format(Locale.US, "baro = %03f", baroValue);
			mTextToShow_baro.setText(textToUpdate_baro);
		}
		
		if(event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY)
		{
			humiValue = event.values[0];
			String textToUpdate_humi = String.format(Locale.US, "step = %03f", humiValue);
			mTextToShow_humi.setText(textToUpdate_humi);
		}
		
/*		if(lightMag < 3.0 || accMag > 1.5 || acclinMag > 1.5)
		{
			
     		mTextStatus.setText("Moving");
			 
		}
		else
		{
			
     		mTextStatus.setText("Stationary");
			 
		}*/
		
		if(blDump)
		{
			dumpToFile(1, Integer.toString(countDump));
		}
	}
	// ================== end of sensor  callback =========================

	double radiusToDegree(double radius){
		return radius*(180/3.1415926);
	}


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		//Not going to bother about sensor accuracy here
	}

	public void registerSensor()
	{
		sm.registerListener(this, acc, SensorManager.SENSOR_DELAY_GAME); //SensorManager.SENSOR_DELAY_FASTEST, SENSOR_DELAY_GAME, SENSOR_DELAY_NORMAL, SENSOR_DELAY_UI
		sm.registerListener(this, gravity, SensorManager.SENSOR_DELAY_GAME);
		sm.registerListener(this, light, SensorManager.SENSOR_DELAY_GAME);
		sm.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
		sm.registerListener(this, linAcc, SensorManager.SENSOR_DELAY_GAME);
		sm.registerListener(this, temp, SensorManager.SENSOR_DELAY_GAME);
		sm.registerListener(this, mag, SensorManager.SENSOR_DELAY_GAME);
		sm.registerListener(this, ori, SensorManager.SENSOR_DELAY_GAME);
		sm.registerListener(this, prox, SensorManager.SENSOR_DELAY_GAME);
		sm.registerListener(this, step, SensorManager.SENSOR_DELAY_GAME);
		sm.registerListener(this, baro, SensorManager.SENSOR_DELAY_GAME);
		sm.registerListener(this, humi, SensorManager.SENSOR_DELAY_GAME);
	}
	
	public void unregisterSensor()
	{
		sm.unregisterListener(this, acc);
		sm.unregisterListener(this, gravity);
		sm.unregisterListener(this, light);
		sm.unregisterListener(this, gyro);
		sm.unregisterListener(this, linAcc);
		sm.unregisterListener(this, temp);
		sm.unregisterListener(this, mag);
		sm.unregisterListener(this, ori);
		sm.unregisterListener(this, prox);
		sm.unregisterListener(this, step);
		sm.unregisterListener(this, baro);
		sm.unregisterListener(this, humi);
	}
}
