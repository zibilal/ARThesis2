package com.zibilal.arthesis2.app.operation;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

/**
 * Created by bmuhamm on 4/14/14.
 */
public class LocationService implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "LocationService";
    public static final int CONNECTION_FAILURE_RESOLUTION_REQUEST=9000;

    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    private boolean mConnected;

    private Activity mContext;
    private Callback mCallback;

    public LocationService(Activity activity) {
        mLocationClient = new LocationClient(activity, this, this);
        mContext = activity;
        mConnected=false;

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(5000);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public boolean isConnected(){
        return mConnected;
    }

    public void start(){
        mLocationClient.connect();
    }

    public void stop(){
        if(mLocationClient.isConnected()) {
            mLocationClient.removeLocationUpdates(this);
        }
        mLocationClient.disconnect();
    }

    public Location getCurrentLocation(){
        return mLocationClient.getLastLocation();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mConnected=true;

        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDisconnected() {
        mConnected=false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mConnected=false;

        if(connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(mContext, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exception : " + e.getMessage());
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    public void showErrorDialog(int errorCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(errorCode, mContext, CONNECTION_FAILURE_RESOLUTION_REQUEST);
        if(dialog != null) {
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            errorFragment.setDialog(dialog);
            errorFragment.show(mContext.getFragmentManager(), "Location Updates");
        }
    }

    public boolean servicesConnected () {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);

        if(ConnectionResult.SUCCESS == resultCode) {
            Log.d(TAG, "Google Play services is available");
            return true;
        } else {
            showErrorDialog(resultCode);
            return false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            mCallback.onCurrentLocation(location);
        }
    }

    static public class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    public interface Callback {
        public void onCurrentLocation(Location location);
    }
}
