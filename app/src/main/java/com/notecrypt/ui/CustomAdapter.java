package com.notecrypt.ui;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;


class CustomAdapter extends SimpleAdapter {

    CustomAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
    }

    @Override
    public void setViewImage(ImageView v, String value) {
        if (value.equals("false")) {
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }
}