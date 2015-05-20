package com.gabmus.co2photoeditor;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;


public class WelcomeActivity extends FragmentActivity {


    private static final int NUM_PAGES = 5;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        this.getActionBar().hide();


        mPager = (ViewPager) findViewById(R.id.welcome_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        private final int[] slidesPics = {R.drawable.tut1, R.drawable.tut2, R.drawable.tut3, R.drawable.tut4, -1};

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            WelcomeFragment w = new WelcomeFragment();
            Bundle args = new Bundle();
            args.putInt("currentSlide", slidesPics[position]);
            w.setArguments(args);
            return w;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }


    //fragment
    public static class WelcomeFragment extends Fragment {

        private ImageView imgVw;
        private int slide;

        public WelcomeFragment() {}



        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
            slide= getArguments().getInt("currentSlide");
            imgVw = (ImageView) rootView.findViewById(R.id.imageViewWelcome);
            if (slide==-1) {
                imgVw.setVisibility(View.GONE);
                (rootView.findViewById(R.id.cont_tutorial_complete)).setVisibility(View.VISIBLE);
                (rootView.findViewById(R.id.buttonGoToApp)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getActivity().finish();
                    }
                });
            }
            else {
                imgVw.setImageDrawable(getResources().getDrawable(slide));
            }

            return rootView;
        }
    }
}
