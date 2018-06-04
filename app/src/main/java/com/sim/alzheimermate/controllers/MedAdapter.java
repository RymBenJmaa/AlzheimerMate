package com.sim.alzheimermate.controllers;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sim.alzheimermate.Models.Medicament;
import com.sim.alzheimermate.R;
import com.sim.alzheimermate.Utils.CustomImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Rym on 24/12/2017.
 */

public class MedAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;


    public MedAdapter(Context context, int layoutResourceId, List<Medicament> array) {
        super(context, layoutResourceId, array);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        Medicament med = (Medicament) getItem(position);
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = row.findViewById(R.id.nom_med_grid);
            holder.image = row.findViewById(R.id.image_med_G);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        assert med != null;
        String str = med.getNom() + " " + med.getNbPrises() + "/jour";
        holder.imageTitle.setText(str);
        Picasso.with(context).load(med.getImage_med()).into(holder.image);
        return row;
    }

    static class ViewHolder {
        TextView imageTitle;
        CustomImageView image;
    }

}
