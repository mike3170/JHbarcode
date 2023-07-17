package com.stit.jhbarcode;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.material.snackbar.Snackbar;
import com.stit.jhbarcode.model.Kind;
import com.stit.jhbarcode.repo.MyDao;


public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private MyDao myDao;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.myDao = new MyDao(this);
        mPreferences = getSharedPreferences(MyInfo.SPKey, Context.MODE_PRIVATE);

        //  登出
        Button btnLogout = this.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener((view) -> {
            this.logout();
        });


        // 1.工令領用
        Button btnJobOrderPickup = this.findViewById(R.id.btnJobOrderPickup);
        btnJobOrderPickup.setOnClickListener((view) -> {
            Intent intent = new Intent(this, JobOrderPickupActivity.class);
            this.startActivity(intent);
        });

        // 2.線材移轉
        Button btnLocationMove = this.findViewById(R.id.btnLocationMove);
        btnLocationMove.setOnClickListener((view) -> {
            Intent intent = new Intent(this, LocationMoveActivity.class);
            this.startActivity(intent);
        });

        // 3.線材退料
        Button btnMatReturn = this.findViewById(R.id.btnMatReturn);
        btnMatReturn.setOnClickListener((view) -> {
            Intent intent = new Intent(this, MatReturnActivity.class);
            this.startActivity(intent);
        });

        // 4.電鍍接收
        Button btnPlatingReception = this.findViewById(R.id.btnPlatingReception);
        btnPlatingReception.setOnClickListener((view) -> {
            Intent intent = new Intent(this, PlatingReceptionActivity.class);
            this.startActivity(intent);
        });

        // 5.酸洗完工
        Button btnPicklingFinished = this.findViewById(R.id.btnPicklingFinished);
        btnPicklingFinished.setOnClickListener((view) -> {
            Intent intent = new Intent(this, PicklingFinishedActivity.class);
            this.startActivity(intent);
        });


        Button btnTest = this.findViewById(R.id.btnTest);
        btnTest.setVisibility(View.INVISIBLE);
        btnTest.setOnClickListener(v -> {
            //List<MainData> mainDataList = this.myDao.getMainDataList(Kind.LocationMove);
            //for (MainData d : mainDataList) {
            //    System.out.println(d);
            //    //System.out.println(d.getClassNo());
            //}
            //List<CodMast> list = this.myDao.getCodMastList();
            //System.out.println(list.size());
            //for (CodMast d : list) {
            //    System.out.println(d);
            //    //System.out.println(d.getClassNo());
            //}

            //List<MainData> mainDataList = this.myDao.getMainDataList(Kind.PicklingFinished);
            //for (MainData d : mainDataList) {
            //    System.out.println(d);
            //}

            //this.myDao.deleteMainData(Kind.PlatingReception);

            Intent intent = new Intent(this, TestActivity.class);
            startActivity(intent);

            //Snackbar.make(getWindow().getDecorView(), "Hello mia", Snackbar.LENGTH_LONG)
            //        .setAction("David", null)
            //        .show();

        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            this.logout();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        this.mPreferences.edit().remove("name").commit();
        this.finishAffinity();
    }

} // end class
