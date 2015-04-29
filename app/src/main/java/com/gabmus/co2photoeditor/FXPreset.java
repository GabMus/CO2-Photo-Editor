package com.gabmus.co2photoeditor;


/**
 * Created by gabmus on 28/04/15.
 */
public class FXPreset {
    private int fxCount;
    private FXPresetTunings [] presetList;
    public String presetTitle;
    public FXPreset(int fxCount_, int [] fxID_, int [] tuningCount_, int [][] tuningValues_, String presetTitle_) {
        fxCount=fxCount_;
        presetList = new FXPresetTunings[fxCount];
        for (int i = 0; i < fxCount; i++) {
            presetList[i] = new FXPresetTunings(fxID_[i], tuningCount_[i], tuningValues_[i]);
        }
        presetTitle=presetTitle_;
    }

    public FXPreset(int fxCount_, FXPresetTunings [] presetList_) {
        fxCount=fxCount_;
        presetList = presetList_.clone();
    }

    public void toggleAllFX(FXHandler mFX, FilterSurfaceView mFsv, boolean active) {
        mFX.resetAllFX(mFsv);
        if (active)
            for (int i = 0; i < fxCount; i++) {
                mFX.enableFX(presetList[i].fxID, MainActivity.fsv, active);
                for (int j = 0; j < presetList[i].tuningCount; j++) {
                    mFX.tuneFX(presetList[i].fxID, j, presetList[i].tuningValues[j], mFsv);
                }
            }
    }


}
