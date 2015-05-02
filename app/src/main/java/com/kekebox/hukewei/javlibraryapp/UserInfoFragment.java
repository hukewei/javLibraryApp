package com.kekebox.hukewei.javlibraryapp;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.kekebox.hukewei.javlibraryapp.jav.JavLibApplication;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
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

    private NonSwipeableViewPager viewPager;
    private UserTabsPagerAdapter mAdapter;
    private Drawable oldBackground = null;
    private PagerSlidingTabStrip tabs;
    private final Handler handler = new Handler();
    private static boolean isFirstLaunch = true;

    /**
     * Keep track of the preference retrieve task to ensure we can cancel it if requested.
     */
    private PreferenceRetrieveTask mPrefTask = null;
    private boolean taskProcessed = false;
    private static Object monitor = new Object();

    // Tab titles


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity faActivity = (FragmentActivity) super.getActivity();
        new PreferenceRetrieveTask(JavUser.getCurrentUser().getUserId()).execute((Void) null);
        if(isFirstLaunch) {
            while (!taskProcessed) {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        isFirstLaunch = false;
        // Replace LinearLayout by the type of the root element of the layout you're trying to load
        RelativeLayout llLayout = (RelativeLayout) inflater.inflate(R.layout.no_swipe_tab_fragment, container, false);
        // Initilization
        viewPager = (NonSwipeableViewPager)llLayout.findViewById(R.id.pager);
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
        private String clientIdToUpdate = null;

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
                    String client_id = null;
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
                        } else if (keyValue.equals("notified_actors")) {
                            values = JavUser.getCurrentUser().getNotifiedActorList();
                        } else if (keyValue.equals("clientID")) {
                            client_id = jsonObj.getString("clientID");
                            continue;
                        } else {
                            continue;
                        }
                        values.clear();
                        for (int i=0;i<jsonObj.getJSONArray(keyValue).length();i++){
                            Log.d(TAG, "add " + jsonObj.getJSONArray(keyValue).getString(i) );
                            values.add(jsonObj.getJSONArray(keyValue).getString(i));
                        }

                    }
                    SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                    String saved_client_id = sharedPref.getString(("REGISTRATION_ID"), null);
                    Log.d(TAG, "client id from server= " + client_id);
                    Log.d(TAG, "client id saved locally= " + saved_client_id);
                    if( (client_id == null && saved_client_id != null) ||
                            (client_id != null &&  saved_client_id != null && !client_id.equals(saved_client_id))) {
                        //update clientID using saved_client_id
                        clientIdToUpdate = saved_client_id;
                    } else {
                        Log.d(TAG, "client ID not need to update");
                    }
                    taskProcessed = true;
                    synchronized(monitor) {
                        monitor.notifyAll();
                    }
                    Log.i(TAG, json_string);
                    return true;
                } else {
                    taskProcessed = true;
                    synchronized(monitor) {
                        monitor.notifyAll();
                    }
                    return false;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            taskProcessed = true;
            synchronized(monitor) {
                monitor.notifyAll();
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if(clientIdToUpdate != null) {
                new ClientIDUpdateTask(JavUser.getCurrentUser().getUserId(), "clientID", "PUSH", clientIdToUpdate, "").execute((Void) null);
            }
            if (success) {

            } else {
                Toast.makeText(getActivity(), "刷新失败，请稍后再试", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mPrefTask = null;
        }
    }


    public class ClientIDUpdateTask extends AsyncTask<Void, Void, Boolean> {

        private static final String TAG = "PreferenceUpdateTask";
        private final String userID;
        private final String preference_type;
        private final String ActionType;
        private final String Content;
        private final String Extra;


        ClientIDUpdateTask(String uid, String preference_type, String action_type, String data, String extra) {
            userID = uid;
            this.preference_type = preference_type;
            ActionType = action_type;
            Content = data;
            Extra = extra;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), MainActivity.CONNECTION_TIMEOUT); //Timeout Limit
            HttpResponse response;
            JSONObject json = new JSONObject();

            try {

                HttpPut put = new HttpPut(getString(R.string.preference_url) + userID + "?action="+ActionType);
                json.put(preference_type, Content);
                StringEntity se = new StringEntity( json.toString(),HTTP.UTF_8);
                Log.i(TAG, json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                put.setHeader("Accept-Charset", "utf-8");
                put.setEntity(se);
                response = client.execute(put);


                /*Checking response */
                if(response!=null && response.getStatusLine().getStatusCode() == 200){
                    String json_string = EntityUtils.toString(response.getEntity());
                    Log.d(TAG, "response = " + json_string);
                    JSONObject jsonObj = new JSONObject(json_string);
                    Iterator<String> keys= jsonObj.keys();
                    while (keys.hasNext())
                    {
                        String keyValue = keys.next();
                        if(keyValue.equals("_id")) {
                            return true;
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
            if(success) {
                Log.d(TAG, "clientID update success");
            } else {
                Log.d(TAG, "client ID update fail");
            }
        }

        @Override
        protected void onCancelled() {
        }
    }
}
