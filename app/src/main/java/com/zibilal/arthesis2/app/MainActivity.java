package com.zibilal.arthesis2.app;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private List<ActivityObjects> objects;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);

        init();
    }

    private void init(){
        objects = new ArrayList<ActivityObjects>();
        objects.add(new ActivityObjects("ARActivity", ARActivity.class));
        objects.add(new ActivityObjects("Sensor Data Test", SensorDateTest.class));
        objects.add(new ActivityObjects("SensorActivity", SensorsActivity.class));
        objects.add(new ActivityObjects("SensorActivity2", com.zibilal.arthesis2.app.outside.SensorsActivity.class));
        objects.add(new ActivityObjects("MatrixTestActivity", MatrixTestActivity.class));
        objects.add(new ActivityObjects("GooglePlacesAPIAutocompleteTest", GooglePlacesAPIAutocompleteTest.class));

        ArrayAdapter<ActivityObjects> adapter = new ArrayAdapter<ActivityObjects>(this, android.R.layout.simple_list_item_1, objects);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, objects.get(i).getActivity());
                startActivity(intent);
            }
        });

    }

    class ActivityObjects {
        private String name;
        private Class<? extends Activity> activity;

        public ActivityObjects(String name, Class<? extends Activity> activity) {
            this.name = name;
            this.activity=activity;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Class<? extends Activity> getActivity() {
            return activity;
        }

        public void setActivity(Class<? extends Activity> activity) {
            this.activity = activity;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
