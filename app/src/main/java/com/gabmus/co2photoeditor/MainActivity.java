package com.gabmus.co2photoeditor;

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.lang.reflect.Field;


public class MainActivity extends Activity {

    private final int REQUEST_IMAGE_CHOOSE=1;
    public static MainHelper helper;
    public static Bitmap appIcon;
    private Intent mShareIntent;
    private ShareActionProvider mShareActionProvider;
    public static DrawerLayout fxDrawer;

    public static int FXselected = -1;
    public static FrameLayout customViewLayout;

    public static Switch fxToggle;
    public static TextView textViewFXTitle;
    public static String strNoFXSelected;

    public static ListView slidersListView;

    public static FilterSurfaceView fsv;

    public static FXHandler FX;
    public static CustomFXAdapter effectsListAdapter;

    ListView effectsList;
    ListView presetList;
    public static Context context;

    ActionBarDrawerToggle drawerToggle;

    public static Handler toastHandler = new Handler();
    public static Runnable toastRunnable;
    public static Runnable loadingRunnableShow;
    public static Runnable loadingRunnableDismiss;
    public static LinearLayout fadingSliderContainer;

    private boolean isFsvBig=false;

    public static String newPresetName="";
    public CustomPresetAdapter presetAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper= new MainHelper(this);
        setContentView(R.layout.activity_main);

        context = this;

        FX = new FXHandler(context);

        //initialize the filtersurfaceview
        fadingSliderContainer= (LinearLayout) findViewById(R.id.fadingSliderContainer);
        customViewLayout = (FrameLayout) findViewById(R.id.customViewLayout);
        fsv = new FilterSurfaceView(getApplicationContext(), this);
        fsv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        customViewLayout.addView(fsv);
        fsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFsvBig) {
                    fadingSliderContainer.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .setListener(new Animator.AnimatorListener() {
                                @Override public void onAnimationStart(Animator animator) {}
                                @Override public void onAnimationCancel(Animator animator) {}
                                @Override public void onAnimationRepeat(Animator animator) {}
                                @Override public void onAnimationEnd(Animator animator) {
                                    fadingSliderContainer.setVisibility(View.GONE);
                                }
                            });
                    isFsvBig=true;
                }
                else {
                    fadingSliderContainer.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .setListener(null);
                    fadingSliderContainer.setVisibility(View.VISIBLE);
                    isFsvBig=false;
                }

            }
        });

        //set default values on startup
        FX.initializeAll(fsv);

        appIcon=BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
        //receive share implicit intent
        helper.receiveShareIntent();

        if (!helper.gotSharedPic && helper.choosePicOnStart && helper.currentBitmap==null) {
            startPickerIntent();
        }

        //force show overflow button TO TEST!
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }



        //runnables for use in external threads
        toastRunnable = new Runnable() {
            public void run() {
                Toast.makeText(context, getString(R.string.toast_image_saved) + fsv.renderer.SavePath, Toast.LENGTH_LONG).show();
            }};
        loadingRunnableShow = new Runnable() {
            public void run() {
                helper.loadingDialog.show();
            }};
        loadingRunnableDismiss = new Runnable() {
            public void run() {
                helper.loadingDialog.dismiss();
            }};
        //welcome activity
        helper.checkAndWelcomeUser();

        slidersListView = (ListView) findViewById(R.id.sliders_list_view);

        //navigation drawer management
        fxDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        drawerToggle = new ActionBarDrawerToggle(this, fxDrawer, R.string.drawer_open, R.string.drawer_close) {
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

        textViewFXTitle = (TextView) findViewById(R.id.textViewEffectTitle);

        strNoFXSelected = getString(R.string.effect_title_textview);

        fxToggle = (Switch) findViewById(R.id.switch1);

        effectsListAdapter=new CustomFXAdapter(this, FX.getFXnames(), FX.getFXicons());
        effectsList = (ListView) findViewById(R.id.listView);
        effectsList.setAdapter(effectsListAdapter);
        effectsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FXselected = i;
                FX.SelectFX(i);
                if (!FX.FXList[i].fxActive && (helper.sharedpreferences.getBoolean("pref_activate_onclick_key", true))) {
                    fxToggle.setChecked(true);
                }
                fxDrawer.closeDrawers();
            }
        });

        effectsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!FX.FXList[i].fxActive) return false;
                FXselected = i;
                FX.SelectFX(i);
                fxToggle.setChecked(false);
                FX.SelectFX(-1);
                Toast.makeText(context, context.getString(R.string.effect_disabled_message), Toast.LENGTH_LONG).show();
                return true;
            }
        });

        //DONE: implement on switch state changed.

        //this makes the switches consistent and changes the FXList value to match the switch
        fxToggle.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    FX.enableFX(FXselected, fsv, true);
                } else {
                    FX.enableFX(FXselected, fsv, false);
                }
            }
        });

        presetList = (ListView) findViewById(R.id.listViewPresets);
        presetAdapter=new CustomPresetAdapter(this, FX.getPresetNames());
        presetList.setAdapter(presetAdapter);
        presetList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FXselected = -1;
                FX.resetAllFX(fsv);
                FX.PresetList[i].toggleAllFX(FX, fsv, true);
                fxDrawer.closeDrawers();
            }
        });
        presetList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i<=7) {
                    Toast.makeText(context,getString(R.string.cantDeleteDefaultPresets),Toast.LENGTH_LONG).show();
                    return true;
                }
                alertDeletePreset(i);
                return true;
            }
        });

        mShareIntent = new Intent();
        mShareIntent.setAction(Intent.ACTION_SEND);
        mShareIntent.setType("image/*");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CHOOSE && resultCode == RESULT_OK) {

            //done: implement PROPER bitmap to gl support (without using the imgview...)
            Uri imgPath = data.getData();

            try {
                helper.currentBitmap.recycle();
                helper.currentBitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgPath);
                fsv.LoadBitmap(helper.currentBitmap);
                helper.setNewColors();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem menuItemShare = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) menuItemShare.getActionProvider();
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(mShareIntent);
        }

        return true;
    }


    public void alertDeletePreset(final int index) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getString(R.string.deletePresetAlert_message));

        alert.setPositiveButton(getString(R.string.alert_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                FX.deletePreset(index);
                presetAdapter=new CustomPresetAdapter(context, FX.getPresetNames());
                presetList.setAdapter(presetAdapter);
            }
        });

        alert.setNegativeButton(getString(R.string.alert_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    public void savePreset() {
        newPresetName="";
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        //alert.setTitle("Title");
        alert.setMessage(getString(R.string.choosePresetName));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton(getString(R.string.alert_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                newPresetName = input.getText().toString();
                if (newPresetName.length() == 0)
                    Toast.makeText(context, getString(R.string.presetTitleIsEmpty), Toast.LENGTH_LONG).show();
                else if (newPresetName.contains("$") || newPresetName.contains("@") || newPresetName.contains("%") || newPresetName.contains("&") || newPresetName.contains("€") || newPresetName.contains(",") || newPresetName.contains("#"))
                    Toast.makeText(context, getString(R.string.presetTitleIsInvalid), Toast.LENGTH_LONG).show();
                else {
                    FX.addPreset(newPresetName);
                    presetAdapter=new CustomPresetAdapter(context, FX.getPresetNames());
                    presetList.setAdapter(presetAdapter);
                }
            }
        });

        alert.setNegativeButton(getString(R.string.alert_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        /*if (id == R.id.tmpLogVals) {
            FX.printAllVals();
        }*/

        if (id == R.id.action_savePreset) {
            savePreset();
            //presetList.setAdapter(new CustomPresetAdapter(this, FX.getPresetNames()));
            return true;
        }

        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.action_save) {
            String imgPath = helper.prepareImagePath();
            fsv.SaveImage(imgPath);
            //Toast.makeText(this, getString(R.string.toast_image_saved) + imgPath, Toast.LENGTH_LONG).show();
            return true;
        }

        if (id == R.id.action_selectpic) {
            startPickerIntent();
            return true;
        }

        if (id == R.id.action_resetFX) {
            boolean res = FX.resetFX(FXselected, fsv);
            if (!res) Toast.makeText(this, getString(R.string.no_fx_selected), Toast.LENGTH_LONG).show();
            return true;
        }

        if (id == R.id.action_resetFXAll) {
            FX.resetAllFX(fsv);
            return true;
        }

        if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }

        if (id == R.id.menu_item_share) {

            helper.setShareIntent(mShareIntent, mShareActionProvider);
            return true;
        }

        //add temp menu for testing welcomeactivity
        /*if (id == R.id.tmpWelcome) {
            helper.welcomeUser();
            return true;
        }*/

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

    public void startPickerIntent() {
        Intent intentChooseUpdate = new Intent(Intent.ACTION_GET_CONTENT);
        intentChooseUpdate.setType("image/*");
        startActivityForResult(Intent.createChooser(intentChooseUpdate, "Choose a picture"), REQUEST_IMAGE_CHOOSE);
    }



}