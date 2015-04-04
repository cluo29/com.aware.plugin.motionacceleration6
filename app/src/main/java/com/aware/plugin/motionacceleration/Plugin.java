package com.aware.plugin.motionacceleration;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.motionacceleration.Provider.MotionAcceleration_Data;
import com.aware.providers.Accelerometer_Provider.Accelerometer_Data;
import com.aware.providers.Gyroscope_Provider.Gyroscope_Data;
import com.aware.utils.Aware_Plugin;

public class Plugin extends Aware_Plugin {
    public static final String ACTION_AWARE_PLUGIN_MOTION_ACCELERATION = "ACTION_AWARE_PLUGIN_MOTION_ACCELERATION";

    public static final String EXTRA_A_VALUES_0 ="A_VALUES_0";
    public static final String EXTRA_A_VALUES_1 ="A_VALUES_1";
    public static final String EXTRA_A_VALUES_2 ="A_VALUES_2";
    public static final String EXTRA_G_VALUES_0 ="G_VALUES_0";
    public static final String EXTRA_G_VALUES_1 ="G_VALUES_1";
    public static final String EXTRA_G_VALUES_2 ="G_VALUES_2";
    private static double a_0 = 0;
    private static double a_1 = 0;
    private static double a_2 = 0;
    private static double g_0 = 0;
    private static double g_1 = 0;
    private static double g_2 = 0;
    public static ContextProducer context_producer;
    public Thread motion_thread = new Thread(){
        public void run(){
            while(true){
                Cursor Acceleration = getApplicationContext().getContentResolver().query(Accelerometer_Data.CONTENT_URI, null, null, null, Accelerometer_Data.TIMESTAMP + " DESC LIMIT 1");
                if(Acceleration!=null && Acceleration.moveToFirst()){
                    a_0 = Acceleration.getDouble(Acceleration.getColumnIndex(Accelerometer_Data.VALUES_0));
                    a_1 = Acceleration.getDouble(Acceleration.getColumnIndex(Accelerometer_Data.VALUES_1));
                    a_2 = Acceleration.getDouble(Acceleration.getColumnIndex(Accelerometer_Data.VALUES_2));
                }
                if( Acceleration != null && ! Acceleration.isClosed() ) Acceleration.close();
                Cursor gyro = getApplicationContext().getContentResolver().query(Gyroscope_Data.CONTENT_URI, null, null, null, Gyroscope_Data.TIMESTAMP + " DESC LIMIT 1");
                if(gyro!=null && gyro.moveToFirst()){

                    g_0 = gyro.getDouble(gyro.getColumnIndex(Gyroscope_Data.VALUES_0));
                    g_1 = gyro.getDouble(gyro.getColumnIndex(Gyroscope_Data.VALUES_1));
                    g_2 = gyro.getDouble(gyro.getColumnIndex(Gyroscope_Data.VALUES_2));
                }
                if( gyro != null && ! gyro.isClosed() ) gyro.close();
                ContentValues data = new ContentValues();
                //require Provider.java here
                data.put(MotionAcceleration_Data.TIMESTAMP, System.currentTimeMillis());
                data.put(MotionAcceleration_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                data.put(MotionAcceleration_Data.A_VALUES_0,a_0);
                data.put(MotionAcceleration_Data.A_VALUES_1,a_1);
                data.put(MotionAcceleration_Data.A_VALUES_2,a_2);
                data.put(MotionAcceleration_Data.G_VALUES_0,g_0);
                data.put(MotionAcceleration_Data.G_VALUES_1,g_1);
                data.put(MotionAcceleration_Data.G_VALUES_2,g_2);

                getContentResolver().insert(MotionAcceleration_Data.CONTENT_URI, data);
                //Share context
                context_producer.onContext();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };




    @Override
    public void onCreate() {
        super.onCreate();
        TAG = "AWARE::Motion Meter";
        DEBUG = true;

        Intent aware = new Intent(this, Aware.class);
        startService(aware);
        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {
                Intent context_motion_meter = new Intent();
                context_motion_meter.setAction(ACTION_AWARE_PLUGIN_MOTION_ACCELERATION);
                //take them out of AWARE providers
                context_motion_meter.putExtra(EXTRA_A_VALUES_0, a_0);
                context_motion_meter.putExtra(EXTRA_A_VALUES_1, a_1);
                context_motion_meter.putExtra(EXTRA_A_VALUES_2, a_2);
                context_motion_meter.putExtra(EXTRA_G_VALUES_0, g_0);
                context_motion_meter.putExtra(EXTRA_G_VALUES_1, g_1);
                context_motion_meter.putExtra(EXTRA_G_VALUES_2, g_2);
                sendBroadcast(context_motion_meter);
            }
        };
        context_producer = CONTEXT_PRODUCER;
        motion_thread.start();
    }
    public void onDestroy() {
        super.onDestroy();
    }
}