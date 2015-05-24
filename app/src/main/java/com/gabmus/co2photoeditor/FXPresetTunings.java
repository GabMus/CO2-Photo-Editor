package com.gabmus.co2photoeditor;

/**
 * Created by gabmus on 28/04/15.
 */
public class FXPresetTunings {
    public Integer fxID;
    public int tuningCount;
    public Integer [] tuningValues;

    public FXPresetTunings(Integer fxID_, Integer [] tuningValues_) {
        fxID=fxID_;
        if (tuningValues_!=null) tuningCount=tuningValues_.length;
        else tuningCount=0;
        if (tuningValues_!=null) tuningValues=tuningValues_.clone();
    }

}
