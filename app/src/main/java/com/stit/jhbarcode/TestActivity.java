package com.stit.jhbarcode;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.koushikdutta.ion.Ion;
import com.stit.jhbarcode.model.ApiResponse;
import com.stit.jhbarcode.model.MP3;
import com.stit.jhbarcode.utils.MyUtil;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.spec.ECField;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class TestActivity extends AppCompatActivity {
    HashMap<Integer, Integer> mHashMap= null;

    MyMediaPlay myMediaPlay;

    IntentIntegrator scanIntegrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        String title = getSupportActionBar().getTitle().toString();
        title = "工令領用查詢";
        getSupportActionBar().setTitle(title);
        //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        //getSupportActionBar().setCustomView(R.layout.custom_title_bar);

        Button btnTest = this.findViewById(R.id.btnTest);
        btnTest.setOnClickListener(v -> {
            showAlert("my title");
        });

        Button btnDatePicker = this.findViewById(R.id.btnDatePicker);
        btnDatePicker.setOnClickListener(this::getDate);

        Button btnDateTest = this.findViewById(R.id.btnDateTest);
        btnDateTest.setOnClickListener(this::dateTest);

        Button btnProc = this.findViewById(R.id.btnProc);
        btnProc.setOnClickListener(this::execProc);


        Button btnHello = this.findViewById(R.id.btnMyTest);
        btnHello.setOnClickListener(v -> {
            String line = "http://localhost:8080?name=aaa bbb";
            try {
                //String foo = URLEncoder.encode(line, "UTF-8");
                //System.out.println(foo);

                //String bar = URLDecoder.decode(foo, "UTF-8");
                //System.out.println(bar);
                System.out.println(line.replaceAll(" ", "%20"));


            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

        });

    }

    private void dateTest(View v) {
        try {
            ApiResponse<Integer> apiResponse =
                Ion.with(this)
                   .load("GET", MyInfo.JH_API_URL + "test")
                   //.setJsonPojoBody(mainData)
                   .as(new TypeToken<ApiResponse<Integer>>() { })
                   .get();
        } catch (Exception ex) {

        }
    }

    private void execProc(View v) {
        try {
            String url = MyInfo.JH_API_URL + "jobOrder/check" +
                    "?jobNo=JOB001" +
                    "&itemNo=1" +
                    "&procDate=2020-10-10" +
                    "&coilNo=W123456789" +
                    "&procEmp=dav";

            System.out.println(url);

            ApiResponse<String> apiResponse =
                    Ion.with(this)
                            .load("GET", url)
                            .as(new TypeToken<ApiResponse<String>>() { })
                            .get();

            System.out.println(apiResponse.status.toString());
            System.out.println(apiResponse.data + "-mia");
            System.out.println(apiResponse.error.desc);

        } catch (Exception ex) {
            showToast(ex.getMessage());
            ex.printStackTrace();
        }

    }



    private void getDate(View v) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                System.out.println(year);
                System.out.println(month +  1);
                System.out.println(dayOfMonth);

            }
        }, year, month, day).show();


    }

    private void showAlert(String title2) {
        String title = "此工令單已無此規格帶領量!";
        String message =
                "工令單號: W20020003\n" +
                "材質: SCM435RCH\n" +
                "領用線徑: 15.63\n" +
                "領用製程: 0121A\n" +
                "Total:0\n";

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("關閉視窗", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();

        alertDialog.show();
    }

    private void showToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        LinearLayout toastLayout = (LinearLayout) toast.getView();
        TextView toastTV = (TextView) toastLayout.getChildAt(0);
        toastTV.setTextSize(20);
        toast.show();
    }




}
