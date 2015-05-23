package com.gabmus.co2photoeditor;

import android.util.Log;

/**
 * Created by gabmus on 19/04/15.
 */
public class FXData {

    public static final String DEBUG_PREFIX="Data Values";

    public final String name;
    public final int icon;
    public final int parCount;
    public int [] parValues;
    public int [] parValuesDefault;
    public final String [] parNames;
    public boolean fxActive;

    public FXData (String name_, int icon_, int parCount_, int[] parValues_, String [] parNames_) {
        name=name_; icon=icon_;
        parCount=parCount_;
        parNames=parNames_;
        fxActive = false;
        parValues=parValues_.clone();
        parValuesDefault=parValues_.clone();
    }

    public void printVals() {
        if (!fxActive) return;

        Log.d(DEBUG_PREFIX, "Filter: "+name);
        for (int i = 0; i < parCount; i++) {
            Log.d(DEBUG_PREFIX, "      "+i+") "+parNames[i]+" "+parValues[i]);
        }
    }
}
