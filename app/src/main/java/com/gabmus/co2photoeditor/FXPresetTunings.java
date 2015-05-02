package com.gabmus.co2photoeditor;

/**
 * Created by gabmus on 28/04/15.
 */
public class FXPresetTunings {
    public int fxID;
    public int tuningCount;
    public int [] tuningValues;

    public FXPresetTunings(int fxID_, int tuningCount_, int [] tuningValues_) {
        fxID=fxID_;
        tuningCount=tuningCount_;
        if (tuningValues_!=null) tuningValues=tuningValues_.clone();
    }

}
