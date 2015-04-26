package com.kekebox.hukewei.javlibraryapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kekebox.hukewei.javlibraryapp.jav.JavLibApplication;

/**
 * Created by hukewei on 25/04/15.
 */
public class VideosTabsPagerAdapter  extends FragmentStatePagerAdapter {

    public VideosTabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    String[] tabs = {"最想要", "高评分", "新发行", "新上架"};
    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                // Most wanted fragment activity
                return BaseVideoFragment.newInstance(JavLibApplication.VideoType.MostWanted.toString());
            case 1:
                // Best rated fragment activity
                return BaseVideoFragment.newInstance(JavLibApplication.VideoType.BestRated.toString());
            case 2:
                // New Releases fragment activity
                return BaseVideoFragment.newInstance(JavLibApplication.VideoType.NewReleases.toString());
            case 3:
                //  New entries fragment activity
                return BaseVideoFragment.newInstance(JavLibApplication.VideoType.NewEntries.toString());
        }

        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return tabs.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs[position];
    }
}
