package com.stit.jhbarcode.utils;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MyUtil {

  public static void showToask(Context context, String msg) {
    Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);

    LinearLayout toastLayout = (LinearLayout) toast.getView();
    TextView tv1 = (TextView) toastLayout.getChildAt(0);
    tv1.setTextSize(20);

    toast.show();
  }

  public static void showDialog(Context context, String msg) {
    Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);

    LinearLayout toastLayout = (LinearLayout) toast.getView();
    TextView tv1 = (TextView) toastLayout.getChildAt(0);
    tv1.setTextSize(20);

    toast.show();
  }

  public static String stringToHex(String string) {
    StringBuilder buf = new StringBuilder(200);
    for (char ch: string.toCharArray()) {
      if (buf.length() > 0)
        buf.append(' ');
      buf.append(String.format("%04x", (int) ch));
    }
    return buf.toString();
  }

} // end class
