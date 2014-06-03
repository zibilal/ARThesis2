package com.zibilal.arthesis2.app;

import android.app.ActionBar;
import android.app.Activity;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.zibilal.arthesis2.app.data.ARGlobal;
import com.zibilal.arthesis2.app.data.GooglePlacesAPIDataSources;
import com.zibilal.arthesis2.app.operation.LocationService;
import com.zibilal.arthesis2.app.operation.LowPassFilter;
import com.zibilal.arthesis2.app.operation.Matrix;
import com.zibilal.arthesis2.app.views.AugmentedView;
import com.zibilal.arthesis2.app.views.CameraView;

import java.util.concurrent.atomic.AtomicBoolean;


public class ARActivity extends Activity implements SensorEventListener, LocationService.Callback {

    private static final String TAG="ARActivity";

    private AtomicBoolean computing = new AtomicBoolean(false);

    private float[] mMagnetic=new float[3];
    private float[] mGravity=new float[3];
    private float[] mRotation=new float[9];
    private float[] mTempRotation=new float[9];

    private Sensor mSensorGravity;
    private Sensor mSensorMagnetic;
    private SensorManager mSensorManager;
    private GeomagneticField gmf;

    private float[] mSmooth = new float[3];
    private static final Matrix mWorldMatrix = new Matrix();
    private static final Matrix mMagneticCompensatedMatrix = new Matrix();
    private static final Matrix mMagneticNorthCompensation = new Matrix();
    private static final Matrix xAxisRotation = new Matrix();
    private static final Matrix yAxisRotation = new Matrix();

    private LocationService mLocationService;
    private LocationManager mLocationManager;

    private CameraView mCameraView;
    private AugmentedView mAugmentedView;

    private GooglePlacesAPIDataSources mDataSources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCameraView = new CameraView(this);
        setContentView(mCameraView);

        mAugmentedView = new AugmentedView(this);
        ViewGroup.LayoutParams augLayout = new ViewGroup.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addContentView(mAugmentedView, augLayout);

        mLocationService = new LocationService(this);
        mLocationService.setCallback(this);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mDataSources = new GooglePlacesAPIDataSources();
    }

    @Override
    protected void onStart() {
        super.onStart();

        float neg90radians = (float) Math.toRadians(-90);
        xAxisRotation.set(1f, 0f, 0f,
                0f, FloatMath.cos(neg90radians), -FloatMath.sin(neg90radians),
                0f, FloatMath.sin(neg90radians), FloatMath.cos(neg90radians));

        yAxisRotation.set(FloatMath.cos(neg90radians), 0f, FloatMath.sin(neg90radians),
                0f, 1f, 0f,
                -FloatMath.sin(neg90radians), 0f, FloatMath.cos(neg90radians));

        Log.d(TAG, String.format("On Start, xAxisRotation : %s", xAxisRotation));
        Log.d(TAG, String.format("On Start, yAxisRotation : %s", yAxisRotation));

        mSensorManager.registerListener(this, mSensorGravity, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorMagnetic, SensorManager.SENSOR_DELAY_UI);

        mLocationService.start();

        Location gpsLoc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location netLoc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if(gpsLoc!=null) {
            onCurrentLocation(gpsLoc);
        } else if(netLoc!=null){
            onCurrentLocation(netLoc);
        } else {
            onCurrentLocation(ARGlobal.hardFix);
        }



        gmf = new GeomagneticField((float) ARGlobal.getCurrentLocation().getLatitude(),
                (float) ARGlobal.getCurrentLocation().getLongitude(),
                (float) ARGlobal.getCurrentLocation().getAltitude(),
                System.currentTimeMillis());

        float dec = (float) Math.toRadians(-gmf.getDeclination());
        synchronized (mMagneticNorthCompensation) {
            mMagneticNorthCompensation.toIdentity();
            mMagneticNorthCompensation.set(FloatMath.cos(dec), 0f, FloatMath.sin(dec),
                    0f, 1f, 0f,
                    -FloatMath.sin(dec), 0f, FloatMath.cos(dec));

            Log.d(TAG, String.format("OnStart, mMagneticNorthCompensation : %s", mMagneticNorthCompensation) );
        }

    }

    @Override
    protected void onStop() {
        mLocationService.stop();
        mSensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ar, menu);
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

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(!computing.compareAndSet(false, true)) return;

        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mSmooth = LowPassFilter.filter(0.5f, 1.0f, sensorEvent.values, mGravity);
            mGravity[0] = mSmooth[0];
            mGravity[1] = mSmooth[1];
            mGravity[2] = mSmooth[2];
        } else if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mSmooth = LowPassFilter.filter(2.0f, 4.0f, sensorEvent.values, mMagnetic);
            mMagnetic[0] = mSmooth[0];
            mMagnetic[1] = mSmooth[1];
            mMagnetic[2] = mSmooth[2];
        }

        SensorManager.getRotationMatrix(mTempRotation, null, mGravity, mMagnetic);
        SensorManager.remapCoordinateSystem(mTempRotation, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_Z, mRotation);

        mWorldMatrix.set(mRotation[0], mRotation[1], mRotation[2],
                mRotation[3], mRotation[4], mRotation[5],
                mRotation[6], mRotation[7], mRotation[8]);


        mMagneticCompensatedMatrix.toIdentity();

        synchronized (mMagneticNorthCompensation) {
            mMagneticCompensatedMatrix.prod(mMagneticNorthCompensation);
            Log.d(TAG, String.format("1. Magnetic compensted coord : %s", mMagneticCompensatedMatrix));
        }

        // The compass assumes the screen is parallel to the ground with the screen pointing to the sky
        // Rotate to compensate
        Log.d(TAG, String.format(" X Axis Rotation : %s", xAxisRotation));
        mMagneticCompensatedMatrix.prod(xAxisRotation);
        Log.d(TAG, String.format("2. Magnetic compensated coord : %s", mMagneticCompensatedMatrix));

        // Cross product with the world coordinates to get a mag north compensated coords
        Log.d(TAG, String.format(" World Coordingate : %s", mWorldMatrix));
        mMagneticCompensatedMatrix.prod(mWorldMatrix);
        Log.d(TAG, String.format("3. Magnetic compensated coord : %s", mMagneticCompensatedMatrix));

        // Y axis
        Log.d(TAG, String.format(" Y Axis Rotation : %s", yAxisRotation));
        mMagneticCompensatedMatrix.prod(yAxisRotation);
        Log.d(TAG, String.format("4. Magnetic compansated coord : %s", mMagneticCompensatedMatrix));

        // Invert the matrix since up-down and left-right are reversed in landscape mode
        mMagneticCompensatedMatrix.invert();
        Log.d(TAG, String.format("5. Magnetic compensated coord : %s", mMagneticCompensatedMatrix));

        ARGlobal.setRotationMatrix(mMagneticCompensatedMatrix);

        computing.set(false);


        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER || sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mAugmentedView.postInvalidate();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD && accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.e("ARActivity", "====================>> [Compass data unreliable]]]]");
        }
    }

    @Override
    public void onCurrentLocation(Location location) {
        if(location != null) {

            mDataSources.createRequest(location.getLatitude(), location.getLongitude(), 0.5f);
            mDataSources.fetchData();

            Log.d(TAG, String.format("----> current location =  Latitude:%.4f  Longitude:%.4f  Altitude:%.4f", location.getLatitude(), location.getLongitude(), location.getAltitude()));
            ARGlobal.setCurrentLocation(location);

            gmf = new GeomagneticField((float) ARGlobal.getCurrentLocation().getLatitude(),
                    (float) ARGlobal.getCurrentLocation().getLongitude(),
                    (float) ARGlobal.getCurrentLocation().getAltitude(),
                    System.currentTimeMillis());

            float dec = (float) Math.toRadians(-gmf.getDeclination());
            mMagneticNorthCompensation.toIdentity();
            mMagneticNorthCompensation.set(FloatMath.cos(dec), 0f, FloatMath.sin(dec),
                    0f, 1f, 0f,
                    -FloatMath.sin(dec), 0f, FloatMath.cos(dec));

            Log.d(TAG, String.format("----> current location = -->>MagneticNorthCompensation :%s", mMagneticNorthCompensation));
        }
    }
}
