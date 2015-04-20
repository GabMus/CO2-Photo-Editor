package com.gabmus.co2photoeditor;

import android.view.View;

/**
 * Created by gabmus on 19/04/15.
 */
public class FXHandler {

    public FXData[] FXList = {
            new FXData("B&W", R.drawable.demo_icon, 0, new int[0], new String[0]),
            new FXData("Negative", R.drawable.demo_icon, 0, new int[0], new String[0]),
            new FXData("Color Correction", R.drawable.demo_icon, 3, new int [] {0,0,0}, new String[] {"Brightness", "Contrast", "Saturation"}),
            new FXData("Vignette", R.drawable.demo_icon, 1, new int [] {0}, new String[] {"FX Strength"})
    };

    public FXHandler() {
    }

    public void SelectFX(int fxID) {
        //Sliders not shown by default, so make them invisible
        MainActivity.makeAllSlidersDisappear();
        //Enable the toggle, since no fx is selected by default, thus it's disabled
        MainActivity.fxToggle.setEnabled(true);
        //FX is active? if yes, let the toggle show it
        if (FXList[fxID].fxActive) MainActivity.fxToggle.setChecked(true);
        else MainActivity.fxToggle.setChecked(false);
        //DONE: Enable sliders needed for each kind of effect, give them the correct label
        if (FXList[fxID].parCount > 0) {
            MainActivity.sst1.setVisibility(View.VISIBLE);
            MainActivity.sk1.setProgress(FXList[fxID].parValues[0]);
            MainActivity.slb1.setText(FXList[fxID].parNames[0]);
            if (FXList[fxID].parCount > 1) {
                MainActivity.sst2.setVisibility(View.VISIBLE);
                MainActivity.sk2.setProgress(FXList[fxID].parValues[1]);
                MainActivity.slb2.setText(FXList[fxID].parNames[1]);
                if (FXList[fxID].parCount > 2) {
                    MainActivity.sst3.setVisibility(View.VISIBLE);
                    MainActivity.sk3.setProgress(FXList[fxID].parValues[2]);
                    MainActivity.slb3.setText(FXList[fxID].parNames[2]);
                    if (FXList[fxID].parCount > 3) {
                        MainActivity.sst4.setVisibility(View.VISIBLE);
                        MainActivity.sk4.setProgress(FXList[fxID].parValues[3]);
                        MainActivity.slb4.setText(FXList[fxID].parNames[3]);
                        if (FXList[fxID].parCount > 4) {
                            MainActivity.sst5.setVisibility(View.VISIBLE);
                            MainActivity.sk5.setProgress(FXList[fxID].parValues[4]);
                            MainActivity.slb5.setText(FXList[fxID].parNames[4]);
                        }
                    }
                }
            }
        }


    }

    public String[] getFXnames() {
        String[] toRet = new String[FXList.length];
        for (int i=0; i< toRet.length; i++) {
            toRet[i]=FXList[i].name;
        }
        return toRet;
    }

    public int[] getFXicons() {
        int[] toRet = new int[FXList.length];
        for (int i=0; i< toRet.length; i++) {
            toRet[i]=FXList[i].icon;
        }
        return toRet;
    }

}
