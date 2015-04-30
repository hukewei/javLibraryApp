package com.kekebox.hukewei.javlibraryapp;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by hukewei on 25/04/15.
 */
public class UserInfoFragment extends android.support.v4.app.Fragment implements
        ActionBar.TabListener{
    private FragmentTabHost fragmentTabHost;

    private ViewPager viewPager;
    private UserTabsPagerAdapter mAdapter;
    private Drawable oldBackground = null;
    private PagerSlidingTabStrip tabs;
    private final Handler handler = new Handler();

    /**
     * Keep track of the preference retrieve task to ensure we can cancel it if requested.
     */
    private PreferenceRetrieveTask mPrefTask = null;

    // Tab titles


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity faActivity = (FragmentActivity) super.getActivity();
        new PreferenceRetrieveTask(JavUser.getCurrentUser().getUserId()).execute((Void) null);
        // Replace LinearLayout by the type of the root element of the layout you're trying to load
        RelativeLayout llLayout = (RelativeLayout) inflater.inflate(R.layout.tab_fragment, container, false);
        // Initilization
        viewPager = (ViewPager)llLayout.findViewById(R.id.pager);
        mAdapter = new UserTabsPagerAdapter(getActivity().getSupportFragmentManager());

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
        tabs.setShouldExpand(false);
        tabs.setDividerColor(Color.TRANSPARENT);
        tabs.setBackgroundColor(Color.WHITE);
        tabs.setIndicatorColor(getResources().getColor(R.color.fuchsia));
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

    public class PreferenceRetrieveTask extends AsyncTask<Void, Void, Boolean> {

        private static final String TAG = "PreferenceRetrieveTask";
        private final String userID;

        PreferenceRetrieveTask(String id) {
            userID = id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), MainActivity.CONNECTION_TIMEOUT); //Timeout Limit
            HttpResponse response;

            try {
                HttpGet get = new HttpGet(getString(R.string.preference_url) + userID);
                response = client.execute(get);

                /*Checking response */
                if(response!=null && response.getStatusLine().getStatusCode() == 200){
                    String json_string = EntityUtils.toString(response.getEntity());
                    Log.d(TAG, "response = " + json_string);
                    JSONObject jsonObj = new JSONObject(json_string);
                    Iterator<String> keys= jsonObj.keys();
                    while (keys.hasNext())
                    {


                        String keyValue = keys.next();
                        ArrayList<String> values = null;
                        if(keyValue.equals("favorite_actors")) {
                            values = JavUser.getCurrentUser().getFavoriteActors();
                        } else if (keyValue.equals("favorite_videos")) {
                            values = JavUser.getCurrentUser().getFavoriteVideos();
                        } else if (keyValue.equals("wanted_videos")) {
                            values = JavUser.getCurrentUser().getWantedVideos();
                        } else if (keyValue.equals("watched_videos")) {
                            values = JavUser.getCurrentUser().getWatchedVideos();
                        } else {
                            continue;
                        }
                        for (int i=0;i<jsonObj.getJSONArray(keyValue).length();i++){
                            if (values != null) {
                                Log.d(TAG, "add " + jsonObj.getJSONArray(keyValue).getString(i) );
                                values.add(jsonObj.getJSONArray(keyValue).getString(i));
                            }
                        }

                    }
                    Log.i(TAG, json_string);
                } else {
                    return false;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {


            if (success) {
                //mAdapter.notifyDataSetChanged();
                //fixme inform current fragment
            } else {
                Toast.makeText(getActivity(), "刷新失败，请稍后再试", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mPrefTask = null;
        }
    }
}
