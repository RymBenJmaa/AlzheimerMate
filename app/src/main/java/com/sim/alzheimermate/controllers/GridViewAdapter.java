package com.sim.alzheimermate.controllers;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sim.alzheimermate.Models.MembreFamille;
import com.sim.alzheimermate.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Rym on 23/12/2017.
 */

public class GridViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;


    public GridViewAdapter(Context context, int layoutResourceId, List<MembreFamille> array) {
        super(context, layoutResourceId, array);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        MembreFamille per = (MembreFamille) getItem(position);
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = row.findViewById(R.id.nom_prenom);
            holder.image = row.findViewById(R.id.imageG);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        holder.imageTitle.setText(per.getNom() + " " + per.getPrenom());
        Picasso.with(context).load(per.getImage_per()).into(holder.image);
        return row;
    }

    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }

}
