package com.stit.jhbarcode;

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

import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.koushikdutta.ion.Ion;
import com.stit.jhbarcode.model.ApiResponse;
import com.stit.jhbarcode.model.Kind;
import com.stit.jhbarcode.model.MP3;
import com.stit.jhbarcode.model.MainData;
import com.stit.jhbarcode.repo.DbHelper;
import com.stit.jhbarcode.repo.MyDao;
import com.stit.jhbarcode.tracer.JobOrderTracerActivity;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 工令領用
 */
public class JobOrderPickupActivity extends AppCompatActivity {
    private SharedPreferences mSPerf;
    MyMediaPlay  myMediaPlay;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private SimpleDateFormat dateFormatDash = new SimpleDateFormat("yyyy-MM-dd");

    private MyDao myDao;
    private SQLiteDatabase db;
    private MyApplication myApplication;

    private String empNo;
    private TextView empNoTv;
    private EditText jobOrderEt;

    // 2020-03-07 移除
    // private EditText itemNoEt;

    private EditText barcodeEt;
    private TextView barcodePrev;
    private ProgressBar progressBar;

    private Button btnScan;
    private Button btnAdd;
    private Button btnUpload;
    private Button btnQuery;
    private Button btnTracer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_order_pickup);
        // -------------------------------------------------
        this.myMediaPlay = new MyMediaPlay(this);
        this.myApplication = (MyApplication) this.getApplication();

        this.db = DbHelper.getInstance(this).getDb(); this.myDao = new MyDao(this);
        this.mSPerf = this.getSharedPreferences(MyInfo.SPKey, Context.MODE_PRIVATE);

        this.empNo =  this.mSPerf.getString("name", "na");
        this.empNoTv = (TextView) this.findViewById(R.id.empNo);
        this.empNoTv.setText(this.empNo);

        // ---------------------------
        this.progressBar = this.findViewById(R.id.progressBar);
        this.progressBar.setVisibility(View.INVISIBLE);

        this.jobOrderEt = this.findViewById(R.id.jobOrder);
        // this.itemNoEt = this.findViewById(R.id.itemNo);
        this.barcodeEt = this.findViewById(R.id.barcode);

        this.barcodePrev = this.findViewById(R.id.barcodePrev);

        this.btnScan = this.findViewById(R.id.btnScan);
        this.btnScan.setOnClickListener(this::scanBarcode);

        this.btnQuery = this.findViewById(R.id.btnQuery);
        this.btnTracer = this.findViewById(R.id.btnTracer);

        this.btnAdd = this.findViewById(R.id.btnAdd);
        this.btnAdd.setOnClickListener((view) -> {
            if (! this.isDataValid()) {
                return;
            }
            this.insertData();
        });

        //------------------------
        //this.jobOrderEt.setOnFocusChangeListener((v, hasFocus) -> {
        //    if (!hasFocus) {
        //        v.requestFocus();
        //    }
        //});

        this.barcodeEt.setOnKeyListener((view, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() != KeyEvent.ACTION_UP) {
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
            Intent intent = new Intent(JobOrderPickupActivity.this, JobOrderPickupQueryActivity.class);
            startActivity(intent);
        });


        // 查單
        this.btnTracer.setOnClickListener(v -> {
            Intent intent = new Intent(this, JobOrderTracerActivity.class);
            this.startActivity(intent);
        });

        // --------------------------------------
        // 上傳
        this.btnUpload = this.findViewById(R.id.btnUpload);
        this.btnUpload.setOnClickListener(view -> {
            //this.showToast("not yet");
            if (! this.myApplication.isConnected()) {
                Toast.makeText(this, getString(R.string.check_network), Toast.LENGTH_SHORT).show();
                return;
            }

            showProgressBar(true);
            new JobOrderPickupActivity.UploadTask().execute();
        });

    }  // onCreate

    @Override
    protected void onResume() {
        super.onResume();
        this.updateButtonsStatus(Kind.JobOrderPickup);
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
                    //Toast.makeText(getApplicationContext(),"掃描內容: "+scanContent.toString(), Toast.LENGTH_SHORT).show();
                    View currentFocus = this.getCurrentFocus();
                    if (currentFocus instanceof EditText) {
                        EditText et = (EditText) currentFocus;
                        et.setText(scanContent.toString());
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
            Toast.makeText(getApplicationContext(),"發生錯誤",Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isDataValid() {
        //System.out.println("isDataInvalid");
        String jobOrder = this.jobOrderEt.getText().toString()
                .replaceAll("\r", "")
                .replaceAll("\n", "")
                .trim();

        if (jobOrder == null || TextUtils.isEmpty(jobOrder.trim())) {
            this.showToast("工令空白!");
            this.myMediaPlay.play(MP3.beepError);
            return false;
        }

        if (jobOrder.contains(",")) {
            String msg = "工令中含有 \",\" 字元.";
            this.myMediaPlay.play(MP3.beepError);
            return false;
        }

        // -----------------------------------------------
        // String itemNo = this.itemNoEt.getText().toString();
        // if ( itemNo == null || TextUtils.isEmpty(itemNo.trim())) {
        //     this.myMediaPlay.play(MP3.beepError);
        //     this.showToast("項次空白!");
        //     return false;
        // }

        // -------------------------------------------------
        String barcode = this.barcodeEt.getText().toString()
                .replaceAll("\r", "")
                .replaceAll("\n", "")
                .trim();
        if (barcode == null || TextUtils.isEmpty(barcode.trim())) {
            this.myMediaPlay.play(MP3.beepError);
            this.showToast("條碼空白!");
            return false;
        }

        if (barcode.contains(",")) {
            String msg = "條碼中含有 \",\" 字元.";
            this.myMediaPlay.play(MP3.beepError);
            showToast(msg);
            return false;
        }

        return true;
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

        AsyncTask<View, Void, Boolean> insertMainDataTask = new AsyncTask<View, Void, Boolean>() {
            EditText barcodeEt;
            TextView barcodePrev;
            MainData mainData;
            String message;

            String jobNo;
            int itemNo = 0;
            String coilNo;
            String procDate;
            String procEmp;
            boolean isCheckOk = false;


            boolean result = false;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                this.jobNo = JobOrderPickupActivity.this.jobOrderEt.getText().toString()
                        .replaceAll("\r", "")
                        .replaceAll("\n", "")
                        .trim();

                // this.itemNo = Integer.valueOf(JobOrderPickupActivity.this.itemNoEt.getText().toString().trim());

                this.coilNo = JobOrderPickupActivity.this.barcodeEt.getText().toString()
                        .replaceAll("\r", "")
                        .replaceAll("\n", "")
                        .trim();

                java.util.Date _date = new java.util.Date();
                this.procDate = dateFormatDash.format(_date);

                this.procEmp = JobOrderPickupActivity.this.empNo.trim();
            }

            @Override
            protected Boolean doInBackground(View... views) {
                this.barcodeEt = (EditText) views[0];
                this.barcodePrev = (TextView) views[1];

                // barcode 存在檢查
                //if (myDao.isExistMainData(Kind.JobOrderPickup, barcode)) {
                //    JobOrderPickupActivity.this.myMediaPlay.play(MP3.beepError);
                //    message = barcode + " - 條碼已重複掃入! ";
                //    return false;
                //}

                // online post, if 網路 connected
                showProgressBar(true);

                if (JobOrderPickupActivity.this.myApplication.isConnected()) {
                    try {
                        // itemNo 移除, 2020-03-07
                        //String url = MyInfo.JH_API_URL + "jobOrder/check" +
                        //        "?jobNo=" + this.jobNo +
                        //        "&itemNo=" + this.itemNo +
                        //        "&procDate=" + this.procDate +
                        //        "&coilNo=" +  this.coilNo +
                        //        "&procEmp=" + this.procEmp;

                        // 工令領用線上檢查
                        String url = MyInfo.JH_API_URL + "jobOrder/check" +
                                "?jobNo=" + this.jobNo +
                                "&procDate=" + this.procDate +
                                "&coilNo=" +  this.coilNo +
                                "&procEmp=" + this.procEmp;

                        url = url.replaceAll(" ", "%20");

                        ApiResponse<String> checkResponse =
                                Ion.with(JobOrderPickupActivity.this)
                                        .load("GET", url)
                                        .as(new TypeToken<ApiResponse<String>>() {
                                        })
                                        .get();

                        //System.out.println(checkResponse.status.toString());
                        //System.out.println(checkResponse.data + "-mia");
                        //System.out.println(checkResponse.error.desc);

                        if (checkResponse.status == ApiResponse.Status.ERROR) {
                            this.isCheckOk = false;
                            this.message = checkResponse.error.desc;
                            return false;
                        } else {
                            this.isCheckOk = true;
                            this.message = "";
                        }

                        // -------------------------------------------------------------
                        mainData = JobOrderPickupActivity.this.createMainData();
                        ApiResponse<String> apiResponse =
                                Ion.with(JobOrderPickupActivity.this)
                                        .load("POST", MyInfo.JH_API_URL + "insertData")
                                        .setJsonPojoBody(mainData)
                                        .as(new TypeToken<ApiResponse<String>>() {
                                        })
                                        .get(15, TimeUnit.SECONDS);

                        // System.out.println(apiResponse.toString());
                        result = apiResponse.status == ApiResponse.Status.OK;
                        message = result ? getString(R.string.post_data_ok) : getString(R.string.post_data_error);

                    } catch (InterruptedException | ExecutionException ieEx) {  // timout occur there
                        // timeout then insert into local SQLite
                        mainData = JobOrderPickupActivity.this.createMainData();  // 新增到 SQLite
                        result = myDao.insertMainData(mainData);
                        message = result ? getString(R.string.insert_data_ok) : getString(R.string.insert_data_error);

                    } catch (Exception ex) {
                        this.isCheckOk = false;
                        this.message = (ex != null) ? ex.getMessage() : getString(R.string.call_administrator);
                        return false;
                    }
                } else {
                    mainData = JobOrderPickupActivity.this.createMainData();  // 新增到 SQLite
                    result = myDao.insertMainData(mainData);
                    message = result ? getString(R.string.insert_data_ok) : getString(R.string.insert_data_error);
                }

                return result;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);

                // check not pass
                if (! this.isCheckOk && ! success) {
                    JobOrderPickupActivity.this.jobOrderEt.setText("");
                    // JobOrderPickupActivity.this.itemNoEt.setText("");
                    JobOrderPickupActivity.this.barcodeEt.setText("");

                    showAlert(message);
                    showProgressBar(false);
                    JobOrderPickupActivity.this.myMediaPlay.play(MP3.beepError);

                    JobOrderPickupActivity.this.jobOrderEt.requestFocus();
                    return;
                }

                if (success) {
                    showToast(message);
                    JobOrderPickupActivity.this.myMediaPlay.play(MP3.sweet);

                    // 保留
                    //JobOrderPickupActivity.this.jobOrderEt.setText("");

                    barcodeEt.setText("");
                    barcodePrev.setText(barcode);

                    // JobOrderPickupActivity.this.itemNoEt.setText("");
                    JobOrderPickupActivity.this.jobOrderEt.requestFocus();

                } else {
                    showToast(message);
                    JobOrderPickupActivity.this.myMediaPlay.play(MP3.beepError);
                }

                showProgressBar(false);
                updateButtonsStatus(Kind.JobOrderPickup);
            }
        };

        insertMainDataTask.execute(this.barcodeEt, this.barcodePrev);
    }

    // 產生  MainData bean
    private MainData createMainData() {
        MainData mainData = new MainData();

        mainData.setProcEmp(this.empNo);
        mainData.setKind(Kind.JobOrderPickup.getValue());

        //---------------
        String scwJobNo =  this.jobOrderEt.getText().toString()
                .replaceAll("\r", "")
                .replaceAll("\n", "")
                .trim();
        mainData.setScwJobNo(scwJobNo);

        //--------------
        // 2020-03-07 移除 itemNo
        //mainData.setItemNo(Integer.valueOf(this.itemNoEt.getText().toString()));
        mainData.setItemNo(0);

        // --------------
        mainData.setBarCode(barcodeEt.getText().toString()
                .replaceAll("\r", "")
                .replaceAll("\n", "")
                .trim());


        mainData.setLocate("");
        mainData.setIsrtType("");
        mainData.setReasonCode("");
        mainData.setClassNo("");
        mainData.setPassYn("");

        String scanDate = this.dateFormat.format(new java.util.Date());
        mainData.setScanDate(scanDate);

        return mainData;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem miBack = menu.add(0, 0, 0, "返回");
        miBack.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        miBack.setVisible(true);

        MenuItem miClearScreen = menu.add(0, 1, 1, "清畫面");
        miClearScreen.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        miClearScreen.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            this.finish();
            return true;
        } else if (item.getItemId() == 1) {
            runOnUiThread(() -> {
                this.jobOrderEt.getText().clear();
                this.barcodeEt.getText().clear();
                this.jobOrderEt.requestFocus();
            });
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

                List<MainData> mainDataList = myDao.getMainDataList(Kind.JobOrderPickup);
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
                apiResponse = Ion.with(JobOrderPickupActivity.this)
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
                this.errMessage = getString(R.string.timeout);
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
                this.errMessage = getString(R.string.timeout);
                return false;
            }

            // step 5
            try {
                boolean isOk = myDao.deleteMainData(Kind.JobOrderPickup);
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
                //mButtonUpdate.setEnabled(false);
                //mBtnScanQuery.setEnabled(false);
                JobOrderPickupActivity.this.myMediaPlay.play(MP3.sweet);
                JobOrderPickupActivity.this.showAlert(msg);
                //beep();
            } else {
                JobOrderPickupActivity.this.myMediaPlay.play(MP3.beepError);
                showAlert(this.errMessage);
                //beep();
            }

            updateButtonsStatus(Kind.JobOrderPickup);
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
