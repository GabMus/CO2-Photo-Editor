package com.gabmus.co2photoeditor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
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
    SharedPreferences sharedpreferences;
    public static boolean userWelcomed;

    public MainHelper(MainActivity act_) {
        act=act_;
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(act);
    }

    public void receiveShareIntent() {
        //receive share implicit intent
        Uri imageUriFromShare = act.getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUriFromShare != null) {
            try {
                sharedPicBmp = MediaStore.Images.Media.getBitmap(act.getContentResolver(), imageUriFromShare);
                gotSharedPic = true;
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
        if (!Boolean.parseBoolean(PreferenceManager.getDefaultSharedPreferences(act).getString("pref_user_welcomed", "false"))) {
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
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/" + sharedpreferences.getString("pref_save_path_key", act.getString(R.string.pref_save_path_default));
        //tempfile is necessary because it creates the subdirectories if they don't exist
        File tempfile = new File(file_path);
        tempfile.mkdirs();
        String preferredFormat = "jpg";
        String imageName = Long.toString(System.currentTimeMillis() / 1000L);
        return file_path + "/" + imageName + "." + preferredFormat;
    }


}
