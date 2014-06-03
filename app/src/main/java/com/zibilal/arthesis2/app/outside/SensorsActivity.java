package com.zibilal.arthesis2.app.outside;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
import android.view.ViewGroup;

import com.zibilal.arthesis2.app.operation.LocationService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class extends Activity and processes sensor data and location data.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class SensorsActivity extends Activity implements SensorEventListener , LocationService.Callback {

    private static final String TAG = "SensorsActivity";
    private static final AtomicBoolean computing = new AtomicBoolean(false);

    private static final int MIN_TIME = 30 * 1000;
    private static final int MIN_DISTANCE = 10;

    private static final float temp[] = new float[9]; // Temporary rotation matrix in Android format
    private static final float rotation[] = new float[9]; // Final rotation matrix in Android format
    private static final float grav[] = new float[3]; // Gravity (a.k.a accelerometer data)
    private static final float mag[] = new float[3]; // Magnetic

    /*
     * Using Matrix operations instead. This was way too inaccurate, private
     * static final float apr[] = new float[3]; //Azimuth, pitch, roll
     */

    private static final Matrix worldCoord = new Matrix();
    private static final Matrix magneticCompensatedCoord = new Matrix();
    private static final Matrix xAxisRotation = new Matrix();
    private static final Matrix yAxisRotation = new Matrix();
    private static final Matrix mageticNorthCompensation = new Matrix();

    private static GeomagneticField gmf = null;
    private static float smooth[] = new float[3];
    private static SensorManager sensorMgr = null;
    private static List<Sensor> sensors = null;
    private static Sensor sensorGrav = null;
    private static Sensor sensorMag = null;
    private static LocationManager locationMgr = null;

    private static LocationService mLocationService=null;

    private CameraSurface mCameraView;
    private AugmentedView mAugmentedView;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraView = new CameraSurface(this);
        setContentView(mCameraView);

        mAugmentedView = new AugmentedView(this);
        ViewGroup.LayoutParams augLayout = new ViewGroup.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addContentView(mAugmentedView, augLayout);

        Marker pondokIndah = new Marker("Pondok Indah Mall", -6.265708333333333d, 106.78429722222222d, 0d, Color.RED);
        List<Marker> markers = new ArrayList<Marker>();
        markers.add(pondokIndah);

        ARData.addMarkers(markers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        super.onStart();

        float neg90rads = (float)Math.toRadians(-90);

        // Counter-clockwise rotation at -90 degrees around the x-axis
        // [ 1, 0, 0 ]
        // [ 0, cos, -sin ]
        // [ 0, sin, cos ]
        xAxisRotation.set(1f, 0f,                       0f, 
                          0f, FloatMath.cos(neg90rads), -FloatMath.sin(neg90rads), 
                          0f, FloatMath.sin(neg90rads), FloatMath.cos(neg90rads));

        // Counter-clockwise rotation at -90 degrees around the y-axis
        // [ cos,  0,   sin ]
        // [ 0,    1,   0   ]
        // [ -sin, 0,   cos ]
        yAxisRotation.set(FloatMath.cos(neg90rads),  0f, FloatMath.sin(neg90rads),
                          0f,                        1f, 0f,
                          -FloatMath.sin(neg90rads), 0f, FloatMath.cos(neg90rads));

        try {
            sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

            sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
            if (sensors.size() > 0)
                sensorGrav = sensors.get(0);

            sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
            if (sensors.size() > 0)
                sensorMag = sensors.get(0);

            sensorMgr.registerListener(this, sensorGrav, SensorManager.SENSOR_DELAY_UI);
            sensorMgr.registerListener(this, sensorMag, SensorManager.SENSOR_DELAY_UI);


            mLocationService = new LocationService(this);
            mLocationService.setCallback(this);
            mLocationService.start();
            locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location gpsLoc = locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location netLoc = locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if(gpsLoc!=null) {
                onCurrentLocation(gpsLoc);
            } else if(netLoc!=null){
                onCurrentLocation(netLoc);
            } else {
                onCurrentLocation(ARData.hardFix);
            }

            try {



                gmf = new GeomagneticField((float) ARData.getCurrentLocation().getLatitude(),
                                           (float) ARData.getCurrentLocation().getLongitude(),
                                           (float) ARData.getCurrentLocation().getAltitude(),
                                           System.currentTimeMillis());

                float dec = (float)Math.toRadians(-gmf.getDeclination());

                synchronized (mageticNorthCompensation) {
                    // Identity matrix
                    // [ 1, 0, 0 ]
                    // [ 0, 1, 0 ]
                    // [ 0, 0, 1 ]
                    mageticNorthCompensation.toIdentity();

                    // Counter-clockwise rotation at negative declination around
                    // the y-axis
                    // note: declination of the horizontal component of the
                    // magnetic field
                    // from true north, in degrees (i.e. positive means the
                    // magnetic
                    // field is rotated east that much from true north).
                    // note2: declination is the difference between true north
                    // and magnetic north
                    // [ cos, 0, sin ]
                    // [ 0, 1, 0 ]
                    // [ -sin, 0, cos ]
                    mageticNorthCompensation.set(FloatMath.cos(dec),     0f, FloatMath.sin(dec), 
                                                 0f,                     1f, 0f, 
                                                 -FloatMath.sin(dec), 0f, FloatMath.cos(dec));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex1) {

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStop() {
        super.onStop();

        try {
            try {
                sensorMgr.unregisterListener(this, sensorGrav);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                sensorMgr.unregisterListener(this, sensorMag);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            sensorMgr = null;

            mLocationService.stop();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSensorChanged(SensorEvent evt) {
        if (!computing.compareAndSet(false, true)) return;

        if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
        	//if (AugmentedReality.useDataSmoothing) {
	            smooth = LowPassFilter.filter(0.5f, 1.0f, evt.values, grav);
	            grav[0] = smooth[0];
	            grav[1] = smooth[1];
	            grav[2] = smooth[2];
        	/*} else {
	            grav[0] = evt.values[0];
	            grav[1] = evt.values[1];
	            grav[2] = evt.values[2];
        	}
        	Orientation.calcOrientation(grav);
        	ARData.setDeviceOrientation(Orientation.getDeviceOrientation());
        	ARData.setDeviceOrientationAngle(Orientation.getDeviceAngle()); */
        } else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
        	//if (AugmentedReality.useDataSmoothing) {
	            smooth = LowPassFilter.filter(2.0f, 4.0f, evt.values, mag);
	            mag[0] = smooth[0];
	            mag[1] = smooth[1];
	            mag[2] = smooth[2];
        	/*} else {
	            mag[0] = evt.values[0];
	            mag[1] = evt.values[1];
	            mag[2] = evt.values[2];
        	} */
        }

        //// Find real world position relative to phone location ////
        // Get rotation matrix given the gravity and geomagnetic matrices
        SensorManager.getRotationMatrix(temp, null, grav, mag);

        SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_Z, rotation);

        /*
         * Using Matrix operations instead. This was way too inaccurate, 
         * //Get the azimuth, pitch, roll 
         * SensorManager.getOrientation(rotation,apr);
         * float floatAzimuth = (float)Math.toDegrees(apr[0]); 
         * if (floatAzimuth<0) floatAzimuth+=360; 
         * ARData.setAzimuth(floatAzimuth);
         * ARData.setPitch((float)Math.toDegrees(apr[1]));
         * ARData.setRoll((float)Math.toDegrees(apr[2]));
         */

        // Convert from float[9] to Matrix
        worldCoord.set(rotation[0], rotation[1], rotation[2], 
                       rotation[3], rotation[4], rotation[5], 
                       rotation[6], rotation[7], rotation[8]);

        //// Find position relative to magnetic north ////
        // Identity matrix
        // [ 1, 0, 0 ]
        // [ 0, 1, 0 ]
        // [ 0, 0, 1 ]
        magneticCompensatedCoord.toIdentity();

        synchronized (mageticNorthCompensation) {
            // Cross product the matrix with the magnetic north compensation
            magneticCompensatedCoord.prod(mageticNorthCompensation);
            Log.d("SensorActivity", String.format("1. Magnetic compensted coord : %s", magneticCompensatedCoord));
        }


        // The compass assumes the screen is parallel to the ground with the screen pointing
        // to the sky, rotate to compensate.
        Log.d("SensorActivity", String.format(" X Axis Rotation : %s", xAxisRotation));
        magneticCompensatedCoord.prod(xAxisRotation);
        Log.d("SensorActivity", String.format("2. Magnetic compensated coord : %s", magneticCompensatedCoord));

        // Cross product with the world coordinates to get a mag north compensated coords
        Log.d("SensorActivity", String.format(" World Coordingate : %s", worldCoord));
        magneticCompensatedCoord.prod(worldCoord);
        Log.d("SensorActivity", String.format("3. Magnetic compensated coord : %s", magneticCompensatedCoord));

        // Y axis
        Log.d("SensorActivity", String.format(" Y Axis Rotation : %s", yAxisRotation));
        magneticCompensatedCoord.prod(yAxisRotation);
        Log.d("SensorActivity", String.format("4. Magnetic compansated coord : %s", magneticCompensatedCoord));

        // Invert the matrix since up-down and left-right are reversed in landscape mode
        magneticCompensatedCoord.invert();
        Log.d("SensorActivity", String.format("5. Magnetic compensated coord : %s", magneticCompensatedCoord));

        // Set the rotation matrix (used to translate all object from lat/lon to x/y/z)
        ARData.setRotationMatrix(magneticCompensatedCoord);



        computing.set(false);

        if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER || evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mAugmentedView.postInvalidate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor == null) throw new NullPointerException();

        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD && accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.e(TAG, "Compass data unreliable");
        }
    }

    @Override
    public void onCurrentLocation(Location location) {
        Log.d(TAG, String.format("----> current location =  Latitude:%.4f  Longitude:%.4f  Altitude:%.4f", location.getLatitude(), location.getLongitude(), location.getAltitude()));
        ARData.setCurrentLocation(location);

        gmf = new GeomagneticField((float) ARData.getCurrentLocation().getLatitude(),
                (float) ARData.getCurrentLocation().getLongitude(),
                (float) ARData.getCurrentLocation().getAltitude(),
                System.currentTimeMillis());

        float dec = (float) Math.toRadians(-gmf.getDeclination());
        mageticNorthCompensation.toIdentity();
        mageticNorthCompensation.set(FloatMath.cos(dec), 0f, FloatMath.sin(dec),
                0f, 1f, 0f,
                -FloatMath.sin(dec), 0f, FloatMath.cos(dec));

        Log.d(TAG, String.format("----> current location = -->>MagneticNorthCompensation : %s ", mageticNorthCompensation));
    }
}
