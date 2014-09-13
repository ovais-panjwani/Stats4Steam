package com.panjwani.ovais.steamstatistics;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;

/**
 * Created by Mohammed on 8/21/2014.
 */
public class Game implements Comparable<Game>{
    public static String COMPARE_ON = "Title";

    public String title = "Portal 2";
    public double actualAmount = 19.99;
    public double paidAmount = 0.50;
    public double hoursPlayed = 10.0;
    public Bitmap icon;

    public void setTitle(String newTitle){
        title = newTitle;
    }

    public void setActualAmount(double newActualAmount) {
        actualAmount = newActualAmount;
    }

    public void setPaidAmount(double newPaidAmount) {
        paidAmount = newPaidAmount;
    }

    public void setHoursPlayed(double newHoursPlayed) {
        hoursPlayed = newHoursPlayed;
    }

    public double getHoursPerDollar() {
        if(paidAmount != 0.0) {
            return hoursPlayed / paidAmount;
        } else{
            return 0.0;
        }
    }

    public int getSavings() {
        if(actualAmount != 0.0) {
            return 100 - (int) (paidAmount * 100 / actualAmount);
        } else{
            return 100;
        }
    }

    public void makeIcon(String iconUrl){
        try {
            URL url = new URL(iconUrl);
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            icon = Bitmap.createScaledBitmap(bmp, 250, 250, false);
        } catch (IOException e){
            try {
                URL url = new URL("http://playcrea.com/images/icon_steam.png");
                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                icon = Bitmap.createScaledBitmap(bmp, 250, 250, false);
            } catch (IOException err){
                Log.d("SomehowBadUrl", err.toString());
            }
        }
    }
    public int compareTo(Game g1, Game g2) {
        if (COMPARE_ON.equals("Title")) {
            return g1.title.compareTo(g2.title);
        } else if (COMPARE_ON.equals("Hours Played")) {
            return Double.valueOf(g1.hoursPlayed).compareTo(Double.valueOf(g2.hoursPlayed));
        } else if (COMPARE_ON.equals("Paid Price")) {
            return Double.valueOf(g1.paidAmount).compareTo(Double.valueOf(g2.paidAmount));
        } else if (COMPARE_ON.equals("Store Price")) {
            return Double.valueOf(g1.actualAmount).compareTo(Double.valueOf(g2.actualAmount));
        } else if (COMPARE_ON.equals("Hours/Dollar")) {
            return Double.valueOf(g1.getHoursPerDollar()).compareTo(Double.valueOf(g2.getHoursPerDollar()));
        } else if (COMPARE_ON.equals("Savings")) {
            return Double.valueOf(g1.getSavings()).compareTo(Double.valueOf(g2.getSavings()));
        }
        return 0;
    }

    @Override
    public int compareTo(Game other){
        return compareTo(this, other);
    }
}
