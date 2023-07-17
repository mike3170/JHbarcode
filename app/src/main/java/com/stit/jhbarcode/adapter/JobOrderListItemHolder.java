package com.stit.jhbarcode.adapter;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.stit.jhbarcode.R;

public class JobOrderListItemHolder extends RecyclerView.ViewHolder {
    TextView tv1;
    TextView tv2;
    TextView tv3;
    Button btnDelete;

    public JobOrderListItemHolder(View view) {
        super(view);
        tv1 = (TextView) view.findViewById(R.id.barcode);
        tv2 = (TextView) view.findViewById(R.id.jobNo);
        tv3 = (TextView) view.findViewById(R.id.itemNo);
        btnDelete = (Button) view.findViewById(R.id.btnDelete);

    }

}