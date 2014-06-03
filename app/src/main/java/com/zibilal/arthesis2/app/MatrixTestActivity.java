package com.zibilal.arthesis2.app;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.zibilal.arthesis2.app.data.ARGlobal;
import com.zibilal.arthesis2.app.data.Marker;
import com.zibilal.arthesis2.app.operation.LocationService;
import com.zibilal.arthesis2.app.operation.OrientationService;
import com.zibilal.arthesis2.app.outside.ARData;
import com.zibilal.arthesis2.app.outside.CameraModel;
import com.zibilal.arthesis2.app.outside.Matrix;


public class MatrixTestActivity extends Activity implements LocationService.Callback, OrientationService.OnSensorUpdate {

    private static final String TAG="MatrixTestActivity";

    private Matrix xAxisRotation = new Matrix();
    private Matrix yAxisRotation = new Matrix();

    private TextView xAxisRotationView;
    private TextView yAxisRotationView;
    private TextView marker1View;
    private TextView marker2View;

    private Marker marker1;
    private com.zibilal.arthesis2.app.outside.Marker marker2;

    private LocationService mLocationService;
    private OrientationService mOrientationService;

    private float[] mGravity;
    private float[] mMagnetic;
    private float[] mTempRotation;
    private float[] mRotation;

    private Matrix mMatrix = new Matrix();
    private com.zibilal.arthesis2.app.operation.Matrix mMatrix2 = new com.zibilal.arthesis2.app.operation.Matrix();
    private CameraModel mCam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matrix_test);

        mGravity = new float[3];
        mMagnetic = new float[3];
        mTempRotation = new float[9];
        mRotation = new float[9];

        xAxisRotationView = (TextView) findViewById(R.id.x_axis_rotation);
        yAxisRotationView = (TextView) findViewById(R.id.y_axis_rotation);
        marker1View = (TextView) findViewById(R.id.marker1);
        marker2View = (TextView) findViewById(R.id.marker2);

        float neg90rads = (float) Math.toRadians(-90);

        xAxisRotation.set(1f, 0f, 0f,
                0f, FloatMath.cos(neg90rads), -FloatMath.sin(neg90rads),
                0f, FloatMath.sin(neg90rads), FloatMath.cos(neg90rads));

        yAxisRotation.set(FloatMath.cos(neg90rads), 0f, FloatMath.sin(neg90rads),
                0f, 1f, 0f,
                -FloatMath.sin(neg90rads), 0f, FloatMath.cos(neg90rads));

        xAxisRotationView.setText(xAxisRotation.toString());
        yAxisRotationView.setText(yAxisRotation.toString());

        Log.d(TAG, String.format("X Axis rotation view : %s", xAxisRotation) );
        Log.d(TAG, String.format("Y Axis rotation view : %s", yAxisRotation) );

        marker1 = new Marker("PIM", -6.265708333333333d, 106.78429722222222d, 3.9643d, 0d, Color.RED);
        marker2 = new com.zibilal.arthesis2.app.outside.Marker("PIM", -6.265708333333333d, 106.78429722222222d, 0d, Color.RED);

        mLocationService = new LocationService(this);
        mLocationService.setCallback(this);

        mOrientationService = new OrientationService(this);
        mOrientationService.setListener(this);
        mCam = new CameraModel(1280, 720, false);
        mCam.setViewAngle(CameraModel.DEFAULT_VIEW_ANGLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationService.start();
        //mOrientationService.start();
    }

    @Override
    protected void onStop() {
        mLocationService.stop();
        //mOrientationService.stop();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.matrix_test, menu);
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
    public void onCurrentLocation(Location location) {
        if(location != null) {
            marker1.calculateRelativePosition(location);
            marker2.calcRelativePosition(location);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity[0]=event.values[0];
            mGravity[1]=event.values[1];
            mGravity[2]=event.values[2];
        } else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mMagnetic[0]=event.values[0];
            mMagnetic[1]=event.values[1];
            mMagnetic[2]=event.values[2];
        }

        SensorManager.getRotationMatrix(mTempRotation, null, mGravity, mMagnetic);
        SensorManager.remapCoordinateSystem(mTempRotation, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_Z, mRotation);

        mMatrix.set(mRotation[0], mRotation[1], mRotation[2],
                mRotation[3], mRotation[4], mRotation[5],
                mRotation[6], mRotation[7], mRotation[8]);
        mMatrix2.set(mRotation[0], mRotation[1], mRotation[2],
                mRotation[3], mRotation[4], mRotation[5],
                mRotation[6], mRotation[7], mRotation[8]);

        ARGlobal.setRotationMatrix(mMatrix2);
        ARData.setRotationMatrix(mMatrix);

        marker1.populateMatrices();
        marker2.populateMatrices(mCam, 0, 0);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
