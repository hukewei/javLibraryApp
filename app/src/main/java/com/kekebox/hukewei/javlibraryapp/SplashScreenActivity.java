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

/**
 * Created by hukewei on 25/04/15.
 */
public class SplashScreenActivity extends Activity {

    private static final int MAX_LOAD_IDS_PER_CATEGORY = 100;
    /** Duration of wait **/
    private int finished_task = 0;
    private int failed_task = 0;
    private int succeed_task = 0;
    private static final int TOTAL_TASK = 4;
    int finishedProgressTask = 0;
    static final int NB_FIRST_LOAD_TASK = 20;
    private static final int PROGRESS_TASK = NB_FIRST_LOAD_TASK;
    boolean loadFinished = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash);
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
        ArrayList<VideoInfoItem> VideoItemList;


        public VideoDetailRetrieveTask(Context context,  String id, JavLibApplication.VideoType type) {
            switch (type) {
                case MostWanted:
                    mFeedURL = getString(R.string.most_wanted_feed_url);
                    VideoItemList = JavLibApplication.getMostWantedItemList();
                    break;
                case BestRated:
                    mFeedURL = getString(R.string.best_rated_feed_url);
                    VideoItemList = JavLibApplication.getBestRatedItemList();
                    break;
                case NewEntries:

                    VideoItemList = JavLibApplication.getNewEntriesItemList();
                    mFeedURL = getString(R.string.new_entries_feed_url);
                    break;
                case NewReleases:
                    VideoItemList = JavLibApplication.getNewReleasesItemList();
                    mFeedURL = getString(R.string.new_releases_feed_url);
                    break;
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
            //SystemClock.sleep(1000);

            try {
                HttpGet get = new HttpGet(mFeedURL+ "/" + mVideoId);
                response = client.execute(get);

                if(response!=null && response.getStatusLine().getStatusCode() == 200){
                    String json_string = EntityUtils.toString(response.getEntity());
                    Log.i(TAG, json_string);
                    JSONObject jsonObj = new JSONObject(json_string);
                    final VideoInfoItem currentItem = new VideoInfoItem(jsonObj);
                    VideoItemList.add(currentItem);
//                    if(!VideoItemList.contains(currentItem)) {
//                        VideoItemList.add(currentItem);
//                    }
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
            finishedProgressTask++;
            DecimalFormat df = new DecimalFormat("#");
            ((TextView)findViewById(R.id.progress_text)).setText(String.valueOf(df.format(100.0 * finishedProgressTask / PROGRESS_TASK)) + " %");
            if (success) {

                JavLibApplication.onLoadSucceed(mVideoId, mType);

            } else {
                JavLibApplication.onLoadFailed(mVideoId, mType);
                //Toast.makeText(mContext,"载入失败，请重试！", Toast.LENGTH_SHORT).show();
            }
            loadFinished = (finishedProgressTask == PROGRESS_TASK);
            if(loadFinished ) {
                Intent mainIntent = new Intent(SplashScreenActivity.this,MainActivity.class);
                SplashScreenActivity.this.startActivity(mainIntent);
                SplashScreenActivity.this.finish();
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
            if(finished_task == TOTAL_TASK) {
                if(succeed_task == TOTAL_TASK) {
                    for (JavLibApplication.VideoType type : JavLibApplication.VideoType.values()) {
                        if(type == JavLibApplication.VideoType.MostWanted) {
                            final ArrayList<String> videos_to_load = ((JavLibApplication) getApplication()).getVideoIDs(type, NB_FIRST_LOAD_TASK);
                            for (int i = 0; i < videos_to_load.size(); i++) {
                                VideoDetailRetrieveTask atask = new VideoDetailRetrieveTask(SplashScreenActivity.this, videos_to_load.get(i), type);
                                atask.execute((Void) null);

                            }
                        }
                    }
                    if(finishedProgressTask == PROGRESS_TASK) {
                        Intent mainIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
                        SplashScreenActivity.this.startActivity(mainIntent);
                        SplashScreenActivity.this.finish();
                    }


                } else {
                    Toast.makeText(mContext,"无法连接到服务器，请稍后再试", Toast.LENGTH_SHORT);
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
}
