package com.zibilal.arthesis2.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.zibilal.arthesis2.app.data.ARGlobal;
import com.zibilal.arthesis2.app.data.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by bmuhamm on 5/22/14.
 */
public class AugmentedView extends View {

    private static final String TAG="AugmentedView";

    private List<Marker> markers;
    private static final AtomicBoolean drawing = new AtomicBoolean(false);

    public AugmentedView(Context context) {
        super(context);
    }

    public AugmentedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(drawing.compareAndSet(false, true)) {
            List<Marker> collection = ARGlobal.getMarkers();
            ListIterator<Marker> iter = collection.listIterator(collection.size());
            while(iter.hasPrevious()) {
                Marker marker = iter.previous();
                Log.d(TAG, "*******Update and draw canvas -->> " + marker.getName());
                marker.updateAndDraw(canvas);
            }
            drawing.set(false);
        }
    }
}
