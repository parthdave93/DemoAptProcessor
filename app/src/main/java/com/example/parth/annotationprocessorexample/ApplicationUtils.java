package com.example.parth.annotationprocessorexample;

import android.app.Application;
import android.util.Log;

import com.parthdave.annotations.FacebookInit;

/**
 * Created by sotsys-014 on 16/1/17.
 */

@FacebookInit
public class ApplicationUtils extends Application {

    public boolean isDebug = false;

    @Override
    public void onCreate() {
        super.onCreate();
        isDebug = true;
        Log.d("asdasd","asdasd");
    }
}
