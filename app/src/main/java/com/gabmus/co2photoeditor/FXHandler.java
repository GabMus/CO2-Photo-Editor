package com.gabmus.co2photoeditor;

import android.content.Context;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by gabmus on 19/04/15.
 */
public class FXHandler {

    public static FXData[] FXList;
    public static FXPreset[] PresetList;
    public static String presetData;

    public FXHandler(Context context) {
        FXData[] tmpFXList = {
                new FXData(context.getString(R.string.blackAndWhite), R.drawable.bnw, 0, new int[0], new String[0]),
                new FXData(context.getString(R.string.sepia), R.drawable.sepia, 0, new int[0], new String[0]),
                new FXData(context.getString(R.string.negative), R.drawable.negative, 0, new int[0], new String[0]),
                new FXData(context.getString(R.string.adjustments), R.drawable.colorcorrection, 3, new int [] {50,25,50}, new String[] {context.getString(R.string.brightness), context.getString(R.string.contrast), context.getString(R.string.saturation)}),
                new FXData(context.getString(R.string.toneMapping1), R.drawable.tonemapping1, 4, new int [] {0,0,50,50}, new String[] {context.getString(R.string.exposure), context.getString(R.string.vignetting), context.getString(R.string.whiteLevel), context.getString(R.string.luminanceSaturation)}),
                new FXData(context.getString(R.string.tonality), R.drawable.tonality, 3, new int [] {50,50,50}, new String [] {context.getString(R.string.red), context.getString(R.string.green), context.getString(R.string.blue)}),
                new FXData(context.getString(R.string.noise1), R.drawable.noise, 5, new int [] {0,0,0,0,0}, new String [] {context.getString(R.string.amount), context.getString(R.string.size), context.getString(R.string.luminance), context.getString(R.string.color), context.getString(R.string.randomizerSeed)}),
                new FXData(context.getString(R.string.filmGrain), R.drawable.demo_icon, 4, new int [] {0,0,0,0}, new String [] {context.getString(R.string.strength), context.getString(R.string.darkNoisePower), context.getString(R.string.randomNoiseStrength), context.getString(R.string.randomizerSeed)}),
                new FXData(context.getString(R.string.bloom), R.drawable.bloom, 5, new int [] {0,0,0,0,50}, new String [] {context.getString(R.string.bloomThreshold), context.getString(R.string.bloomSaturation), context.getString(R.string.bloomBlur), context.getString(R.string.bloomIntensity), context.getString(R.string.bloomBaseIntensity)}),
                new FXData(context.getString(R.string.crt), R.drawable.crt, 1, new int [] {0}, new String[] {context.getString(R.string.lineWidth)}),
                new FXData(context.getString(R.string.sharpness), R.drawable.demo_icon, 2, new int [] {0,0}, new String[] {context.getString(R.string.radius), context.getString(R.string.intensity)})

        };

        presetData=MainActivity.helper.getPresetsPreference();




        FXList = tmpFXList.clone();
        refreshPresetList();
    }

    public void refreshPresetList() {
        PresetList = buildPresetsFromPreference().clone();
    }

    public void addPreset(String name) {
        presetData= presetData.substring(0,presetData.length()-1)+getPreset(name)+"#";
        refreshPresetList();
        MainActivity.helper.writePresetPreference(presetData);
    }

    public void deletePreset(int index) {
        int i=-1;
        int pointer=-1;
        while (i!=index) {
            pointer++;
            if (presetData.charAt(pointer)=='$') i++;
        }
        int trailer=pointer;
        while (presetData.charAt(trailer)!='€') {
            trailer++;
        }
        presetData=presetData.substring(0, pointer)+presetData.substring(trailer+1,presetData.length());
        refreshPresetList();
        MainActivity.helper.writePresetPreference(presetData);
    }

    public FXPreset[] buildPresetsFromPreference() {
        String pref=presetData;
        ArrayList<FXPreset> list = new ArrayList<>();
        ArrayList<Integer> indexes = new ArrayList<>();
        ArrayList<Integer[]> tuningValues = new ArrayList<>();
        ArrayList<Integer> tuningGroup = new ArrayList<>();
        String singleTuning="";
        String presetName="";
        String tmpPresetIndex="";
        int i=0;
        while (pref.charAt(i)!='#') {
            if (pref.charAt(i)=='$') {
                i++;
                while (pref.charAt(i)!='@') {
                    presetName+=pref.charAt(i);
                    i++;
                }
            }
            if (pref.charAt(i)=='@') {
                i++;
                while (pref.charAt(i)!='%') {
                    tmpPresetIndex+=pref.charAt(i);
                    i++;
                }
                indexes.add(Integer.parseInt(tmpPresetIndex));
                tmpPresetIndex="";
            }
            if (pref.charAt(i)=='%') {
                i++;
                if (pref.charAt(i)=='&') {
                    tuningValues.add(null);
                    i++;
                }
                while (pref.charAt(i)!='@' && pref.charAt(i)!='€') {

                    while (pref.charAt(i)!=',') {
                        singleTuning+=pref.charAt(i);
                        i++;
                    }
                    tuningGroup.add(Integer.parseInt(singleTuning));
                    singleTuning="";
                    i++;
                }
                tuningValues.add(tuningGroup.toArray(new Integer[tuningGroup.size()]));
                tuningGroup.clear();
            }

            if (pref.charAt(i)=='€') {
                list.add(new FXPreset(
                        indexes.toArray(new Integer[indexes.size()]),
                        tuningValues.toArray(new Integer[tuningValues.size()][]),
                        presetName
                ));
                indexes.clear();
                tuningValues.clear();
                presetName="";
                i++;
            }

        }

        return list.toArray(new FXPreset[list.size()]);
    }


         //boolean type is just to kill the method with return
    public boolean SelectFX(int fxID) {
        //Sliders not shown by default, so make them invisible
        //Enable the toggle, since no fx is selected by default, thus it's disabled

        //on fxID==-1 get to starting screen
        if (fxID==-1) {
            MainActivity.fxToggle.setEnabled(false);
            MainActivity.fxToggle.setChecked(false);
            MainActivity.textViewFXTitle.setText(MainActivity.strNoFXSelected);
            MainActivity.FXselected=-1;
            MainActivity.slidersListView.setAdapter(null);
            return true;
        }
        MainActivity.textViewFXTitle.setText(FXList[fxID].name);
        MainActivity.fxToggle.setEnabled(true);
        //FX is active? if yes, let the toggle show it
        if (FXList[fxID].fxActive) MainActivity.fxToggle.setChecked(true);
        else MainActivity.fxToggle.setChecked(false);
        //DONE: Enable sliders needed for each kind of effect, give them the correct label

        MainActivity.slidersListView.setAdapter(new CustomSlidersAdapter(MainActivity.context, FXList[fxID].parNames, FXList[fxID].parValues));

        return true;

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

    public String[] getPresetNames() {
        String[] toRet = new String[PresetList.length];
        for (int i = 0; i < toRet.length; i++) {
            toRet[i]=PresetList[i].presetTitle;
        }
        return toRet;
    }


    public void enableFX(int index, FilterSurfaceView mFsv, boolean active) {
        if (index!=-1) FXList[index].fxActive=active;
        mFsv.renderer.Render();
        switch (index) {
            case -1: //no fx
                break;
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
                mFsv.renderer.PARAMS_EnableContrastSaturationBrightness = active;
                break;
            case 4: // Tone mapping 1
                mFsv.renderer.PARAMS_EnableToneMapping = active;
                break;
            case 9: //CRT
                mFsv.renderer.PARAMS_EnableCathodeRayTube = active;
                break;
            case 6: //VHS Noise, ex Film Grain
                mFsv.renderer.PARAMS_EnableFilmGrain = active;
                break;
            case 7: //Proper Film Grain
                mFsv.renderer.PARAMS_EnableProperFilmGrain = active;
                break;
            case 8: //bloom
                mFsv.renderer.PARAMS_EnableBloom = active;
                break;
            case 5: //tonality
                mFsv.renderer.PARAMS_EnableTonality = active;
                break;
            case 10: //sharpness
                mFsv.renderer.PARAMS_EnableSharpness = active;
                break;
            default:
                Log.e("CO2 Photo Editor", "enableFX: index out of range");
                break;
        }
        MainActivity.effectsListAdapter.notifyDataSetChanged();
    }

    public void tuneFX(int FXIndex, int valIndex, int tuningValue, FilterSurfaceView mFsv) {
        float finalValue=0.0f;
        switch (FXIndex) {
            case 3: //color correction
                switch (valIndex) {
                    case 1:
                        //edit brightness
                        finalValue = (tuningValue/100f)*2f;
                        mFsv.renderer.PARAMS_Brightness = finalValue;
                        break;
                    case 2:
                        //edit contrast
                        finalValue = (tuningValue/100f)*4f;
                        mFsv.renderer.PARAMS_Contrast = finalValue;
                        break;
                    case 3:
                        //edit saturation
                        finalValue = (tuningValue/100f)*2f;
                        mFsv.renderer.PARAMS_Saturation = finalValue;
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
                    case 3:
                        //edit white level
                        finalValue = (tuningValue/100f)*2f;
                        mFsv.renderer.PARAMS_ToneMappingWhiteLevel = finalValue;
                        break;
                    case 4:
                        //edit luminance saturation
                        finalValue= (tuningValue/100f)*2f;
                        mFsv.renderer.PARAMS_ToneMappingLuminanceSaturation = finalValue;
                        break;
                }
                break;

            case 9: //crt
                switch (valIndex) {
                    case 1: //edit line width
                        finalValue = ((tuningValue/100f)*10)+1;
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
                        mFsv.renderer.PARAMS_FilmGrainSeed=tuningValue;
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

            case 8: //bloom
                switch (valIndex) {
                    case 1: //threshold
                        finalValue = (tuningValue / 100f) * 1f;
                        mFsv.renderer.PARAMS_BloomThreshold = finalValue;
                        break;
                    case 2: //saturation
                        finalValue = (tuningValue / 100f) * 1f;
                        mFsv.renderer.PARAMS_BloomSaturation = finalValue;
                        break;
                    case 3: //blur
                        finalValue = ((tuningValue / 100f) * 9f)+1f;
                        mFsv.renderer.PARAMS_BloomBlur = finalValue;
                        break;
                    case 4: //intensity
                        finalValue = (tuningValue / 100f) * 2f;
                        mFsv.renderer.PARAMS_BloomIntensity=finalValue;
                        break;
                    case 5: //base-intensity
                        finalValue = (tuningValue / 100f) * 2f;
                        mFsv.renderer.PARAMS_BloomBaseIntensity=finalValue;
                        break;
                }
                break;
            case 5: //tonality
                switch (valIndex) {
                    case 1: //Red
                        finalValue = (tuningValue / 100f) * 2f;
                        mFsv.renderer.PARAMS_TonalityR = finalValue;
                        break;
                    case 2: //Green
                        finalValue = (tuningValue / 100f) * 2f;
                        mFsv.renderer.PARAMS_TonalityG = finalValue;
                        break;
                    case 3: //Blue
                        finalValue = (tuningValue / 100f) * 2f;
                        mFsv.renderer.PARAMS_TonalityB = finalValue;
                        break;
                }
                break;

            case 10: //sharpness
                switch (valIndex) {
                    case 1: //radius
                        finalValue = (tuningValue / 100f) * 4f;
                        mFsv.renderer.PARAMS_SharpnessRadius = finalValue;
                        break;
                    case 2: //intensity
                        finalValue = (tuningValue / 100f) * 3f;
                        mFsv.renderer.PARAMS_SharpnessIntensity = finalValue;
                        break;
                }
                break;
            //other cases

            default:
                Log.e("CO2 Photo Editor", "tuneFX: index out of range");
                break;
        }
    }



    public boolean resetFX(int index, FilterSurfaceView mFsv) {
        if (index==-1) return false;
        SelectFX(-1); //go to default screen
        FXList[index].parValues=FXList[index].parValuesDefault.clone();
        for (int i = 0; i < FXList[index].parCount; i++) {
            tuneFX(index, i+1, FXList[index].parValuesDefault[i], mFsv);
        }
        return true;
    }

    private void resetFXModular(int index, FilterSurfaceView mFsv) {
        FXList[index].parValues=FXList[index].parValuesDefault.clone();
        for (int i = 0; i < FXList[index].parCount; i++) {
            tuneFX(index, i+1, FXList[index].parValuesDefault[i], mFsv);
        }
    }

    public void resetAllFX(FilterSurfaceView mFsv) {
        for (int i = 0; i < FXList.length; i++) {
            resetFXModular(i, mFsv);
            enableFX(i, mFsv, false);
        }
        SelectFX(-1); //go to default screen
    }

    public void initializeAll(FilterSurfaceView mFsv) {
        for (int i = 0; i < FXList.length; i++) {
            for (int j = 0; j < FXList[i].parCount; j++) {
                tuneFX(i, j+1, FXList[i].parValues[j], mFsv);
            }
        }

    }

    public void printAllVals() {
        for (int i = 0; i < FXList.length; i++) {
            if (FXList[i].fxActive) {
                Log.d(FXData.DEBUG_PREFIX, "Index: "+i);
                FXList[i].printVals();
                Log.d(FXData.DEBUG_PREFIX, "\n**********\n");
            }
        }
    }

    public String getPreset(String presetName) {
        String toRet="";
        toRet+="$"+presetName;
        for (int i = 0; i < FXList.length; i++) {
            if (FXList[i].fxActive) {
                toRet+="@"+i;
                toRet+="%";
                if (FXList[i].parCount==0)
                    toRet+="&";
                else {
                    for (int j = 0; j < FXList[i].parCount; j++) {
                        toRet+=FXList[i].parValues[j]+",";
                    }
                }
            }
        }
        toRet+="€";
        return toRet;
    }

}
