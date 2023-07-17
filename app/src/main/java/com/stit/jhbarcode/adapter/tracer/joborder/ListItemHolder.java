package com.stit.jhbarcode.adapter.tracer.joborder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.stit.jhbarcode.R;

// job order pick up
// 查單
public class ListItemHolder extends RecyclerView.ViewHolder {
    TextView itemNo;
    TextView wirKind;
    TextView luoNo;
    TextView assmName;
    TextView drawDia;
    TextView requQty;
    TextView issuQty;
    TextView fnshQty;

    public ListItemHolder(View view) {
        super(view);

        itemNo = (TextView) view.findViewById(R.id.itemNo);
        wirKind = (TextView) view.findViewById(R.id.wirKind);
        luoNo = view.findViewById(R.id.luoNo);
        assmName = view.findViewById(R.id.asssName);
        drawDia = (TextView) view.findViewById(R.id.drawDia);
        requQty = (TextView) view.findViewById(R.id.requQty);
        issuQty = (TextView) view.findViewById(R.id.issuQty);
        fnshQty = (TextView) view.findViewById(R.id.fnshQty);

    }

}