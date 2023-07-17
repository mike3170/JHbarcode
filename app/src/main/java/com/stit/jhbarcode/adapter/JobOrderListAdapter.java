package com.stit.jhbarcode.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.stit.jhbarcode.MyMediaPlay;
import com.stit.jhbarcode.R;
import com.stit.jhbarcode.model.DataSourceKind;
import com.stit.jhbarcode.model.Kind;
import com.stit.jhbarcode.model.MP3;
import com.stit.jhbarcode.model.MainData;
import com.stit.jhbarcode.repo.DbHelper;
import com.stit.jhbarcode.repo.MyDao;

import java.util.List;

/**
 * 工令領用
 *
 */
public class JobOrderListAdapter extends RecyclerView.Adapter<JobOrderListItemHolder> {
    private List<MainData> mainDataList;
    private Activity activity;

    private MyMediaPlay myMediaPlay;
    private MyDao myDao;
    private SQLiteDatabase db;

    private TextView tvQueryCount;

    /** local or remote */
    private DataSourceKind dsKind;

    /**
     *
     * @param activity
     * @param mainDataList
     * @param dsKind
     * @param tvQueryCount  queryCount, because of delete
     */
    public JobOrderListAdapter(Activity activity, List<MainData> mainDataList, DataSourceKind dsKind, TextView tvQueryCount) {
        this.activity = activity;
        this.mainDataList = mainDataList;
        this.dsKind = dsKind;
        this.tvQueryCount = tvQueryCount;

        // System.out.println(this.dsKind.toString());
        //for (MainData d : this.mainDataList) {
        //    System.out.println(d.getScanDate());
        //}

        this.db = DbHelper.getInstance(activity).getDb();
        this.myDao = new MyDao(activity);
        this.myMediaPlay = new MyMediaPlay(activity);
    }

    @Override
    public JobOrderListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView =
                LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.job_order_list_item, parent, false);

        return new JobOrderListItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(JobOrderListItemHolder holder, int position) {
        MainData mainData = mainDataList.get(position);

        holder.tv1.setText(mainData.getBarCode());
        holder.tv2.setText(mainData.getScwJobNo());
        holder.tv3.setText(mainData.getItemNo().toString());

        // 以上傳不刪除
        if (dsKind == DataSourceKind.REMOATE) {
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            holder.btnDelete.setOnClickListener(v -> {
                this.deleteItem(holder.getAdapterPosition());
            });
        }

    }

    @Override
    public int getItemCount() {
        return mainDataList.size();
    }

    private void deleteItem(int position) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            boolean success = false;
            String message = null;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                MainData mainData = JobOrderListAdapter.this.mainDataList.get(position);

                success = myDao.deleteMainDataById(mainData.getId());
                return  success;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    JobOrderListAdapter.this.mainDataList.remove(position);
                    notifyItemRemoved(position);

                    int size = mainDataList.size();
                    JobOrderListAdapter.this.tvQueryCount.setText(size + ""); // update query count

                    showToast("刪除成功");
                    myMediaPlay.play(MP3.sweet);
                } else {
                    showAlert(message);
                    myMediaPlay.play(MP3.beepError);
                }
            }

        };

        task.execute();

    }


    private void showToast(String msg) {
        Toast toast = Toast.makeText(activity, msg, Toast.LENGTH_SHORT);
        LinearLayout toastLayout = (LinearLayout) toast.getView();
        TextView toastTV = (TextView) toastLayout.getChildAt(0);
        toastTV.setTextSize(20);
        toast.show();
    }

    private void showAlert(String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle(message)
                .setPositiveButton("關閉視窗", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();

        alertDialog.show();
    }

} // class
