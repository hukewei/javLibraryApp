package com.kekebox.hukewei.javlibraryapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kekebox.hukewei.javlibraryapp.jav.JavLibApplication;

/**
 * Created by hukewei on 25/04/15.
 */
public class UserTabsPagerAdapter extends FragmentStatePagerAdapter {

    public UserTabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    //String[] tabs = {"收藏影片", "想看", "已看过"};
    String[] tabs = {"收藏影片", "想看", "已看过", "关注艺人"};
    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                // Most wanted fragment activity
                return BaseVideoFragment.newInstance(JavLibApplication.VideoType.FavoriteVideos.toString());
            case 1:
                // Best rated fragment activity
                return BaseVideoFragment.newInstance(JavLibApplication.VideoType.WantedVideos.toString());
            case 2:
                // New Releases fragment activity
                return BaseVideoFragment.newInstance(JavLibApplication.VideoType.WatchedVideos.toString());
            case 3:
                //  New entries fragment activity
                return new FavoriteActorFragment();
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
