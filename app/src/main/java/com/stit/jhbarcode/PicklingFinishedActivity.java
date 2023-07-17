package com.stit.jhbarcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.koushikdutta.ion.Ion;
import com.stit.jhbarcode.model.ApiResponse;
import com.stit.jhbarcode.model.CodMast;
import com.stit.jhbarcode.model.CodMastKind;
import com.stit.jhbarcode.model.Kind;
import com.stit.jhbarcode.model.MP3;
import com.stit.jhbarcode.model.MainData;
import com.stit.jhbarcode.repo.DbHelper;
import com.stit.jhbarcode.repo.MyDao;
import com.stit.jhbarcode.tracer.PicklingFinishedTracerActivity;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 酸洗完工
 */
public class PicklingFinishedActivity extends AppCompatActivity {
    private SharedPreferences mSPerf;
    private MyMediaPlay myMediaPlay;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private MyDao myDao;
    private SQLiteDatabase db;
    private MyApplication myApplication;

    private String empNo;
    private TextView empNoTv;
    private ToggleButton dayOrNightTb;
    private ToggleButton passsOrNgTb;
    private EditText barcodeEt;
    private TextView barcodePrev;
    private ProgressBar progressBar;

    private Button btnScan;
    private Button btnAdd;
    private Button btnUpload;
    private Button btnQuery;
    private Button btnTracer;

    private List<CodMast> codMastList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickling_finished);
        // -------------------------------------------------
        this.myMediaPlay = new MyMediaPlay(this);
        this.myApplication = (MyApplication) this.getApplication();

        this.db = DbHelper.getInstance(this).getDb();
        this.myDao = new MyDao(this);
        this.mSPerf = this.getSharedPreferences(MyInfo.SPKey, Context.MODE_PRIVATE);

        this.empNo =  this.mSPerf.getString("name", "na");
        this.empNoTv = (TextView) this.findViewById(R.id.empNo);
        this.empNoTv.setText(this.empNo);

        // ---------------------------
        this.progressBar = this.findViewById(R.id.progressBar);
        this.progressBar.setVisibility(View.INVISIBLE);

        this.dayOrNightTb = this.findViewById(R.id.btnDayOrNight);
        this.passsOrNgTb = this.findViewById(R.id.btnPassOrNg);

        this.barcodeEt = this.findViewById(R.id.barcode);
        this.barcodePrev = this.findViewById(R.id.barcodePrev);

        this.btnScan = this.findViewById(R.id.btnScan);
        this.btnScan.setOnClickListener(this::scanBarcode);

        this.btnUpload = this.findViewById(R.id.btnUpload);
        this.btnQuery = this.findViewById(R.id.btnQuery);
        this.btnTracer = this.findViewById(R.id.btnTracer);

        this.btnAdd = this.findViewById(R.id.btnAdd);
        this.btnAdd.setOnClickListener((view) -> {
            if (! this.isDataValid()) {
                return;
            }
            this.insertData();
        });

        //
        this.barcodeEt.setOnKeyListener((view, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() != KeyEvent.ACTION_UP) {
                this.barcodePrev.setText(this.barcodeEt.getText());

                // empty check
                if (! this.isDataValid()) {
                    return false;
                }

                this.insertData();
            }
            return false;
        });

        // query, RecycleView
        btnQuery.setOnClickListener(view -> {
            Intent intent = new Intent(PicklingFinishedActivity.this, PicklingFinishedQueryActivity.class);
            startActivity(intent);
        });

        // 查單
        this.btnTracer.setOnClickListener(v -> {
            // this.showToast("hello");
            Intent intent = new Intent(this, PicklingFinishedTracerActivity.class);
            this.startActivity(intent);
        });

        // --------------------------------------
        btnUpload.setOnClickListener(view -> {
            //this.showToast("not yet");
            if (! this.myApplication.isConnected()) {
                Toast.makeText(this, getString(R.string.check_network), Toast.LENGTH_SHORT).show();
                return;
            }

            showProgressBar(true);
            new PicklingFinishedActivity.UploadTask().execute();
        });

    }  // onCreate


    private boolean isDataValid() {
        String barcode = this.barcodeEt.getText().toString()
                .replaceAll("\r", "")
                .replace("\n", "")
                .trim();

        if (barcode == null || TextUtils.isEmpty(barcode)) {
            this.myMediaPlay.play(MP3.beepError);
            this.showToast("卷號空白!");
            return false;
        }

        if (barcode.contains(",")) {
            this.myMediaPlay.play(MP3.beepError);
            this.showToast("卷號中含有 , 字元!");
            return false;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            protected Void doInBackground(Void... voids) {
                PicklingFinishedActivity.this.codMastList =
                        PicklingFinishedActivity.this.myDao.getCodMastList(CodMastKind.CLAS);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                PicklingFinishedActivity.this.codMastList.remove(0);  // C-夜班
                for (CodMast cm : PicklingFinishedActivity.this.codMastList) {
                    if (cm.getCodeName().contains("早")) {
                        ToggleButton dayOrNightTb =PicklingFinishedActivity.this.dayOrNightTb;
                        dayOrNightTb.setTextOn(cm.getCodeNo()+ "-" + cm.getCodeName());
                    }
                    if (cm.getCodeName().contains("晚")) {
                        ToggleButton dayOrNightTb =PicklingFinishedActivity.this.dayOrNightTb;
                        dayOrNightTb.setTextOff(cm.getCodeNo()+ "-" + cm.getCodeName());
                    }

                }

                PicklingFinishedActivity.this.dayOrNightTb.setChecked(true);
                PicklingFinishedActivity.this.passsOrNgTb.setChecked(true);
            }
        };

        task.execute();

        this.updateButtonsStatus(Kind.PicklingFinished);
    }

    // 開始掃描 zxing
    public void scanBarcode(View v) {
        IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        scanIntegrator.setPrompt("請掃描...");
        scanIntegrator.initiateScan();

    }

    // 掃描結果
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            if(scanningResult.getContents() != null) {
                String scanContent = scanningResult.getContents();
                if (!scanContent.equals("")) {
                    //Toast.makeText(getApplicationContext(),"掃描內容: "+scanContent.toString(), Toast.LENGTH_LONG).show();
                    View currentFocus = this.getCurrentFocus();
                    if (currentFocus instanceof EditText) {
                        EditText et = (EditText) currentFocus;
                        et.setText(scanContent.toString());
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
            Toast.makeText(getApplicationContext(),"發生錯誤",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Insert data
     */
    private void insertData() {
        String barcode = this.barcodeEt.getText()
                .toString()
                .replaceAll("\r", "")
                .replaceAll("\n", "")
                .trim();

        AsyncTask<Void, Void, Boolean> insertMainDataTask = new AsyncTask<Void, Void, Boolean>() {
            EditText barcodeEt;
            TextView barcodePrev;
            MainData mainData;
            String message;
            boolean result = false;

            @Override
            protected Boolean doInBackground(Void... params) {
                // barcode 存在檢查
                //if (myDao.isExistMainData(Kind.PicklingFinished, barcode)) {
                //    message = barcode + " - 已重複掃入! ";
                //    return false;
                //}
                // ------------------------------------------
                // online post, if 網路 connected
                showProgressBar(true);
                if (PicklingFinishedActivity.this.myApplication.isConnected()) {
                    try {
                        mainData = PicklingFinishedActivity.this.createMainData();
                        ApiResponse<Integer> apiResponse =
                                Ion.with(PicklingFinishedActivity.this)
                                        .load("POST", MyInfo.JH_API_URL + "insertData")
                                        .setJsonPojoBody(mainData)
                                        .as(new TypeToken<ApiResponse<Integer>>() { })
                                        .get(15, TimeUnit.SECONDS);

                        result = apiResponse.data.intValue() == 1;
                        message = result ? getString(R.string.post_data_ok) : getString(R.string.post_data_error);
                    } catch (Exception ex) {
                        mainData = PicklingFinishedActivity.this.createMainData();      // 新增到 SQLite
                        result = myDao.insertMainData(mainData);
                        message = result ? getString(R.string.insert_data_ok) : getString(R.string.insert_data_error);
                    }
                } else {
                    mainData = PicklingFinishedActivity.this.createMainData();          // 新增到 SQLite
                    result = myDao.insertMainData(mainData);
                    message = result ? getString(R.string.insert_data_ok) : getString(R.string.insert_data_error);
                }

                return result;

            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                if (success) {
                    PicklingFinishedActivity.this.myMediaPlay.play(MP3.sweet);
                    showToast(message);

                    PicklingFinishedActivity.this.dayOrNightTb.setChecked(true);
                    PicklingFinishedActivity.this.passsOrNgTb.setChecked(true);

                    String barcode = PicklingFinishedActivity.this.barcodeEt.getText().toString();
                    PicklingFinishedActivity.this.barcodePrev.setText(barcode);
                    PicklingFinishedActivity.this.barcodeEt.setText("");

                    PicklingFinishedActivity.this.barcodeEt.requestFocus();

                } else {
                    PicklingFinishedActivity.this.myMediaPlay.play(MP3.beepError);
                    showToast(message);
                }

                showProgressBar(false);
                updateButtonsStatus(Kind.PicklingFinished);
            }
        };

        insertMainDataTask.execute();
    }

    // 產生  MainData bean
    private MainData createMainData() {
        MainData mainData = new MainData();

        mainData.setProcEmp(this.empNo);
        mainData.setKind(Kind.PicklingFinished.getValue());

        mainData.setLocate("");

        String barcode =  this.barcodeEt.getText().toString()
                .replaceAll("\r", "")
                .replaceAll("\n", "")
                .trim();

        mainData.setBarCode(barcode);

        mainData.setScwJobNo("");
        mainData.setItemNo(0);
        mainData.setIsrtType("");
        mainData.setReasonCode("");

        // 早晚班
        String classNo = "";
        if (this.dayOrNightTb.isChecked()) {
            classNo = this.dayOrNightTb.getTextOn().toString().split(("-"))[0];
        } else {
            classNo = this.dayOrNightTb.getTextOff().toString().split(("-"))[0];
        }
        mainData.setClassNo(classNo);

        // pass or ng
        String yn = this.passsOrNgTb.isChecked() ? "Y" : "N";
        mainData.setPassYn(yn);

        String scanDate = this.dateFormat.format(new java.util.Date());
        mainData.setScanDate(scanDate);

        return mainData;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem mi = menu.add(0, 0, 0, "返回");
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mi.setVisible(true);

        MenuItem miClearScreen = menu.add(0, 1, 1, "清畫面");
        miClearScreen.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        miClearScreen.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 0) {
            this.finish();
            return true;
        } else if (item.getItemId() == 1) {
            runOnUiThread(() -> {
                this.barcodeEt.getText().clear();
                this.barcodeEt.requestFocus();
            });
            return true;
        } else {
            return super.onOptionsItemSelected(item);
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


    private void showToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
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


    /**
     * 上傳 async task
     */
    public class UploadTask extends AsyncTask<Void, Void, Boolean> {
        private int okRowCount = 0;
        private String errMessage = null;

        @Override
        protected Boolean doInBackground(Void... voids) {
            String filename = DateFormat.format("yyyyMMddHHmmss-sss", new Date()).toString() + ".csv";

            File file = new File(getCacheDir().getPath(), filename);

            StringBuffer sb = null;
            FileWriter fileWriter = null;

            // step 1
            try {
                sb = new StringBuffer();
                fileWriter = new FileWriter(file);

                List<MainData> mainDataList = myDao.getMainDataList(Kind.PicklingFinished);
                if (mainDataList.size() == 0) {
                    this.errMessage = "無資料, 不用上傳.";
                    return false;
                }

                for (MainData mainData : mainDataList) {
                    // david add 6/24 2 lines
                    String barcode = mainData.getBarCode();
                    if (barcode == null || barcode.trim().isEmpty()) continue;

                    sb.append(mainData.getProcEmp()).append(",");
                    sb.append(mainData.getScanDate()).append(",");
                    sb.append(mainData.getKind()).append(",");
                    sb.append(mainData.getBarCode()).append(",");
                    sb.append(mainData.getLocate()).append(",");
                    sb.append(mainData.getScwJobNo()).append(",");
                    sb.append(mainData.getItemNo()).append(",");
                    sb.append(mainData.getIsrtType()).append(",");
                    sb.append(mainData.getReasonCode()).append(",");
                    sb.append(mainData.getClassNo()).append(",");
                    sb.append(mainData.getPassYn());
                    sb.append("\n");
                }

                fileWriter = new FileWriter(file);
                fileWriter.append(sb.toString());

            } catch (Exception ex) {
                this.errMessage = ex.getMessage();
                return false;

            } finally {
                try {
                    fileWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // step 2
            ApiResponse<Integer> apiResponse = null;
            try {
                apiResponse = Ion.with(PicklingFinishedActivity.this)
                        .load(MyInfo.JH_API_URL + "upload")
                        .uploadProgressBar(progressBar)
                        .setMultipartFile("file", "application/csv", file)
                        .as(new TypeToken<ApiResponse<Integer>>() {
                        })
                        .get();

            } catch (InterruptedException e) {
                this.errMessage = "網路發生錯誤";
                return false;
            } catch (Exception e) {
                this.errMessage = e.getMessage();
                return false;
            }

            // step 3
            if (apiResponse.status == ApiResponse.Status.ERROR) {
                this.errMessage = apiResponse.error.desc;
                return false;
            }

            // step 4
            // 處理筆數
            try {
                if (apiResponse.data == null) {
                    throw new IllegalArgumentException("處理筆數錯誤, call stit.");
                }
                okRowCount = apiResponse.data;
            } catch (NumberFormatException e) {
                this.errMessage = e.getMessage();
                return false;
            } catch (Exception e) {
                this.errMessage = e.getMessage();
                return false;
            }

            // step 5
            try {
                boolean isOk = myDao.deleteMainData(Kind.PicklingFinished);
                if (!isOk) {
                    this.errMessage = "刪除表格失敗";
                    return false;
                }
            } catch (Exception ex) {
                this.errMessage = ex.getMessage();
                return false;
            }

            // ---------------------
            try {
                file.delete();
            } catch (Exception ex) {
                this.errMessage = "檔案刪除失敗!";
                return false;
            }

            showProgressBar(false);

            return true;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            showProgressBar(false);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values.length);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            showProgressBar(false);

            if (success) {
                String msg = okRowCount + " 筆資料, " + getString(R.string.upload_success);
                PicklingFinishedActivity.this.showAlert(msg);
                PicklingFinishedActivity.this.myMediaPlay.play(MP3.sweet);
            } else {
                PicklingFinishedActivity.this.myMediaPlay.play(MP3.beepError);
                showAlert(this.errMessage);
                //beep();
            }

            updateButtonsStatus(Kind.PicklingFinished);

        }
    }  // end upload task

    // for upload button
    private void updateButtonsStatus (Kind kind) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... views) {
                boolean existed = myDao.isExistMainData(kind);
                return existed;
            }

            @Override
            protected void onPostExecute(Boolean existed) {
                super.onPostExecute(existed);

                btnUpload.setEnabled(existed);
            }

        };

        task.execute();
    }


} // class
