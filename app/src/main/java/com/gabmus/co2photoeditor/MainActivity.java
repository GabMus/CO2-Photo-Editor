package com.gabmus.co2photoeditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.ShareActionProvider;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;


public class MainActivity extends Activity {

    public static boolean userWelcomed = false;
    private Bitmap bmp;
    private ImageView imgView;
    private BitmapDrawable abmp;
    private Intent mShareIntent;
    private ShareActionProvider mShareActionProvider;
    public static DrawerLayout fxDrawer;

    public static int FXselected=-1;
    public LinearLayout customViewLayout;

    public static Switch fxToggle;

    //declared up to 5 sliders (already present in the GUI but hidden by default) since various effects can have 0 to 5 parameters
    public static SeekBar sk1;
    public static SeekBar sk2;
    public static SeekBar sk3;
    public static SeekBar sk4;
    public static SeekBar sk5;

    public static TextView slb1;
    public static TextView slb2;
    public static TextView slb3;
    public static TextView slb4;
    public static TextView slb5;

    public static LinearLayout sst1;
    public static LinearLayout sst2;
    public static LinearLayout sst3;
    public static LinearLayout sst4;
    public static LinearLayout sst5;

    public FilterSurfaceView fsv;

    public static FXHandler FX = new FXHandler();

    ListView effectsList;
    Context context;

    ActionBarDrawerToggle drawerToggle;

    public static void makeAllSlidersDisappear() {
        sst1.setVisibility(View.GONE);
        sst2.setVisibility(View.GONE);
        sst3.setVisibility(View.GONE);
        sst4.setVisibility(View.GONE);
        sst5.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //DONE: launch welcome activity
        //welcomeUser(); //TODO: fix welcome activity


        //DONE: insert drawer button
        fxDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        drawerToggle = new ActionBarDrawerToggle( this, fxDrawer, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                invalidateOptionsMenu();
            }
        };
        fxDrawer.setDrawerListener(drawerToggle);

        customViewLayout= (LinearLayout) findViewById(R.id.customViewLayout);

        fsv = new FilterSurfaceView(getApplicationContext(),this);

        fsv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //setContentView(fsv);
        customViewLayout.addView(fsv);

        fxToggle = (Switch) findViewById(R.id.switch1);

        sk1=(SeekBar) findViewById(R.id.seekBar1);
        sk2=(SeekBar) findViewById(R.id.seekBar2);
        sk3=(SeekBar) findViewById(R.id.seekBar3);
        sk4=(SeekBar) findViewById(R.id.seekBar4);
        sk5=(SeekBar) findViewById(R.id.seekBar5);

        slb1=(TextView) findViewById(R.id.sliderLabel1);
        slb2=(TextView) findViewById(R.id.sliderLabel2);
        slb3=(TextView) findViewById(R.id.sliderLabel3);
        slb4=(TextView) findViewById(R.id.sliderLabel4);
        slb5=(TextView) findViewById(R.id.sliderLabel5);

        sst1=(LinearLayout) findViewById(R.id.sSetting1);
        sst2=(LinearLayout) findViewById(R.id.sSetting2);
        sst3=(LinearLayout) findViewById(R.id.sSetting3);
        sst4=(LinearLayout) findViewById(R.id.sSetting4);
        sst5=(LinearLayout) findViewById(R.id.sSetting5);

        //DONE: setup seekbar methods

        sk1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override                                   //i=value, b=by user?
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    FX.FXList[FXselected].parValues[0]=i;
                    FX.tuneFX(FXselected, 1, i, fsv);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sk2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override                                   //i=value, b=by user?
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    FX.FXList[FXselected].parValues[1]=i;
                    FX.tuneFX(FXselected, 2, i, fsv);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sk3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override                                   //i=value, b=by user?
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    FX.FXList[FXselected].parValues[2]=i;
                    FX.tuneFX(FXselected, 3, i, fsv);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sk4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override                                   //i=value, b=by user?
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    FX.FXList[FXselected].parValues[3]=i;
                    FX.tuneFX(FXselected, 4, i, fsv);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sk5.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override                                   //i=value, b=by user?
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {

                    FX.FXList[FXselected].parValues[4]=i;
                    FX.tuneFX(FXselected, 5, i, fsv);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //DONE: implement on switch state changed.

        //this makes the switches consistent and changes the FXList value to match the switch
        fxToggle.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    FX.FXList[FXselected].fxActive = true;
                    FX.enableFX(FXselected, fsv, true);
                }
                else {
                    FX.FXList[FXselected].fxActive = false;
                    FX.enableFX(FXselected, fsv, false);
                }
            }


        });

        //disable all sliders by default
        makeAllSlidersDisappear();

        context=this;

        effectsList=(ListView) findViewById(R.id.listView);
        effectsList.setAdapter(new CustomAdapter(this, FX.getFXnames(),FX.getFXicons()));

        mShareIntent = new Intent();
        mShareIntent.setAction(Intent.ACTION_SEND);
        mShareIntent.setType("image/*");

        imgView = (ImageView) findViewById(R.id.imageView);


    }

    private void welcomeUser() {
        if (!Boolean.parseBoolean(PreferenceManager.getDefaultSharedPreferences(this).getString("pref_user_welcomed", "false"))) {
            Intent i = new Intent(this, WelcomeActivity.class);
            startActivity(i);
        }
    }


    public String bmpToFile(Bitmap mBmp) {
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/"+PreferenceManager.getDefaultSharedPreferences(this).getString("pref_save_path_key", getString(R.string.pref_save_path_default));
        String preferredFormat = "jpg";
        Calendar c = Calendar.getInstance();
        String savePath =  Integer.toString(c.get(Calendar.SECOND));
        File dir = new File(file_path);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dir,savePath+"."+preferredFormat);
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        mBmp.compress(Bitmap.CompressFormat.PNG, 85, fOut);

        try {
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file_path+"/"+savePath+".png";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==RESULT_OK) {

            //todo: implement PROPER bitmap to gl support (without using the imgview...)
            Uri imgPath = data.getData();
            /*
            File img = new File(imgPath);
            BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(img.getAbsolutePath(),bmpOptions);

            bitmap = Bitmap.createBitmap(bitmap);*/
            imgView.setImageURI(imgPath);

            abmp = (BitmapDrawable)imgView.getDrawable();
            bmp = abmp.getBitmap();
            //bmp = BitmapFactory.decodeFile(imgPath.toString(), new BitmapFactory.Options());
            //if (true) throw new RuntimeException("W" + bmp.getWidth() + "\nH" + bmp.getHeight());
            fsv.LoadBitmap(bmp);
        }
            super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem menuItemShare = menu.findItem(R.id.menu_item_share);
        mShareActionProvider=(ShareActionProvider) menuItemShare.getActionProvider();
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(mShareIntent);
        }

        return true;
    }

    public void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            String pathImgToShare=bmpToFile(fsv.GetImage());
            mShareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(pathImgToShare));
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.action_save) {
            bmpToFile(fsv.GetImage());
            return true;
        }

        if (id == R.id.action_selectpic) {
            Intent intentChooseUpdate = new Intent(Intent.ACTION_GET_CONTENT);
            intentChooseUpdate.setType("image/*");
            startActivityForResult(Intent.createChooser(intentChooseUpdate, "Choose a picture"), 1);
            return true;
        }

        if (id == R.id.menu_item_share) {

            setShareIntent(mShareIntent);
            return true;
        }

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }





        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }
}
