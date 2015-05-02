package com.kekebox.hukewei.javlibraryapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.kekebox.hukewei.javlibraryapp.jav.JavLibApplication;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Handler;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by hukewei on 25/04/15.
 */
public class SplashScreenActivity extends Activity {

    private static final int MAX_LOAD_IDS_PER_CATEGORY = 200;
    private static final String TAG = "SplashActivity";
    /** Duration of wait **/
    private int finished_task = 0;
    private int failed_task = 0;
    private int succeed_task = 0;
    private static final int TOTAL_TASK = 5;
    int finishedProgressTask = 0;
    static final int NB_FIRST_LOAD_TASK = 20;
    private static final int PROGRESS_TASK = NB_FIRST_LOAD_TASK;
    boolean loadFinished = false;
    String videoId = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash);
        Intent intent = getIntent();
        videoId = intent.getStringExtra("VideoID");
        if(videoId != null) {
            Log.d(TAG, "video id = "+ videoId);
            new VideoDetailRetrieveTask(this, videoId, JavLibApplication.VideoType.All).execute((Void) null);
        } else {
            Log.d(TAG, "no intent for video id");
        }
        new VideoIDsRetrieveTask(this, getString(R.string.most_wanted_feed_url), ((JavLibApplication)getApplication()).mostWantedIDs).execute((Void) null);
        new VideoIDsRetrieveTask(this, getString(R.string.best_rated_feed_url), ((JavLibApplication)getApplication()).bestRatedIDs).execute((Void) null);
        new VideoIDsRetrieveTask(this, getString(R.string.new_releases_feed_url), ((JavLibApplication)getApplication()).newReleasesIDs).execute((Void) null);
        new VideoIDsRetrieveTask(this, getString(R.string.new_entries_feed_url), ((JavLibApplication)getApplication()).newEntriesIDs).execute((Void) null);




    }

    public class VideoDetailRetrieveTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "VideoDetailRetrieveTask";
        Context mContext;
        String mFeedURL;
        JavLibApplication.VideoType mType;
        String mVideoId;
        VideoInfoItem currentItem = null;


        public VideoDetailRetrieveTask(Context context,  String id, JavLibApplication.VideoType type) {
            switch (type) {
                case MostWanted:
                    mFeedURL = getString(R.string.most_wanted_feed_url);
                    break;
                case BestRated:
                    mFeedURL = getString(R.string.best_rated_feed_url);
                    break;
                case NewEntries:
                    mFeedURL = getString(R.string.new_entries_feed_url);
                    break;
                case NewReleases:
                    mFeedURL = getString(R.string.new_releases_feed_url);
                    break;
                default:
                    mFeedURL = getString(R.string.all_videos_feed_url);
            }
            mContext = context;
            mType = type;
            mVideoId = id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), MainActivity.CONNECTION_TIMEOUT); //Timeout Limit
            HttpResponse response;

            try {
                HttpGet get = new HttpGet(mFeedURL+ "/" + mVideoId);
                response = client.execute(get);

                if(response!=null && response.getStatusLine().getStatusCode() == 200){
                    String json_string = EntityUtils.toString(response.getEntity());
                    Log.i(TAG, json_string);
                    JSONArray jsonObj = new JSONArray(json_string);
                    JSONObject jsob = jsonObj.getJSONObject(0);
                    currentItem = new VideoInfoItem(jsob);
                    return true;

                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            //Things to do when Task finished with success or not
            //mMileAccrualHistoryTask = null;
            if (success && currentItem != null) {
                JavLibApplication.setCurrentVideoItem(currentItem);
            } else {
                Log.d(TAG, "currentItem is null");
                //Toast.makeText(mContext,"载入失败，请重试！", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            //mMileAccrualHistoryTask = null;
        }
    }

    public class VideoIDsRetrieveTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "VideoIDsRetrieveTask";
        Context mContext;
        String mFeedURL;
        ArrayList<String> mResultReference;


        public VideoIDsRetrieveTask(Context context, String req_url, ArrayList<String> result) {
            mContext = context;
            mFeedURL = req_url + "?only_id=1&limit="+MAX_LOAD_IDS_PER_CATEGORY;
            mResultReference = result;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), MainActivity.CONNECTION_TIMEOUT); //Timeout Limit
            HttpResponse response;
            JSONObject json = new JSONObject();
            //SystemClock.sleep(1000);

            try {
                HttpGet get = new HttpGet(mFeedURL);
                Log.i(TAG, json.toString());
                response = client.execute(get);

                /*Checking response */
                if(response!=null && response.getStatusLine().getStatusCode() == 200){
                    String json_string = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObj = new JSONObject(json_string);
                    Iterator<String> keys= jsonObj.keys();
                    while (keys.hasNext())
                    {
                        String keyValue = (String)keys.next();
                        String valueString = jsonObj.getString(keyValue);
                        Log.i(TAG, keyValue);
                        Log.i(TAG, valueString);
                        if (keyValue.equals("results")) {
                            JSONArray results = jsonObj.getJSONArray(keyValue);
                            if (results.length()>0) {
                                mResultReference.clear();
                                for (int i = 0; i <MAX_LOAD_IDS_PER_CATEGORY; i++) {
                                    String current_record = results.getJSONObject(i).getString("_id");
                                    mResultReference.add(current_record);
                                    Log.d(TAG, "add one item into the list = " + current_record);
                                }
                                return true;
                            }
                        }
                    }
                    Log.i(TAG, json_string);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            //Things to do when Task finished with success or not
            //mMileAccrualHistoryTask = null;
            finished_task++;
            if (success) {
                succeed_task++;

            } else {
                failed_task++;
            }
            DecimalFormat df = new DecimalFormat("#");
            ((TextView)findViewById(R.id.progress_text)).setText(String.valueOf(df.format(100.0 * finished_task / TOTAL_TASK)) + " %");
            if(finished_task == TOTAL_TASK -1) {
                if(succeed_task == TOTAL_TASK - 1) {
                    for (JavLibApplication.VideoType type : JavLibApplication.VideoType.values()) {
                        if(type == JavLibApplication.VideoType.MostWanted) {
                            ArrayList<String> videos_to_load = ((JavLibApplication) getApplication()).getVideoIDs(type, NB_FIRST_LOAD_TASK);
                            new VideoDetailMultipleRetrieveTask(SplashScreenActivity.this, videos_to_load, type).execute((Void) null);

                        }
                    }



                } else {
                    //Toast.makeText(mContext,"无法连接到服务器，请稍后再试", Toast.LENGTH_SHORT);
                    findViewById(R.id.progressbar).setVisibility(View.INVISIBLE);
                    ((TextView)findViewById(R.id.progress_text)).setText("无法连接到服务器，请稍后再试");
                }
            }

        }

        @Override
        protected void onCancelled() {
            //mMileAccrualHistoryTask = null;
        }
    }

    public class VideoDetailMultipleRetrieveTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "VideoDetailRetrieveTask";
        private ArrayList<String> pendingList;
        private ArrayList<VideoInfoItem> VideoItemList;
        Context mContext;
        String mFeedURL;
        JavLibApplication.VideoType mType;
        String mEncodedVideoId;
        ArrayList<String> ids;
        boolean endOfList = false;


        public VideoDetailMultipleRetrieveTask(Context context,  ArrayList<String> ids, JavLibApplication.VideoType type) {
            switch (type) {
                case MostWanted:
                    mFeedURL = getString(R.string.most_wanted_feed_url);
                    VideoItemList = JavLibApplication.getMostWantedItemList();
                    pendingList = ((JavLibApplication)getApplication()).getMostWantedPendingIDs();

                    break;
                case BestRated:
                    mFeedURL = getString(R.string.best_rated_feed_url);
                    VideoItemList = JavLibApplication.getBestRatedItemList();
                    pendingList = ((JavLibApplication)getApplication()).getBestRatedPendingIDs();

                    break;
                case NewEntries:

                    VideoItemList = JavLibApplication.getNewEntriesItemList();
                    pendingList = ((JavLibApplication)getApplication()).getNewEntriesPendingIDs();

                    mFeedURL = getString(R.string.new_entries_feed_url);
                    break;
                case NewReleases:
                    VideoItemList = JavLibApplication.getNewReleasesItemList();
                    pendingList = ((JavLibApplication)getApplication()).getNewReleasesPendingIDs();

                    mFeedURL = getString(R.string.new_releases_feed_url);
                    break;
            }

            this.ids = ids;
            mContext = context;
            mType = type;

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if(ids.isEmpty()) {
                endOfList = true;
                return false;

            }
            mEncodedVideoId = ids.get(0);
            //pendingList.add(ids.get(0));
            for (int i = 1; i < ids.size(); i++) {
                //pendingList.add(ids.get(i));
                mEncodedVideoId += "@" + ids.get(i);
            }
            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), MainActivity.CONNECTION_TIMEOUT); //Timeout Limit
            HttpResponse response;

            try {
                HttpGet get = new HttpGet(mFeedURL+ "/" + mEncodedVideoId);
                response = client.execute(get);

                if(response!=null && response.getStatusLine().getStatusCode() == 200){
                    String json_string = EntityUtils.toString(response.getEntity());
                    JSONArray results =  new JSONArray(json_string);
                    if (results.length()>0) {
                        for (int i = 0; i <results.length(); i++) {
                            final VideoInfoItem currentItem = new VideoInfoItem(results.getJSONObject(i));
                            VideoItemList.add(currentItem);

                        }

                        return true;
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            //Things to do when Task finished with success or not
            //mMileAccrualHistoryTask = null;
            finished_task++;
            DecimalFormat df = new DecimalFormat("#");
            ((TextView)findViewById(R.id.progress_text)).setText(String.valueOf(df.format(100.0 * finished_task / TOTAL_TASK)) + " %");
            if (success) {
                for (int i = 0; i < ids.size(); i++) {
                    JavLibApplication.onLoadSucceed(ids.get(i), mType);
                }

            } else {
                for (int i = 0; i < ids.size(); i++) {
                    JavLibApplication.onLoadFailed(ids.get(i), mType);
                }
            }
            Intent mainIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
            if(videoId != null) {
                Bundle myBundle = new Bundle();
                myBundle.putString("VideoID", videoId);
                mainIntent.putExtras(myBundle);

            }
            startActivity(mainIntent);
            finish();


        }

        @Override
        protected void onCancelled() {
            for (int i = 0; i < ids.size(); i++) {
                JavLibApplication.onLoadFailed(ids.get(i), mType);
            }
            //mMileAccrualHistoryTask = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        JPushInterface.onResume(this);
    }

    @Override
    protected  void onPause() {
        super.onPause();
        JPushInterface.onPause(this);
    }



}
