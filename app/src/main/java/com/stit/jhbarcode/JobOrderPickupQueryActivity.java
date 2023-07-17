package com.stit.jhbarcode;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
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
import android.widget.ToggleButton;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.ion.Ion;
import com.stit.jhbarcode.adapter.JobOrderListAdapter;
import com.stit.jhbarcode.model.ApiResponse;
import com.stit.jhbarcode.model.DataSourceKind;
import com.stit.jhbarcode.model.Kind;
import com.stit.jhbarcode.model.MP3;
import com.stit.jhbarcode.model.MainData;
import com.stit.jhbarcode.repo.DbHelper;
import com.stit.jhbarcode.repo.MyDao;
import com.stit.jhbarcode.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JobOrderPickupQueryActivity extends AppCompatActivity {
    private String title = "工令領用查詢";

    private MyMediaPlay myMediaPlay;
    private SharedPreferences sPerf;

    private SimpleDateFormat dateFormatWithSlash = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat dateFormatNoSlash = new SimpleDateFormat("yyyyMMdd");

    private MyDao myDao;
    private SQLiteDatabase db;
    private MyApplication myApplication;


    private TextView tvQueryCount;
    private EditText scanDate;
    private ProgressBar progressBar;
    private Button btnQuery;
    private Button btnDatePicker;

    private ToggleButton tbUploaded;

    private RecyclerView rv1;

    //private final String key_scan_date = "jobOrderScanDateKey";
    //private boolean bln = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_order_pickup_query);

        getSupportActionBar().setTitle(this.title);
        // -------------------

        this.myMediaPlay = new MyMediaPlay(this);
        this.myApplication = (MyApplication) this.getApplication();

        this.db = DbHelper.getInstance(this).getDb();
        this.myDao = new MyDao(this);
        this.sPerf = this.getSharedPreferences(MyInfo.SPKey, Context.MODE_PRIVATE);

        this.progressBar = this.findViewById(R.id.progressBar);
        this.scanDate = this.findViewById(R.id.scanDate);

        Date now = new Date();
        this.scanDate.setText(this.dateFormatWithSlash.format(now));

        this.tvQueryCount = this.findViewById(R.id.queryCount);

        Button btnExit = this.findViewById(R.id.btnExit);
        btnExit.setOnClickListener(v ->  this.finish());

        //--------------------------------
        this.btnDatePicker = this.findViewById(R.id.btnDatePicker);
        this.btnDatePicker.setOnClickListener(this::datePickerLsnr);

        this.btnQuery = this.findViewById(R.id.btnQuery);
        this.btnQuery.setOnClickListener(this::doQuery);

        this.tbUploaded = this.findViewById(R.id.uploadedTb);
        this.tbUploaded.setChecked(true);
        //--------------------------------

        this.rv1 = findViewById(R.id.recycleView1);

        RecyclerView.LayoutManager lm = new LinearLayoutManager(this.getApplicationContext());
        rv1.setLayoutManager(lm);
        rv1.setItemAnimator(new DefaultItemAnimator());
        rv1.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

    }

    private void datePickerLsnr(View v) {
        Calendar calendar = null;
        int yy = 0;
        int mm = 0;
        int dd = 0;

        if (TextUtils.isEmpty(this.scanDate.getText().toString())) {
            calendar = Calendar.getInstance();
            yy = calendar.get(Calendar.YEAR);
            mm = calendar.get(Calendar.MONTH);
            dd = calendar.get(Calendar.DAY_OF_MONTH);

            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, dayOfMonth);
                JobOrderPickupQueryActivity.this.scanDate.setText(dateFormatWithSlash.format(cal.getTime()));
            }, yy, mm, dd).show();

        } else {
            String strDate = this.scanDate.getText().toString();
            Date date = DateUtils.string2Date(strDate);
            if (date != null) {
                calendar = Calendar.getInstance();
                calendar.setTime(date);
                yy = calendar.get(Calendar.YEAR);
                mm = calendar.get(Calendar.MONTH);
                dd = calendar.get(Calendar.DAY_OF_MONTH);
            } else {
                calendar = Calendar.getInstance();
                yy = calendar.get(Calendar.YEAR);
                mm = calendar.get(Calendar.MONTH);
                dd = calendar.get(Calendar.DAY_OF_MONTH);
            }

            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, dayOfMonth);
                JobOrderPickupQueryActivity.this.scanDate.setText(dateFormatWithSlash.format(cal.getTime()));
            }, yy, mm, dd).show();
        }
    }

    private void doQuery(View v) {
        String scanDate = this.scanDate.getText().toString();
        if (TextUtils.isEmpty(scanDate)) {
            showToast("日期空白");
            return;
        }

        java.util.Date _date = DateUtils.string2Date(scanDate);
        if (_date == null) {
            showToast(getString(R.string.date_format_error));
            return;
        }

        MyTask myTask = new MyTask();
        myTask.execute();
    }


    private class MyTask extends AsyncTask<Void, Void, Boolean> {
        private List<MainData> mainDataList = null;
        private Boolean uploaded;
        private String scanDate;
        private String message;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.uploaded = JobOrderPickupQueryActivity.this.tbUploaded.isChecked();
            this.scanDate = JobOrderPickupQueryActivity.this.scanDate.getText().toString();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            showProgressBar(true);

            // 已上傳
            if (uploaded) {
                if (! myApplication.isConnected()) {
                    message = getString(R.string.check_network);
                    return false;
                }

                try {
                    String url = MyInfo.JH_API_URL +
                            "askCoilScanTemp" +
                            "?kind=" + Kind.JobOrderPickup.getValue() +
                            "&scanDate=" + this.scanDate;

                    url = url.replaceAll(" ", "%20");
                    //System.out.println(url);

                    ApiResponse<List<MainData>> apiResponse = Ion.with(JobOrderPickupQueryActivity.this)
                            .load("GET", url)
                            .as(new TypeToken<ApiResponse<List<MainData>>>() {})
                            .get(15, TimeUnit.SECONDS);


                    this.mainDataList = apiResponse.data;
                    return true;

                } catch (InterruptedException interEx) {
                    this.message = getString(R.string.network_error);
                    return false;
                } catch (ExecutionException execEx) {
                    this.message = getString(R.string.timeout);
                    return false;
                } catch (TimeoutException timeoutEx) {
                    this.message = getString(R.string.timeout);
                    return false;
                } catch (Exception ex) {
                    this.message = ex.toString();
                    return false;
                }
            } else {    // sqlite
                java.util.Date _scanDate = DateUtils.string2Date(scanDate);
                mainDataList = myDao.getMainDataList(Kind.JobOrderPickup, _scanDate);
                return true;
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            showProgressBar(false);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            DataSourceKind dsKind = uploaded ? DataSourceKind.REMOATE : DataSourceKind.LOCAL;
            if (success) {
                JobOrderListAdapter adapter = new JobOrderListAdapter(
                        JobOrderPickupQueryActivity.this,
                        mainDataList,
                        dsKind,
                        tvQueryCount
                );

                rv1.setAdapter(adapter);

                tvQueryCount.setText(mainDataList.size() + "");
                if (mainDataList.size() == 0) {
                    showToast("查無資料");
                }

                myMediaPlay.play(MP3.sweet);
            } else {
                mainDataList = new ArrayList<>();
                JobOrderListAdapter adapter = new JobOrderListAdapter(
                        JobOrderPickupQueryActivity.this,
                        mainDataList,
                        dsKind,
                        tvQueryCount
                );

                rv1.setAdapter(adapter);
                showAlert(message);
                myMediaPlay.play(MP3.beepError);
            }

            showProgressBar(false);
            hideKeyboard();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem mi = menu.add(0, 0, 0, "返回");
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mi.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            this.finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
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

    private void showToast(String msg) {
        boolean isUIThread = Looper.getMainLooper().getThread() == Thread.currentThread();
        if (isUIThread) {
            Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
            LinearLayout toastLayout = (LinearLayout) toast.getView();
            TextView toastTV = (TextView) toastLayout.getChildAt(0);
            toastTV.setTextSize(20);
            toast.show();
        } else {
            runOnUiThread(() -> {
                Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
                LinearLayout toastLayout = (LinearLayout) toast.getView();
                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                toastTV.setTextSize(20);
                toast.show();
            });
        }
    }

    private void showAlert(String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alertDialog_title))
                .setMessage(message)
                .setPositiveButton("關閉視窗", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();

        alertDialog.show();
    }

    private  void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusView = this.getCurrentFocus();
        if (focusView != null) {
            imm.hideSoftInputFromWindow(focusView.getWindowToken(),0);
        }
    }

} // class
