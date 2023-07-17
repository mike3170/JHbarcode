package com.stit.jhbarcode.adapter;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.stit.jhbarcode.R;

public class PlatingReceptionListItemHolder extends RecyclerView.ViewHolder {
    TextView tv1;
    TextView tv2;

    Button btnDelete;

    public PlatingReceptionListItemHolder(View view) {
        super(view);
        tv1 = (TextView) view.findViewById(R.id.tv1);
        tv2 = (TextView) view.findViewById(R.id.tv2);

        btnDelete = (Button) view.findViewById(R.id.btnDelete);
    }

}