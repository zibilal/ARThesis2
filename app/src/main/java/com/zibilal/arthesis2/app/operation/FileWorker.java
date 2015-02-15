package com.zibilal.arthesis2.app.operation;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by bilalmuhammad on 1/19/15.
 */
public class FileWorker implements Runnable {

    private File dir;
    private String[] fileNames;
    private Object[] data;
    private Handler handler;

    public static int WHAT_FILE_WORKER =1;

    private static final String TAG="FileWorker";

    public FileWorker(Handler h){
        dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
        ), "/sensor/data");
        if(!dir.exists()){
            dir.mkdirs();
        }
        handler = h;
        Log.d(TAG, "Directory worker = " + dir.toString());
    }

    public void setFileNames(String... names) {
        fileNames = names;
    }

    public void saveData(Object... data) {
        if(fileNames.length != data.length)
            throw new IllegalStateException("Names and data length must be the same length");
        this.data=data;

        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        if(fileNames == null || data == null)
            throw new IllegalStateException("Illegal state");

        for (int i=0; i < data.length;i++) {
            FileWriter output = null;
            try {
                File file = new File(dir, fileNames[i]);
                output = new FileWriter(file);
                List<String> d = (List<String>) data[i];
                for(String t: d){
                    output.write(t);
                }
                output.flush();
                Message success = new Message();
                success.what= FileWorker.WHAT_FILE_WORKER;
                success.obj="Success saving file " + dir.toString() + "/" + fileNames[i];
                handler.sendMessage(success);
            } catch(Exception e){
                e.printStackTrace();
                Message error = new Message();
                error.what= FileWorker.WHAT_FILE_WORKER;
                error.obj="Error saving file " + e.getMessage() + ": "+ dir.toString() + "/" + fileNames[i];
                handler.sendMessage(error);
            } finally{
                if(output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
