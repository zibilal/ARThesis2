package com.zibilal.arthesis2.app.operation;

import android.util.FloatMath;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class LowPassFilter {

    /*
     * Time smoothing constant for low-pass filter 0 ≤ α ≤ 1 ; a smaller value
     * basically means more smoothing See:
     * http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
     */
    private static final float ALPHA_DEFAULT = 0.333f;
    private static final float ALPHA_STEADY = 0.001f;
    private static final float ALPHA_START_MOVING = 0.1f;
    private static final float ALPHA_MOVING = 0.9f;

    public static final String SENSOR_MAGNETIC="magnetic";
    public static final String SENSOR_ACCELL="accell";

    private static final String TAG="LowPassFilter";

    private static List<String> dataAccell=new ArrayList<String>();
    private static List<String> dataMagnet=new ArrayList<String>();

    private LowPassFilter() {
    }

    /**
     * Filter the given input against the previous values and return a low-pass
     * filtered result.
     * 
     * @param low
     *            lowest alpha threshold
     * @param high
     *            highest alpha threshold
     * @param current
     *            float array to smooth.
     * @param previous
     *            float array representing the previous values.
     * @param sensorName
     * @return float array smoothed with a low-pass filter.
     */
    public static float[] filter(float low, float high, float[] current, float[] previous, String sensorName) {
        if (current == null || previous == null) throw new NullPointerException("input and prev float arrays must be non-NULL");
        if (current.length != previous.length) throw new IllegalArgumentException("input and prev must be the same length");

        float alpha = computeAlpha(low, high, current, previous);
        String msg = String.format("%f,%f,%f,%f,%f,%f,%f\n", alpha, current[0],
                current[1],current[2],previous[0],previous[1],previous[2]);
        if(sensorName != null && sensorName.length() > 0) {
            if(sensorName.equals(SENSOR_ACCELL)){
                dataAccell.add(msg);
            }else if(sensorName.equals(SENSOR_MAGNETIC)){
                dataMagnet.add(msg);
            }
        }

        for (int i = 0; i < current.length; i++) {
            previous[i] = previous[i] + alpha * (current[i] - previous[i]);
        }
        return previous;
    }

    public static List<String> getDataAccell(){
        return dataAccell;
    }

    public static List<String> getDataMagnet(){
        return dataMagnet;
    }

    // contoh lain standard deviasi
    // deteksi kecepatan perubahan data
    // kalo std 0 , data tidak perubahan
    // kalo tinggi , perubahan sangat tinggi
    // kalo rendah (-) , cenderung rendah perubahannya.
    // berbanding terbalik dengan standard deviasi.

    private static final float computeAlpha(float low, float high, float[] current, float[] previous) {
        if (previous.length != 3 || current.length != 3) return ALPHA_DEFAULT;

        float x1 = current[0], y1 = current[1], z1 = current[2];
        float x2 = previous[0], y2 = previous[1], z2 = previous[2];
        float distance = FloatMath.sqrt((float)(Math.pow((x2 - x1), 2d) + Math.pow((y2 - y1), 2d) + Math.pow((z2 - z1), 2d)));

        if (distance < low) {
            return ALPHA_STEADY;
        } else if (distance >= low || distance < high) {
            return ALPHA_START_MOVING;
        }
        return ALPHA_MOVING;
    }
}
