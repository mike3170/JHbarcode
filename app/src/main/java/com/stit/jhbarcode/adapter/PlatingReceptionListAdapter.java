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
import com.stit.jhbarcode.model.MP3;
import com.stit.jhbarcode.model.MainData;
import com.stit.jhbarcode.repo.DbHelper;
import com.stit.jhbarcode.repo.MyDao;

import java.util.List;

/**
 * 電鍍接收
 *
 */
public class PlatingReceptionListAdapter extends RecyclerView.Adapter<PlatingReceptionListItemHolder> {
    private List<MainData> mainDataList;
    private Activity activity;

    private MyMediaPlay myMediaPlay;
    private MyDao myDao;
    private SQLiteDatabase db;

    private TextView tvQueryCount;

    /** local or remote */
    private DataSourceKind dsKind;

    public PlatingReceptionListAdapter(Activity activity, List<MainData> mainDataList, DataSourceKind dsKind, TextView tvQueryCount) {
        this.activity = activity;
        this.mainDataList = mainDataList;

        this.dsKind = dsKind;
        this.tvQueryCount = tvQueryCount;

        this.db = DbHelper.getInstance(activity).getDb();
        this.myDao = new MyDao(activity);
        this.myMediaPlay = new MyMediaPlay(activity);
    }

    @Override
    public PlatingReceptionListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.plating_reception_list_item, parent, false);

        return new PlatingReceptionListItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PlatingReceptionListItemHolder holder, int position) {
        MainData mainData = mainDataList.get(position);
        //System.out.println(mainData.toString());

        holder.tv1.setText(mainData.getLocate());
        holder.tv2.setText(mainData.getScwJobNo());

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
                MainData mainData = PlatingReceptionListAdapter.this.mainDataList.get(position);

                success = myDao.deleteMainDataById(mainData.getId());
                return  success;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    PlatingReceptionListAdapter.this.mainDataList.remove(position);
                    notifyItemRemoved(position);

                    int size = mainDataList.size();
                    PlatingReceptionListAdapter.this.tvQueryCount.setText(size + ""); // update query count

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


}
