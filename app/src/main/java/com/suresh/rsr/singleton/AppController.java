package com.suresh.rsr.singleton;

import android.app.Application;

import com.suresh.rsr.reciever.ConnectivityReceiver;


public class AppController extends Application {
    public static final String TAG = AppController.class
            .getSimpleName();


    private static AppController mInstance;

    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;



    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;



    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }




    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }


}
