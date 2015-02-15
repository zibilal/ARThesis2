package com.zibilal.arthesis2.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.zibilal.arthesis2.app.operation.SensorData;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.List;


public class SensorDateTest extends Activity implements SensorEventListener {

    private CheckBox lowPassFilter;
    private CheckBox showGraphics;
    private RadioButton accSensor;
    private RadioButton magSensor;
    private RadioGroup selectSensor;
    private TextView textInfo;

    private Button startButton;
    private Button endButton;
    private LinearLayout chartContainer;
    private List<SensorData> sensorDatas;
    private boolean isGravity=false;
    private boolean isStart=false;

    private SensorManager sensorManager;

    private LayoutInflater layoutInflater;

    private float[] gravities = {0f, 0f, 0f};
    private float[] magnetics = {0f, 0f, 0f};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorDatas = new ArrayList<SensorData>();

        setContentView(R.layout.activity_sensor_date_test);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        lowPassFilter = (CheckBox) findViewById(R.id.lowPassCheckbox);
        showGraphics = (CheckBox) findViewById(R.id.show_graphics);
        accSensor = (RadioButton) findViewById(R.id.acc_sensor);
        magSensor = (RadioButton) findViewById(R.id.mag_sensor);
        selectSensor = (RadioGroup) findViewById(R.id.selectSensor);
        textInfo = (TextView) findViewById(R.id.text_info);
        selectSensor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                isGravity =  checkedId == R.id.acc_sensor? true : false;
            }
        });
        startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStart=true;
                if(sensorDatas.size() > 0) sensorDatas.clear();

                Sensor sensor;

                if(isGravity) {
                    sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                } else {
                    sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                }

                sensorManager.registerListener(SensorDateTest.this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

                String msg="" + (lowPassFilter.isChecked()? "With LowPass filter..." : "");
                if(isGravity) {
                    textInfo.setText("Collecting data accelerometer..." + msg);
                } else {
                    textInfo.setText("Collecting data magnetometer..." + msg);
                }

                chartContainer.removeAllViews();
            }
        });
        endButton = (Button) findViewById(R.id.end_button);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStart=false;
                chartContainer.removeAllViews();
                if(showGraphics.isChecked()) {
                    openChart();
                } else {
                    openList();
                }

                sensorManager.unregisterListener(SensorDateTest.this);

                textInfo.setText("");
            }
        });

        chartContainer = (LinearLayout) findViewById(R.id.chart_container);

        isGravity = selectSensor.getCheckedRadioButtonId() == R.id.acc_sensor ? true : false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            if(isGravity && isStart) {
                if(lowPassFilter.isChecked()) {
                    float[] lowpass = lowPassFilter(event.values, gravities);
                    SensorData data = new SensorData(System.currentTimeMillis(),
                            lowpass[0], lowpass[1], lowpass[2]);
                    sensorDatas.add(data);
                    gravities = event.values;
                } else {
                    SensorData data = new SensorData(System.currentTimeMillis(),
                            event.values[0], event.values[1], event.values[2]);
                    sensorDatas.add(data);
                }
            }
        } else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

            if(!isGravity && isStart) {
                if(lowPassFilter.isChecked()) {
                    float[] lowpass = lowPassFilter(event.values, magnetics);
                    SensorData data = new SensorData(System.currentTimeMillis(),
                            lowpass[0], lowpass[1], lowpass[2]);
                    sensorDatas.add(data);
                    magnetics = event.values;
                } else {
                    SensorData data = new SensorData(System.currentTimeMillis(),
                            event.values[0], event.values[1], event.values[2]);
                    sensorDatas.add(data);
                }
            }
        }
    }

    private float[] lowPassFilter(float[] current, float[] before) {
        float[] result = {0f, 0f, 0f};
        float alpha=0.8f;
        result[0] = alpha * before[0] + (1 - alpha) * current[0];
        result[1] = alpha * before[1] + (1 - alpha) * current[1];
        result[2] = alpha * before[2] + (1 - alpha) * current[2];
        return result;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public class DataAdapter extends BaseAdapter {

        int resouceId;

        public DataAdapter(int resource) {
            super();
            resouceId=resource;
        }

        @Override
        public int getCount() {
            return sensorDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return sensorDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null) {
                convertView = layoutInflater.inflate(resouceId, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.xView =(TextView) convertView.findViewById(R.id.text_x);
                viewHolder.yView = (TextView) convertView.findViewById(R.id.text_y);
                viewHolder.zView = (TextView) convertView.findViewById(R.id.text_z);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }


            SensorData data = (SensorData) getItem(position);
            viewHolder.xView.setText(String.format("%3.4f", data.getX()) );
            viewHolder.yView.setText(String.format("%3.4f", data.getY()) );
            viewHolder.zView.setText(String.format("%3.4f", data.getZ()) );

            return convertView;
        }
    }

    class ViewHolder {
        TextView xView;
        TextView yView;
        TextView zView;
    }

    private void openList() {
        if(sensorDatas != null && sensorDatas.size() > 0) {
            ListView listView = new ListView(this);
            DataAdapter adapter = new DataAdapter(R.layout.layout_item);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            chartContainer.addView(listView);
        }
    }

    private void openChart() {
        if(sensorDatas != null && sensorDatas.size() > 0) {
            long t = sensorDatas.get(0).getTimestamp();
            XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

            XYSeries xSeries = new XYSeries("X");
            XYSeries ySeries = new XYSeries("Y");
            XYSeries zSeries = new XYSeries("Z");

            long temp;

            for(SensorData data : sensorDatas) {
                temp = data.getTimestamp() - t;
                xSeries.add(temp, data.getX());
                ySeries.add(temp, data.getY());
                zSeries.add(temp, data.getZ());
            }

            dataset.addSeries(xSeries);
            dataset.addSeries(ySeries);
            dataset.addSeries(zSeries);

            XYSeriesRenderer xRenderer = new XYSeriesRenderer();
            xRenderer.setColor(Color.RED);
            xRenderer.setPointStyle(PointStyle.CIRCLE);
            xRenderer.setFillPoints(true);
            xRenderer.setLineWidth(1);
            xRenderer.setDisplayChartValues(false);

            XYSeriesRenderer yRenderer = new XYSeriesRenderer();
            yRenderer.setColor(Color.GREEN);
            yRenderer.setPointStyle(PointStyle.CIRCLE);
            yRenderer.setFillPoints(true);
            yRenderer.setLineWidth(1);
            yRenderer.setDisplayChartValues(false);

            XYSeriesRenderer zRenderer = new XYSeriesRenderer();
            zRenderer.setColor(Color.BLUE);
            zRenderer.setPointStyle(PointStyle.CIRCLE);
            zRenderer.setFillPoints(true);
            zRenderer.setLineWidth(1);
            zRenderer.setDisplayChartValues(false);

            XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
            multiRenderer.setXLabels(0);
            multiRenderer.setLabelsColor(Color.RED);
            multiRenderer.setChartTitle("t vs (x,y,z)");
            multiRenderer.setXTitle("Sensor Data");
            multiRenderer.setYTitle("Values of Acceleration");
            multiRenderer.setZoomButtonsVisible(true);
            for (int i = 0; i < sensorDatas.size(); i++) {

                multiRenderer.addXTextLabel(i + 1, ""
                        + (sensorDatas.get(i).getTimestamp() - t));
            }
            for (int i = 0; i < 12; i++) {
                multiRenderer.addYTextLabel(i + 1, ""+i);
            }

            multiRenderer.addSeriesRenderer(xRenderer);
            multiRenderer.addSeriesRenderer(yRenderer);
            multiRenderer.addSeriesRenderer(zRenderer);

            // Getting a reference to LinearLayout of the MainActivity Layout

            // Creating a Line Chart
            View chartView = ChartFactory.getLineChartView(getBaseContext(), dataset,
                    multiRenderer);

            // Adding the Line Chart to the LinearLayout
            chartContainer.addView(chartView);
        }
    }
}
