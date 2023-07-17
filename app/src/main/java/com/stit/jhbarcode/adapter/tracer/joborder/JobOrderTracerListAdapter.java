package com.stit.jhbarcode.adapter.tracer.joborder;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.stit.jhbarcode.R;
import com.stit.jhbarcode.model.ApcJobOrde;

import java.util.List;

/**
 * 工令領用-查單
 *
 *
 */
public class JobOrderTracerListAdapter extends RecyclerView.Adapter<ListItemHolder> {
    private List<ApcJobOrde> jobOrdesList;
    private Activity activity;

    public JobOrderTracerListAdapter(Activity activity, List<ApcJobOrde> jobOrdesList) {
        this.activity = activity;
        this.jobOrdesList = jobOrdesList;
    }

    @Override
    public ListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.job_order_tracer_card, parent, false);

        return new ListItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ListItemHolder holder, int position) {
        ApcJobOrde jobOrde = jobOrdesList.get(position);
        String assmNo = jobOrde.getAssmNo();
        String assmName = jobOrde.getAssmName();

        holder.itemNo.setText(String.valueOf(jobOrde.getItemNo()));

        // System.out.println(jobOrde.getItemNo().toString());

        holder.wirKind.setText(jobOrde.getWirKind());
        holder.luoNo.setText(jobOrde.getLuoNo());

        holder.assmName.setText(assmNo + "-" + assmName);
        holder.drawDia.setText(jobOrde.getDrawDia().toString());
        holder.requQty.setText(jobOrde.getRequQty().toString());
        holder.issuQty.setText(jobOrde.getIssuQty().toString());
        holder.fnshQty.setText(jobOrde.getFnshQty().toString());

        if (position % 2 == 0) {
            CardView cardView = (CardView) holder.itemView;
            cardView.setCardBackgroundColor(activity.getResources().getColor(R.color.cardViewBackground));
        }

    }

    @Override
    public int getItemCount() {
        return jobOrdesList.size();
    }
}
