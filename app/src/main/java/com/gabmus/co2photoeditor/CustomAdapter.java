package com.gabmus.co2photoeditor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class CustomAdapter extends BaseAdapter{
    String [] result;
    Context context;
    int [] imageId;
    TextView titleTv;
    private static LayoutInflater inflater=null;
    public CustomAdapter(MainActivity mainActivity, String[] prgmNameList, int[] prgmImages) {
        // TODO Auto-generated constructor stub
        titleTv=(TextView) mainActivity.findViewById(R.id.textViewEffectTitle);
        result=prgmNameList;
        context=mainActivity;
        imageId=prgmImages;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return result.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView tv;
        ImageView img;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.effect_list_item, null);
        holder.tv=(TextView) rowView.findViewById(R.id.textViewListItem);
        holder.img=(ImageView) rowView.findViewById(R.id.imageViewListItem);
        holder.tv.setText(result[position]);
        holder.img.setImageResource(imageId[position]);


        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.FXselected=position;
                MainActivity.FX.SelectFX(position);


                titleTv.setText(result[position]);
            }
        });
        return rowView;
    }

}