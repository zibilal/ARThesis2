package com.zibilal.arthesis2.app;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.zibilal.arthesis2.app.operation.SensorData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LowPassTestActivity extends ActionBarActivity {

    private EditText editAlpha;
    private EditText editLow;
    private EditText editHigh;

    private Handler workerHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_low_pass_test);

        editAlpha = (EditText) findViewById(R.id.alphaET);
        editLow = (EditText) findViewById(R.id.lowET);
        editHigh = (EditText) findViewById(R.id.highET);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_low_pass_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onUpdate(View view) {
        if(editAlpha.getText() != null && editAlpha.getText().toString().length() > 0) {
            AsyncTask<Void,Void,Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    BufferedReader reader = null;
                    try {
                        File dir = new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS
                        ), "/sensor/data");
                        File file = new File(dir,"accellraw.csv");
                        reader = new BufferedReader(new FileReader(file));
                        String tmp;
                        List<SensorData> list = new ArrayList<SensorData>();
                        while((tmp = reader.readLine()) != null) {
                            String[] split = tmp.split(",");
                            SensorData d = new SensorData(System.currentTimeMillis(),
                                    Double.parseDouble(split[0]),
                                    Double.parseDouble(split[1]),
                                    Double.parseDouble(split[2]));

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if(reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    return null;
                }
            };
            task.execute();

        } else if(
                (editLow.getText() != null && editLow.getText().toString().length() > 0) &&
                        (editHigh.getText() != null && editHigh.getText().toString().length() > 0)
                ) {

        }
    }
}
