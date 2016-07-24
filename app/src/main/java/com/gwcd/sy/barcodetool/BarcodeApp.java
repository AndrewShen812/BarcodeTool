package com.gwcd.sy.barcodetool;

import android.app.Application;

public class BarcodeApp extends Application{
    
    private static BarcodeApp mInstance;
    
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        mInstance = this;
    }
    
    public static BarcodeApp getInstance() {
        return mInstance;
    }
}
