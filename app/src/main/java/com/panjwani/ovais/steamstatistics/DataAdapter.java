package com.panjwani.ovais.steamstatistics;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

/**
 * Created by Mohammed on 8/24/2014.
 */
public class DataAdapter extends BaseAdapter {

    Context mContext;
    private ArrayList<Game> gamesList = new ArrayList<Game>();
    private LayoutInflater mInflater;
    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
    DecimalFormat df = new DecimalFormat();
    public DataAdapter(Context c, ArrayList<Game> listOfGames)
    {
        mContext=c;
        gamesList = listOfGames;
        mInflater = LayoutInflater.from(c);
    }
    public int getCount()
    {
        return gamesList.size();
    }
    public Object getItem(int position)
    {
        return position;
    }
    public long getItemId(int position)
    {
        return position;
    }
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder=null;
        if(convertView==null)
        {
            convertView = mInflater.inflate(R.layout.customgrid,
                    parent,false);
            holder = new ViewHolder();
            holder.title=(TextView)convertView.findViewById(R.id.title);
            holder.title.setPadding(50, 10, 10, 10);
            Typeface custom_font = Typeface.createFromAsset(mContext.getAssets(),
                    "SF Speedwaystar.ttf");
            holder.title.setTypeface(custom_font);
            holder.actualAmount=(TextView)convertView.findViewById(R.id.actualAmount);
            holder.actualAmount.setPadding(50, 7, 10, 7);
            holder.paidAmount=(TextView)convertView.findViewById(R.id.paidAmount);
            holder.paidAmount.setPadding(50, 7, 10, 7);
            holder.hoursPlayed=(TextView)convertView.findViewById(R.id.hoursPlayed);
            holder.hoursPlayed.setPadding(50, 15, 10, 7);
            holder.hoursPerDollar=(TextView)convertView.findViewById(R.id.hoursPerDollar);
            holder.hoursPerDollar.setPadding(50, 7, 10, 15);
            holder.savings=(TextView)convertView.findViewById(R.id.savings);
            holder.savings.setPadding(50, 7, 10, 7);
            holder.icon=(ImageView) convertView.findViewById(R.id.icon);
            holder.icon.setPadding(50, 0, 0, 0);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }
        df.setMaximumFractionDigits(3);
        df.setMinimumFractionDigits(2);
        holder.title.setText(gamesList.get(position).title);
        holder.actualAmount.setText("Store Price: " + currencyFormatter.format(gamesList.get(position).actualAmount));
        holder.paidAmount.setText("Paid Price: " + currencyFormatter.format(gamesList.get(position).paidAmount));
        holder.hoursPlayed.setText("Hours Played: " + Double.toString(gamesList.get(position).hoursPlayed));
        holder.hoursPerDollar.setText("Hours/Dollar: " + df.format(gamesList.get(position).getHoursPerDollar()));
        holder.savings.setText("Savings: " + Integer.toString((int) Math.floor(((gamesList.get(position).getSavings()) +  5/ 2) / 5) * 5) + "%");
        holder.icon.setImageBitmap(gamesList.get(position).icon);
        if(position % 2 == 1){
            convertView.setBackgroundColor(Color.rgb(54, 52, 51));
        } else {
            convertView.setBackgroundColor(Color.rgb(38, 38, 38));
        }
        return convertView;
    }
    static class ViewHolder
    {
        TextView title;
        TextView actualAmount;
        TextView paidAmount;
        TextView hoursPlayed;
        TextView hoursPerDollar;
        TextView savings;
        ImageView icon;
    }
    public void updateSort(){
        Collections.sort(gamesList);
        notifyDataSetChanged();
    }
}
