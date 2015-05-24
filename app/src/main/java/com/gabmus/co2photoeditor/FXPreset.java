package com.gabmus.co2photoeditor;


/**
 * Created by gabmus on 28/04/15.
 */
public class FXPreset {
    private int fxCount;
    private FXPresetTunings [] presetList;
    public String presetTitle;
    public FXPreset(Integer [] fxID_, Integer [][] tuningValues_, String presetTitle_) {
        if (fxID_==null) return;
        fxCount=fxID_.length;
        presetList = new FXPresetTunings[fxCount];
        for (int i = 0; i < fxCount; i++) {
            presetList[i] = new FXPresetTunings(fxID_[i], tuningValues_[i]);
        }
        presetTitle=presetTitle_;
    }

    public FXPreset(String presetTitle_, FXPresetTunings [] presetList_) {
        if (presetList_==null) return;
        presetTitle=presetTitle_;
        fxCount=presetList_.length;
        presetList = presetList_.clone();
    }

    public void toggleAllFX(FXHandler mFX, FilterSurfaceView mFsv, boolean active) {
        mFX.resetAllFX(mFsv);
        if (active)
            for (int i = 0; i < fxCount; i++) {
                mFX.enableFX(presetList[i].fxID, MainActivity.fsv, active);
                for (int j = 0; j < presetList[i].tuningCount; j++) {
                    mFX.tuneFX(presetList[i].fxID, j, presetList[i].tuningValues[j], mFsv);
                    mFX.FXList[presetList[i].fxID].parValues[j]=presetList[i].tuningValues[j];
                }
            }
        mFX.initializeAll(mFsv);
    }


}
