package com.kekebox.hukewei.javlibraryapp;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.astuetz.PagerSlidingTabStrip;

/**
 * Created by hukewei on 25/04/15.
 */
public class CategoryFragment extends android.support.v4.app.Fragment implements
        ActionBar.TabListener{
    private FragmentTabHost fragmentTabHost;

    private ViewPager viewPager;
    private VideosTabsPagerAdapter mAdapter;
    private Drawable oldBackground = null;
    private PagerSlidingTabStrip tabs;
    private final Handler handler = new Handler();

    // Tab titles


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity faActivity = (FragmentActivity) super.getActivity();
        // Replace LinearLayout by the type of the root element of the layout you're trying to load
        RelativeLayout llLayout = (RelativeLayout) inflater.inflate(R.layout.tab_fragment, container, false);
        // Initilization
        viewPager = (ViewPager)llLayout.findViewById(R.id.pager);
        mAdapter = new VideosTabsPagerAdapter(getActivity().getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        // Bind the tabs to the ViewPager
        tabs = (PagerSlidingTabStrip) llLayout.findViewById(R.id.tabs);
        tabs.setViewPager(viewPager);
        tabs.setShouldExpand(true);
        tabs.setDividerColor(Color.TRANSPARENT);
        tabs.setBackgroundColor(Color.LTGRAY);
        tabs.setIndicatorColor(Color.BLUE);
        return llLayout;
    }
    private Drawable.Callback drawableCallback = new Drawable.Callback() {
        @Override
        public void invalidateDrawable(Drawable who) {
            getActivity().getActionBar().setBackgroundDrawable(who);
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
            handler.postAtTime(what, when);
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
            handler.removeCallbacks(what);
        }
    };

    /*@Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentTabHost = null;
    }*/

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        // on tab selected
        // show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());
    }
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }
}
