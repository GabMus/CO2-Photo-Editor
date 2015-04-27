package com.gabmus.co2photoeditor;

/**
 * Created by gabmus on 19/04/15.
 */
public class FXData {

    public final String name;
    public final int icon;
    public final int parCount;
    public int [] parValues;
    public int [] parValuesDefault;
    public final String [] parNames;
    public boolean fxActive;

    public FXData (String name_, int icon_, int parCount_, int[] parValues_, String [] parNames_) {
        name=name_; icon=icon_; parCount=parCount_;
        parNames=parNames_; fxActive = false;

        parValues= new int[parCount];
        for (int i = 0; i < parCount; i++) {
            parValues[i]=parValues_ [i];
        }
        parValuesDefault= new int[parCount];
        for (int i = 0; i < parCount; i++) {
            parValuesDefault[i]=parValues_ [i];
        }


    }
}
