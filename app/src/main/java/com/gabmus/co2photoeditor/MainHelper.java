package com.gabmus.co2photoeditor;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.ShareActionProvider;

import java.io.File;
import java.io.IOException;

/**
 * Created by gabmus on 29/04/15.
 */
public class MainHelper {
    MainActivity act;
    public static boolean gotSharedPic = false;
    public static Bitmap sharedPicBmp;
    public static Bitmap currentBitmap;
    SharedPreferences sharedpreferences;
    public static boolean userWelcomed;
    public static boolean choosePicOnStart = false;
    public static ProgressDialog loadingDialog;

    public MainHelper(MainActivity act_) {
        act=act_;
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(act);
        loadingDialog= new ProgressDialog(act);
        loadingDialog.setTitle("");
        loadingDialog.setMessage(act.getString(R.string.loading_message));
        choosePicOnStart=sharedpreferences.getBoolean("pref_choose_file_on_start_key", false);

    }

    public void receiveShareIntent() {
        //receive share implicit intent
        Uri imageUriFromShare = act.getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUriFromShare != null) {
            try {
                sharedPicBmp = MediaStore.Images.Media.getBitmap(act.getContentResolver(), imageUriFromShare);
                currentBitmap = sharedPicBmp;
                gotSharedPic = true;
                setNewColors();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void checkAndWelcomeUser() { //check if need to run the welcome activity
        SharedPreferences.Editor editor = sharedpreferences.edit();
        if (!sharedpreferences.contains("userWelcomedKey")) {
            editor.putBoolean("userWelcomedKey", false);
            editor.commit();
            userWelcomed=false;
        }
        if (sharedpreferences.contains("userWelcomedKey")) {
            userWelcomed = sharedpreferences.getBoolean("userWelcomedKey", false);
            if (!userWelcomed) {
                welcomeUser();
                userWelcomed=true;
                editor.putBoolean("userWelcomedKey", true);
                editor.commit();
            }
        }
    }

    public void welcomeUser() {
        if (!Boolean.parseBoolean(sharedpreferences.getString("pref_user_welcomed", "false"))) {
            Intent i = new Intent(act, WelcomeActivity.class);
            act.startActivity(i);
        }
    }

    public void setShareIntent(Intent shareIntent, ShareActionProvider mShareActionProvider) {
        if (mShareActionProvider != null) {
            String shareLocation = prepareImagePath();
            act.fsv.SaveImage(shareLocation);
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(shareLocation));
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    public String prepareImagePath() {
        String prefPath = sharedpreferences.getString("pref_save_path_key", act.getString(R.string.pref_save_path_default));
        if (prefPath.length() <=0) prefPath = act.getString(R.string.pref_save_path_default); //this should go away
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/" + prefPath;
        //tempfile is necessary because it creates the subdirectories if they don't exist

        File tempfile = new File(file_path);
        tempfile.mkdirs();
        String preferredFormat = "jpg";
        String imageName = Long.toString(System.currentTimeMillis() / 1000L);
        return file_path + "/" + imageName + "." + preferredFormat;
    }

    public int generatePalette(Bitmap bmp) {
        //note: could do this in an asyntask, but it's not worth it, i tried
        Palette pal = Palette.generate(bmp);
        return pal.getVibrantColor(0);
    }

    public void setNewColors() {
        if (android.os.Build.VERSION.SDK_INT>= 21) {
            int color = generatePalette(currentBitmap);
            act.getActionBar().setBackgroundDrawable(new ColorDrawable(color));
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            hsv[2] -= 0.2;
            int darkColor = Color.HSVToColor(hsv);
            act.getWindow().setStatusBarColor(darkColor);
            ActivityManager.TaskDescription desc = new ActivityManager.TaskDescription(act.getString(R.string.app_name), act.appIcon, color);
            act.setTaskDescription(desc);
        }
    }
}
