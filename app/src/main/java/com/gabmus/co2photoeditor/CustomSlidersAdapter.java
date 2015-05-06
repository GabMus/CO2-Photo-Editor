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


public class CustomSlidersAdapter extends BaseAdapter{
    String [] titles;
    int [] startingValues;
    Context context;
    private static LayoutInflater inflater=null;
    public CustomSlidersAdapter(Context context_, String[] titles_, int [] startingValues_) {
        titles=titles_.clone();
        startingValues=startingValues_.clone();
        context=context_;
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
        TextView titleTV;
        SeekBar seekBar;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.slider_list_item, null);

        holder.titleTV=(TextView) rowView.findViewById(R.id.sliderLabelGlobal);
        holder.seekBar=(SeekBar) rowView.findViewById(R.id.seekBarGlobal);
        holder.titleTV.setText(titles[position]);
        //holder.seekBar.setProgress(startingValues[position]);
        holder.seekBar.setProgress(MainActivity.FX.FXList[MainActivity.FXselected].parValues[position]);

        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override                                   //i=value, b=by user?
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                MainActivity.FX.FXList[MainActivity.FXselected].parValues[position] = i;
                MainActivity.FX.tuneFX(MainActivity.FXselected, position+1, i, MainActivity.fsv);
                MainActivity.fsv.renderer.Render();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        return rowView;
    }

}