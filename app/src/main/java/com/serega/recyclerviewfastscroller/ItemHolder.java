package com.serega.recyclerviewfastscroller;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @author S.A.Bobrischev
 *         Developed by Magora Team (magora-systems.com). 2017.
 */
public class ItemHolder extends RecyclerView.ViewHolder {
    public TextView text;

    private ItemHolder(View itemView) {
        super(itemView);
        text = (TextView) itemView.findViewById(R.id.text);
    }

    public static ItemHolder createHolder(ViewGroup parent) {
        return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.holder, parent, false));
    }
}
