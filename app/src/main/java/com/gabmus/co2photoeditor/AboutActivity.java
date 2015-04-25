package com.gabmus.co2photoeditor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;


public class AboutActivity extends Activity {

    private ListView listViewAbout;
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        listViewAbout = (ListView) findViewById(R.id.listViewAbout);

        String[] titles = new String[] {
                getString(R.string.label_about_appAuthor),
                getString(R.string.label_about_glAuthor),
                getString(R.string.label_about_wantToHelp),
                getString(R.string.label_about_license),
        };

        String[] contents = new String[] {
                getString(R.string.label_about_appAuthor_c),
                getString(R.string.label_about_glAuthor_c),
                getString(R.string.label_about_wantToHelp_c),
                getString(R.string.label_about_license_c),
        };

        listViewAbout.setAdapter(new CustomAboutAdapter(this, titles, contents));
        listViewAbout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0: //app author
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://gabmus.github.io")));
                        break;
                    case 1: //gl author
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/xeredent")));
                        break;
                    case 2: //wanna help
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://youtube.com/gabrielemusco")));
                        break;
                    case 3: //license
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://www.apache.org/licenses/")));
                        break;
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }
}
