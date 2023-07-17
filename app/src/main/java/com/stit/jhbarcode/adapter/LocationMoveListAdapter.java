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

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.ion.Ion;
import com.stit.jhbarcode.MyInfo;
import com.stit.jhbarcode.MyMediaPlay;
import com.stit.jhbarcode.R;
import com.stit.jhbarcode.model.ApiResponse;
import com.stit.jhbarcode.model.DataSourceKind;
import com.stit.jhbarcode.model.Kind;
import com.stit.jhbarcode.model.MP3;
import com.stit.jhbarcode.model.MainData;
import com.stit.jhbarcode.repo.DbHelper;
import com.stit.jhbarcode.repo.MyDao;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 線材移轉
 */
public class LocationMoveListAdapter  extends RecyclerView.Adapter<LocationMoveListItemHolder> {
    private List<MainData> mainDataList;
    private Activity activity;

    private MyMediaPlay myMediaPlay;
    private MyDao myDao;
    private SQLiteDatabase db;

    private TextView tvQueryCount;

    /** local or remote */
    private DataSourceKind dsKind;

    private SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd");

    /**
     *
     * @param activity
     * @param mainDataList
     * @param dsKind        local or remove
     * @param tvQueryCount
     */
    public LocationMoveListAdapter(Activity activity, List<MainData> mainDataList, DataSourceKind dsKind, TextView tvQueryCount) {
        this.activity = activity;
        this.mainDataList = mainDataList;
        this.dsKind = dsKind;
        this.tvQueryCount = tvQueryCount;

        this.db = DbHelper.getInstance(activity).getDb();
        this.myDao = new MyDao(activity);
        this.myMediaPlay = new MyMediaPlay(activity);
    }

    @Override
    public LocationMoveListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.location_move_list_item, parent, false);

        return new LocationMoveListItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(LocationMoveListItemHolder holder, int position) {
        MainData mainData = mainDataList.get(position);

        holder.tv1.setText(mainData.getBarCode());

        if (dsKind == DataSourceKind.REMOATE) {
            java.util.Date _date = new java.util.Date(Long.valueOf(mainData.getScanDate()));
            holder.tv2.setText(dateFmt.format(_date));

        } else {
            String yyyy = mainData.getScanDate().substring(0, 4);
            String mm = mainData.getScanDate().substring(4, 6);
            String dd = mainData.getScanDate().substring(6, 8);
            holder.tv2.setText(yyyy+ "-" + mm + "-" + dd);
        }

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
                MainData mainData = LocationMoveListAdapter.this.mainDataList.get(position);
                success = myDao.deleteMainDataById(mainData.getId());
                return  success;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    LocationMoveListAdapter.this.mainDataList.remove(position);
                    notifyItemRemoved(position);

                    int size = mainDataList.size();
                    LocationMoveListAdapter.this.tvQueryCount.setText(size + ""); // update query count

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
