package com.stit.jhbarcode;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;


public class MyApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();

    // this.sp = this.getSharedPreferences(MyInfo.SPKey, Context.MODE_PRIVATE);

  }
  public boolean isConnected(){
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isConnected()) {
      return true;
    }
    return false;
  }

}  // end class
