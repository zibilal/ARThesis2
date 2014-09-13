package com.zibilal.arthesis2.app.views;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by bmuhamm on 5/22/14.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback{

    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraView(Context context) {
        super(context);
        init();
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Camera getCamera() {
        return mCamera;
    }

    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            mCamera = Camera.open();
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        Camera.Parameters params = mCamera.getParameters();
        Camera.Size size = getBestPreviewSize(width, height, params);
        params.setPreviewSize(size.width, size.height);
        mCamera.setParameters(params);
        mCamera.startPreview();
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters params) {
        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = params.getSupportedPreviewSizes();

        float ff = (float) width / height;
        float bff = 0f;

        if(sizeList != null && sizeList.size() > 0) {
            bestSize = sizeList.get(0);

            for (Camera.Size sz : sizeList) {
                float cff = (float) sz.width / sz.height;
                if ((ff - cff <= ff - bff) && (sz.width <= width) && (sz.width >= bestSize.width)) {
                    bff = cff;
                    bestSize = sz;
                }
            }
        }

        if(bestSize == null) {
            bestSize = mCamera.new Size(480, 300);
        }
        return bestSize;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera=null;
    }
}
