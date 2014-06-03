package com.zibilal.arthesis2.app.camera;

import android.util.Log;

import com.zibilal.arthesis2.app.operation.Vector;

/**
 * Created by bmuhamm on 5/23/14.
 */
public class PointOfView {

    public static final float DEFAULT_VIEW_ANGLE=(float) Math.toRadians(45);

    private static final String TAG="PointOfView";

    private int mWidth=0;
    private int mHeight=0;
    private float mViewAngle=0f;
    private float mDistance=0f;

    public PointOfView(int width, int height) {
        mWidth = width;
        mHeight = height;
        initPov(DEFAULT_VIEW_ANGLE);
    }

    private void initPov(float viewAngle) {
        this.mViewAngle=viewAngle;
        this.mDistance=(mWidth / 2) / (float) Math.tan(mViewAngle / 2);
    }

    public void setViewAngle(float viewAngle) {
        mViewAngle=viewAngle;
        mDistance=(mWidth / 2) / (float)  Math.tan(mViewAngle / 2);
    }

    public void projectPoint(Vector input, Vector output ) {
        float[] temp1 = new float[3];
        float[] temp2 = new float[3];

        input.get(temp1);
        Log.d(TAG, String.format("\n\n-- Width : %d Height : %d ViewAngle : %.4f Distance : %.4f", mWidth, mHeight, mViewAngle, mDistance));
        Log.d(TAG, String.format("-- Input point = %.4f %.4f %.4f", temp1[0], temp1[1], temp1[2]));
        temp2[0] = (mDistance * temp1[0]) / -temp1[2];
        temp2[1] = (mDistance * temp1[1]) / -temp1[2];
        temp2[2] = temp1[2];

        temp2[0] = temp2[0] + mWidth / 2;
        temp2[1] = -temp2[1] + mHeight / 2;
        Log.d(TAG, String.format("-- Output point = %.4f %.4f %.4f", temp2[0], temp2[1], temp2[2]));
        output.set(temp2);
    }

    public void projectPoint(Vector input, com.zibilal.arthesis2.app.outside.Vector output ) {
        float[] temp1 = new float[3];
        float[] temp2 = new float[3];

        input.get(temp1);
        Log.d(TAG, String.format("\n\n-- Width : %d Height : %d ViewAngle : %.4f Distance : %.4f", mWidth, mHeight, mViewAngle, mDistance));
        Log.d(TAG, String.format("-- Input point = %.4f %.4f %.4f", temp1[0], temp1[1], temp1[2]));
        temp2[0] = (mDistance * temp1[0]) / -temp1[2];
        temp2[1] = (mDistance * temp1[1]) / -temp1[2];
        temp2[2] = temp1[2];

        temp2[0] = temp2[0] + mWidth / 2;
        temp2[1] = -temp2[1] + mWidth / 2;
        Log.d(TAG, String.format("-- Output point = %.4f %.4f %.4f", temp2[0], temp2[1], temp2[2]));
        output.set(temp2);
    }

    @Override
    public String toString() {
        return String.format("[PointOfView] width:%d height:%d viewangle:%.4f distance:%.4f", mWidth, mHeight, mViewAngle, mDistance);
    }
}
