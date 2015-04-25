package com.gabmus.co2photoeditor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class CustomAboutAdapter extends BaseAdapter{
    String [] titles;
    String [] contents;
    Context context;
    private static LayoutInflater inflater=null;
    public CustomAboutAdapter(AboutActivity mainActivity, String[] prgmTitlesList, String[] prgmContentsList) {
        titles=prgmTitlesList;
        contents=prgmContentsList;
        context=mainActivity;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder
    {
        TextView tTv;
        TextView cTv;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.about_list_item, null);
        holder.tTv=(TextView) rowView.findViewById(R.id.aboutLabelBig);
        holder.cTv=(TextView) rowView.findViewById(R.id.aboutLabelSmall);
        holder.tTv.setText(titles[position]);
        holder.cTv.setText(contents[position]);
        return rowView;
    }

}