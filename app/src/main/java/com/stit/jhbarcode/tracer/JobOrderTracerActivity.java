package com.stit.jhbarcode.tracer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.ion.Ion;
import com.stit.jhbarcode.MyApplication;
import com.stit.jhbarcode.MyInfo;
import com.stit.jhbarcode.MyMediaPlay;
import com.stit.jhbarcode.R;
import com.stit.jhbarcode.adapter.tracer.joborder.JobOrderTracerListAdapter;
import com.stit.jhbarcode.model.ApcJobOrde;
import com.stit.jhbarcode.model.ApiResponse;
import com.stit.jhbarcode.model.MP3;
import com.stit.jhbarcode.repo.DbHelper;
import com.stit.jhbarcode.repo.MyDao;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 工令領用- 查單
 * attn to recycle keyListner
 */
public class JobOrderTracerActivity extends AppCompatActivity {
    private SharedPreferences mSPerf;
    MyMediaPlay  myMediaPlay;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private MyDao myDao;
    private SQLiteDatabase db;
    private MyApplication myApplication;

    private String empNo;
    private TextView empNoTv;

    private EditText jobOrderEt;
    private TextView previousJobOrderTv;
    private TextView countTv;
    private TextView totalRequTyTv;
    private TextView totalIssuQty;
    private TextView totalFnshQty;


    private ProgressBar progressBar;
    private Button btnQuery;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_order_tracer);
        // -------------------------------------------------
        this.myMediaPlay = new MyMediaPlay(this);
        this.myApplication = (MyApplication) this.getApplication();

        this.db = DbHelper.getInstance(this).getDb();
        this.myDao = new MyDao(this);
        this.mSPerf = this.getSharedPreferences(MyInfo.SPKey, Context.MODE_PRIVATE);

        this.empNo = this.mSPerf.getString("name", "na");
        this.empNoTv = (TextView) this.findViewById(R.id.empNo);
        this.empNoTv.setText(this.empNo);

        // ---------------------------
        this.progressBar = this.findViewById(R.id.progressBar);
        this.progressBar.setVisibility(View.INVISIBLE);

        this.jobOrderEt = this.findViewById(R.id.jobOrder);
        this.previousJobOrderTv = this.findViewById(R.id.previousJobOrder);

        this.countTv = this.findViewById(R.id.count);
        this.totalRequTyTv = this.findViewById(R.id.totalRequTy);
        this.totalIssuQty = this.findViewById(R.id.totalIssuQty);
        this.totalFnshQty = this.findViewById(R.id.totalFnshQty);

        /*
        this.jobOrderEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    System.out.println("mia");
                    recyclerView.clearFocus();
                    v.requestFocus();
                }
            }
        });
         */


        this.jobOrderEt.setOnKeyListener((view, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() != KeyEvent.ACTION_UP) {
                // empty check
                String jobOrder = this.jobOrderEt.getText().toString()
                        .replaceAll("\r", "")
                        .replaceAll("\n", "")
                        .trim();

                if (TextUtils.isEmpty(jobOrder) || jobOrder.length() == 0) {
                    this.myMediaPlay.play(MP3.beepError);
                    showToast("工令不可空白");
                    return false;
                }

                if (jobOrder.contains(",")) {
                    String msg = "工令中含有 \",\" 字元.";
                    this.myMediaPlay.play(MP3.beepError);
                    showToast(msg);
                    return false;
                }

                this.doQuery(null);
                return true;
            }

            return false;
        });

        this.btnQuery = this.findViewById(R.id.btnQuery);
        this.btnQuery.setOnClickListener(this::doQuery);

        //---------------
        this.recyclerView = this.findViewById(R.id.jobOrderTracerRecycleView);

        // tricky
        this.recyclerView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.clearFocus();
                jobOrderEt.requestFocus();
            }
        });

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // test
        //this.jobOrderEt.setText("202010100001");
    }

    private void test(View v) {
        System.out.println("hello");
    }

    // 查單
    private void doQuery(View v) {
        String jobNo = JobOrderTracerActivity.this.jobOrderEt.getText()
                .toString()
                .replaceAll("\r", "")
                .replaceAll("\n", "");

        if (TextUtils.isEmpty(jobNo)) {
            this.showToast("請輸入工令");
            return;
        }

        if (! this.myApplication.isConnected()) {
            String checkNetwork = this.getResources().getString(R.string.check_network);
            this.showAlert(checkNetwork);
            this.myMediaPlay.play(MP3.beepError);
            return;
        }

        this.previousJobOrderTv.setText(jobNo);
        this.jobOrderEt.setText("");


        this.showProgressBar(true);

        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            private String msg;
            private List<ApcJobOrde> tracerList;

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    String url = MyInfo.JH_API_URL + "apcJobOrde/list?jobNo=" + jobNo;
                    ApiResponse<List<ApcJobOrde>> apiResponse = Ion.with(JobOrderTracerActivity.this)
                            .load(url)
                            .as(new TypeToken<ApiResponse<List<ApcJobOrde>>>() {})
                            .get();



                    this.tracerList = apiResponse.data;
                    this.msg = "";
                    return true;
                } catch (Exception ex) {
                    this.msg = ex.getMessage();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    myMediaPlay.play(MP3.sweet);
                    if (this.tracerList.size() == 0) {
                        showToast("查無資料");
                    }

                    adapter = new JobOrderTracerListAdapter(JobOrderTracerActivity.this, tracerList);
                    recyclerView.setAdapter(adapter);

                    JobOrderTracerActivity.this.countTv.setText("筆: "  +tracerList.size());

                    int totalRequQty = 0;
                    int totalIssuQty = 0;
                    int totalFnshQty = 0;
                    for (ApcJobOrde bean : tracerList) {
                        totalRequQty += bean.getRequQty().intValue();
                        totalIssuQty += bean.getIssuQty().intValue();
                        totalFnshQty += bean.getFnshQty().intValue();
                    }
                    JobOrderTracerActivity.this.totalRequTyTv.setText("共: " + String.valueOf(totalRequQty));
                    JobOrderTracerActivity.this.totalIssuQty.setText("領: " + String.valueOf(totalIssuQty));
                    JobOrderTracerActivity.this.totalFnshQty.setText("完: " + String.valueOf(totalFnshQty));

                } else {
                    showAlert(this.msg);
                    myMediaPlay.play(MP3.beepError);
                }

                showProgressBar(false);
                hideKeyboard();
            }
        };

        task.execute();
    }

    private void showAlert(String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(message)
                .setPositiveButton("關閉視窗", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();

        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem mi = menu.add(0, 0, 0, "返回");
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mi.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 0) {
            this.finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        LinearLayout toastLayout = (LinearLayout) toast.getView();
        TextView toastTV = (TextView) toastLayout.getChildAt(0);
        toastTV.setTextSize(20);
        toast.show();
    }

    private void showProgressBar(final boolean value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (value) {
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }
        });
    }

    private  void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
    }

} // class
