package com.sensorreading;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends ActionBarActivity
{

    private PrintWriter mSensorOutFile, mTickOutFile;
    TextView textSensorShow_tilt, textSensorShow_acc, textSensorShow_acclin, textSensorShow_gyro, textSensorShow_temp, 
    	textSensorShow_mag, textSensorShow_light, textSensorShow_ori, textSensorShow_prox, textSensorShow_step, textSensorShow_baro, textSensorShow_humi;
    Button button_start, button_stop, button_tick;
    MySensorController MySensorControllerA, MySensorControllerB;
    Boolean blDump = false;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize buttons
        button_start=(Button)findViewById(R.id.button_start);  
        button_start.setOnClickListener(new ButtonClick());
        button_stop=(Button)findViewById(R.id.button_stop);  
        button_stop.setOnClickListener(new ButtonClick());
        button_tick=(Button)findViewById(R.id.button_tick);  
        button_tick.setOnClickListener(new ButtonClick());
        
       	textSensorShow_tilt = (TextView)findViewById(R.id.text_tilt_show);
       	textSensorShow_acc = (TextView)findViewById(R.id.text_acc_show);
       	textSensorShow_acclin = (TextView)findViewById(R.id.text_acclin_show);
       	textSensorShow_gyro = (TextView)findViewById(R.id.text_gyro_show);
       	textSensorShow_temp = (TextView)findViewById(R.id.text_temp_show);
       	textSensorShow_mag = (TextView)findViewById(R.id.text_mag_show);
       	textSensorShow_light = (TextView)findViewById(R.id.text_light_show);
       	textSensorShow_ori = (TextView)findViewById(R.id.text_ori_show);
       	textSensorShow_prox = (TextView)findViewById(R.id.text_prox_show);
       	textSensorShow_step = (TextView)findViewById(R.id.text_step_show);
       	textSensorShow_baro = (TextView)findViewById(R.id.text_baro_show);
       	textSensorShow_humi = (TextView)findViewById(R.id.text_humi_show);
       	MySensorControllerA = new MySensorController(textSensorShow_tilt, textSensorShow_acc, textSensorShow_acclin, textSensorShow_gyro, textSensorShow_temp, 
       			textSensorShow_mag, textSensorShow_light, textSensorShow_ori, textSensorShow_prox, textSensorShow_step, textSensorShow_baro, textSensorShow_humi, 
       			(SensorManager) getSystemService(Context.SENSOR_SERVICE), mSensorOutFile, blDump); // blDump = false
    }
	
	@Override
	protected void onPause() {
		super.onPause();
		MySensorControllerA.unregisterSensor();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MySensorControllerA.registerSensor();
	}
	
	class ButtonClick implements OnClickListener  
    {  
        public void onClick(View v)
        {  
            switch (v.getId()) {  
            case R.id.button_stop: 
                Toast.makeText(MainActivity.this, "stopped", Toast.LENGTH_LONG).show(); 
                mSensorOutFile.close();
                mSensorOutFile = null;
                mTickOutFile.close();
                mTickOutFile = null;
                MySensorControllerA.unregisterSensor();
            	MySensorControllerA = null;
                break;
            case R.id.button_start:
            	Toast.makeText(MainActivity.this, "started", Toast.LENGTH_LONG).show();  
                try {
            		String systemPath, outputFolder;
            		systemPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
            		outputFolder = "SensorReading/";
                	mSensorOutFile = new PrintWriter(new FileOutputStream(new File(systemPath + outputFolder + "sensor.txt"), true)); // true: append
                	mTickOutFile = new PrintWriter(new FileOutputStream(new File(systemPath + outputFolder + "tick.txt"), true)); // true: append

                	blDump = true;
                	if (null != MySensorControllerA){
                		MySensorControllerA.unregisterSensor();
                		MySensorControllerA = null;
                	}
                	MySensorControllerA = new MySensorController(textSensorShow_tilt, textSensorShow_acc, textSensorShow_acclin, textSensorShow_gyro, textSensorShow_temp, 
                			textSensorShow_mag, textSensorShow_light, textSensorShow_ori, textSensorShow_prox, textSensorShow_step, textSensorShow_baro, textSensorShow_humi, 
                   			(SensorManager) getSystemService(Context.SENSOR_SERVICE), mSensorOutFile, blDump);
                }
                catch (FileNotFoundException e) {
                	String LOG_TAG = null;
            		Log.e(LOG_TAG, "ERROR: can't open file to write");
                	e.printStackTrace();
               	}
            	break;
            case R.id.button_tick:
            	Toast.makeText(MainActivity.this, "tick", Toast.LENGTH_LONG).show();  
            	long sysTime = System.currentTimeMillis();
            	String stringToSave = String.format(Locale.US, "%d", sysTime);
            	//String stringToSave = String.format(Locale.US, "%d\t%s", sysTime, refFormatNowDate(sysTime));
            	mTickOutFile.println(stringToSave);
            	break;
            default:  
                break;  
            }  
        }         
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
    
    private long exitTime = 0; 
    @Override 
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
		    if((System.currentTimeMillis()-exitTime) > 2000){ 
			    Toast.makeText(getApplicationContext(), "Press again to exit", Toast.LENGTH_SHORT).show(); 
			    exitTime = System.currentTimeMillis(); 
		    }
		    else { 
			    finish(); 
			    System.exit(0); 
		    } 
		    return true; 
	    } 
	    return super.onKeyDown(keyCode, event); 
    }
    
	public String refFormatNowDate(long sysTimeIn) {
		Date nowTime = new Date(sysTimeIn);
		SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault()); 
		String retStrFormatNowDate = sdFormatter.format(nowTime);
		return retStrFormatNowDate;
	}
}
