package com.gabmus.co2photoeditor;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

import java.util.logging.Filter;

/**
 * Created by gabmus on 19/04/15.
 */
public class FXHandler {

    public FXData[] FXList;

    public FXHandler(Context context) {
        FXData[] tmpFXList = {
                new FXData(context.getString(R.string.blackAndWhite), R.drawable.demo_icon, 0, new int[0], new String[0]),
                new FXData(context.getString(R.string.sepia), R.drawable.demo_icon, 0, new int[0], new String[0]),
                new FXData(context.getString(R.string.negative), R.drawable.demo_icon, 0, new int[0], new String[0]),
                new FXData("Color Correction", R.drawable.demo_icon, 3, new int [] {0,0,0}, new String[] {context.getString(R.string.brightness), context.getString(R.string.contrast), context.getString(R.string.saturation)}),
                new FXData(context.getString(R.string.toneMapping1), R.drawable.demo_icon, 2, new int [] {0,0}, new String[] {context.getString(R.string.exposure), context.getString(R.string.vignetting)}),
                new FXData(context.getString(R.string.crt), R.drawable.demo_icon, 1, new int [] {0}, new String[] {context.getString(R.string.lineWidth)}),
                new FXData(context.getString(R.string.noise1), R.drawable.demo_icon, 5, new int [] {0,0,0,0,0}, new String [] {context.getString(R.string.amount), context.getString(R.string.size), context.getString(R.string.luminance), context.getString(R.string.color), context.getString(R.string.randomizerSeed)}),
                new FXData(context.getString(R.string.filmGrain), R.drawable.demo_icon, 4, new int [] {0,0,0,0}, new String [] {context.getString(R.string.strength), context.getString(R.string.darkNoisePower), context.getString(R.string.randomNoiseStrength), context.getString(R.string.randomizerSeed)})
        };
        FXList = tmpFXList;
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

    public void enableFX(int index, FilterSurfaceView mFsv, boolean active) {
        switch (index) {
            case 0: //B&W
                mFsv.renderer.PARAMS_EnableBlackAndWhite = active;
                break;
            case 1: // Sepia
                mFsv.renderer.PARAMS_EnableSepia = active;
                break;
            case 2: // Negative
                mFsv.renderer.PARAMS_EnableNegative = active;
                break;
            case 3: //color correction
                //mFsv.renderer.PARAMS_EnableColorCorrection = active;
                break;
            case 4: // Tone mapping 1
                mFsv.renderer.PARAMS_EnableToneMapping = active;
                break;
            case 5: //CRT
                mFsv.renderer.PARAMS_EnableCathodeRayTube = active;
                break;
            case 6: //VHS Noise, ex Film Grain
                mFsv.renderer.PARAMS_EnableFilmGrain = active;
                break;
            case 7: //Proper Film Grain
                mFsv.renderer.PARAMS_EnableProperFilmGrain = active;
                break;
            default:
                Log.e("CO2 Photo Editor", "enableFX: index out of range");
                break;
        }
    }

    public void tuneFX(int FXIndex, int valIndex, int tuningValue, FilterSurfaceView mFsv) {
        float finalValue=0.0f;
        switch (FXIndex) {
            case 3: //color correction
                switch (valIndex) {
                    case 1:
                        //edit brightness
                        break;
                    case 2:
                        //edit contrast
                        break;
                    case 3:
                        //edit saturation
                        break;
                    default:
                        Log.e("CO2 Photo Editor", "tuneFX: colorCorrection: index out of range (>3)");
                        break;
                }
                break;
            case 4: //tone mapping
                switch (valIndex) {
                    case 1:
                        //edit exposure
                        finalValue = (tuningValue/100f)*2f;
                        mFsv.renderer.PARAMS_ToneMappingExposure = finalValue;
                        break;
                    case 2:
                        //edit vignetting
                        finalValue = (tuningValue/100f)*3f;
                        mFsv.renderer.PARAMS_ToneMappingVignetting = finalValue;
                        break;
                }
                break;

            case 5: //crt
                switch (valIndex) {
                    case 1: //edit line width
                        finalValue = (tuningValue/100f)*4;
                        int tmpValue=(int)finalValue;
                        mFsv.renderer.PARAMS_CathodeRayTubeLineWidth = tmpValue;
                        break;
                }
                break;
//"Grain Amount", "Grain Size", "Luminance Amount", "Color Amount"
            case 6: //VHS Noise, ex Film Grain
                switch (valIndex) {
                    case 1: //Grain amount
                        finalValue = (tuningValue/100f)*1f;
                        mFsv.renderer.PARAMS_FilmGrainAmount = finalValue;
                        break;
                    case 2: //grain size
                        finalValue = ((tuningValue/100f)*3f)+1;
                        mFsv.renderer.PARAMS_FilmGrainParticleSize = finalValue;
                        break;
                    case 3: //luminance amount
                        finalValue = ((tuningValue/100f)*3f);
                        mFsv.renderer.PARAMS_FilmGrainLuminance = finalValue;
                        break;
                    case 4: //color amount
                        finalValue = (tuningValue/100f)*10f;
                        mFsv.renderer.PARAMS_FilmGrainColorAmount = finalValue;
                        break;
                    case 5: //randomizer seed
                        //this has a certain amount of randomicity
                        mFsv.renderer.setPARAMS_FilmGrainSeed(tuningValue);
                        break;
                }
                break;

            case 7:
                switch (valIndex) {
                    case 1: //Strength
                        finalValue = (tuningValue / 100f) * 1f;
                        mFsv.renderer.PARAMS_ProperFilmGrainStrength = finalValue;
                        break;
                    case 2: //Dark Noise Power
                        finalValue = ((tuningValue / 100f) * 3f) + 1;
                        mFsv.renderer.PARAMS_ProperFilmGrainAccentuateDarkNoisePower = finalValue;
                        break;
                    case 3: //Random noise strength
                        finalValue = ((tuningValue / 100f) * 3f);
                        mFsv.renderer.PARAMS_ProperFilmGrainRandomNoiseStrength = finalValue;
                        break;
                    case 4: //randomizer seed
                        mFsv.renderer.PARAMS_ProperFilmGrainRandomValue=tuningValue;
                        break;
                }
                break;

            //other cases

            default:
                Log.e("CO2 Photo Editor", "tuneFX: index out of range");
                break;
        }
    }

}
