package com.zibilal.arthesis2.app.operation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by bmuhamm on 5/29/14.
 */
public class OrientationService implements SensorEventListener {

    private SensorManager mManager;
    private Sensor mGravitySensor;
    private Sensor mMagneticSensor;
    private Context mContext;

    private OnSensorUpdate mListener;

    public OrientationService(Context context) {
        mContext=context;
        initSensor();
    }

    public void setListener(OnSensorUpdate listener) {
        mListener=listener;
    }

    private void initSensor(){
        mManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mGravitySensor = mManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticSensor = mManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void start() {
        mManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_UI);
        mManager.registerListener(this, mMagneticSensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void stop() {
        mManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        mListener.onSensorChanged(sensorEvent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        mListener.onAccuracyChanged(sensor, i);
    }

    public static interface OnSensorUpdate {
        public void onSensorChanged(SensorEvent event);
        public void onAccuracyChanged(Sensor sensor, int accuracy);
    }
}
