package com.kekebox.hukewei.javlibraryapp;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * Created by hukewei on 02/05/15.
 */
public class SearchResultsActivity extends ActionBarActivity {

    private static final String TAG = "SearchResultsActivity";
    private static final int MAX_LOAD_IDS = 250;
    private static final int NB_TASK_LOAD_SCROLL = 15;
    private static final int NB_FIRST_LOAD_TASK = 30;
    String Query;
    VideoPictureAdapter mAdapter;
    ArrayList<VideoInfoItem> searchResult;
    ArrayList<String> searchIDsResult;
    ListView myListView;
    private ProgressBar pb;
    private SmoothProgressBar spb;
    private ArrayList<String> searchPendingIDs;
    private ArrayList<String> searchLoadedIDs;
    public int nbFinishedTask = 0;
    public final int nbTotalTask = 3;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.searching));
        searchResult = new ArrayList<>();
        searchIDsResult = new ArrayList<>();
        searchPendingIDs = new ArrayList<>();
        searchLoadedIDs = new ArrayList<>();
        mAdapter = new VideoPictureAdapter(this, searchResult);
        pb = (ProgressBar)findViewById(R.id.video_detail_progress);
        spb = (SmoothProgressBar) findViewById(R.id.smooth_progressbar);
        myListView = ((ListView)findViewById(R.id.video_list));
        myListView.setAdapter(mAdapter);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VideoInfoItem item = searchResult.get(position);
                JavLibApplication.setCurrentVideoItem(item);
                Intent intent = new Intent(SearchResultsActivity.this, VideoDetailActivity.class);
                startActivity(intent);
            }
        });
        ImageView searchIcon = (ImageView)findViewById(R.id.search_icon);
        searchIcon.setColorFilter(getResources().getColor(R.color.silver));
        handleIntent(getIntent());
        myListView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Log.d(TAG, "onLoadMore");
                ArrayList<String> videos_to_load =  getVideoIDs(JavLibApplication.VideoType.All, NB_TASK_LOAD_SCROLL);
                Log.d(TAG,"VIDEOS TO LOADS = " + videos_to_load);
                new VideoDetailMultipleRetrieveTask(SearchResultsActivity.this, videos_to_load, JavLibApplication.VideoType.All, null).execute((Void) null);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            query = processQuery(query);
            Log.d(TAG, "query = " + query);
            Query = query;
            new VideoIDsRetrieveTask(this, getString(R.string.all_videos_feed_url), searchIDsResult, "title").execute((Void) null);
            new VideoIDsRetrieveTask(this, getString(R.string.new_entries_feed_url), searchIDsResult, "title").execute((Void) null);
            new VideoIDsRetrieveTask(this, getString(R.string.new_releases_feed_url), searchIDsResult, "title").execute((Void) null);
        } else if(Intent.ACTION_SEARCH_LONG_PRESS.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
            query = processQuery(query);
            Log.d(TAG, "query = " + query);

            Query = query;
            new VideoIDsRetrieveTask(this, getString(R.string.all_videos_feed_url), searchIDsResult, "actor").execute((Void) null);
            new VideoIDsRetrieveTask(this, getString(R.string.new_entries_feed_url), searchIDsResult, "actor").execute((Void) null);
            new VideoIDsRetrieveTask(this, getString(R.string.new_releases_feed_url), searchIDsResult, "actor").execute((Void) null);

        }
    }

    private String processQuery(String query) {
        query = query.toUpperCase();
        if(isDesignation(query)){
            return query;
        }
        if(isAlpha(query)) {
            return query;
        }

        if (isNumeric(query)) {
            if(query.length() == 1) {
                query = "00" + query;
            } else if (query.length() == 2){
                query = "0" + query;
            }
            return query;
        }

        //remove all whitespaces
        query = query.replaceAll("\\s+","-");
        List<String> list = Arrays.asList(query.split("-"));
        if(list.size() == 2) {
            String numeric_part = list.get(0);
            String alpha_part = list.get(1);
            if(!isNumeric(numeric_part)) {
                numeric_part = list.get(1);
                alpha_part = list.get(0);
            }
            if(numeric_part.length() == 1) {
                numeric_part = "00" + numeric_part;
            } else if (numeric_part.length() == 2){
                numeric_part = "0" + numeric_part;
            }
            return alpha_part + "-" + numeric_part;
        }
        return query;
    }

    public boolean isAlpha(String name) {
        return name.matches("[a-zA-Z]+");
    }

    public boolean isNumeric(String name) {
        return name.matches("[0-9]+");
    }

    public boolean isDesignation(String name) {
        return name.matches("[a-zA-Z]+\\-[0-9]+");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class VideoIDsRetrieveTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "VideoIDsRetrieveTask";
        Context mContext;
        String mFeedURL;
        ArrayList<String> mResultReference;
        String searchType;


        public VideoIDsRetrieveTask(Context context, String req_url, ArrayList<String> result, String type) {
            mContext = context;
            mFeedURL = req_url;
            mResultReference = result;
            searchType = type;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), MainActivity.CONNECTION_TIMEOUT); //Timeout Limit
            HttpResponse response;
            JSONObject json = new JSONObject();
            //SystemClock.sleep(1000);
            if(searchType.equals("actor")) {
                mFeedURL = mFeedURL + "?regex[actor]=/"+ Query +"/&limit="+MAX_LOAD_IDS + "&only_id=1";
            } else {
                mFeedURL = mFeedURL + "?regex[title]=/"+ Query +"/&limit="+MAX_LOAD_IDS + "&only_id=1";
            }

            try {
                HttpGet get = new HttpGet(mFeedURL);
                Log.d(TAG, "query = " + mFeedURL);
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
                                //mResultReference.clear();
                                int ub = results.length()>MAX_LOAD_IDS?MAX_LOAD_IDS:results.length();
                                for (int i = 0; i <ub; i++) {
                                    String current_record = results.getJSONObject(i).getString("_id");
                                    mResultReference.add(current_record);
                                    Log.d(TAG, "add one id into the list = " + current_record);
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
            nbFinishedTask++;
            //Things to do when Task finished with success or not
            //mMileAccrualHistoryTask = null;
            if(nbFinishedTask == nbTotalTask) {
                ArrayList<String> videos_to_load = getVideoIDs(JavLibApplication.VideoType.All, NB_FIRST_LOAD_TASK);
                new VideoDetailMultipleRetrieveTask(SearchResultsActivity.this, videos_to_load, JavLibApplication.VideoType.All, searchType).execute((Void) null);
            }
        }

        @Override
        protected void onCancelled() {
            //mMileAccrualHistoryTask = null;
        }
    }

    public class VideoDetailMultipleRetrieveTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "VideoDetailRetrieveTask";
        Context mContext;
        String mFeedURL;
        JavLibApplication.VideoType mType;
        String mEncodedVideoId;
        ArrayList<String> ids;
        boolean endOfList = false;
        String searchType;


        public VideoDetailMultipleRetrieveTask(Context context,  ArrayList<String> ids, JavLibApplication.VideoType type, String search_type) {
            switch (type) {
                default:
                    mFeedURL = getString(R.string.all_videos_feed_url);
            }
            this.ids = ids;
            mContext = context;
            mType = type;
            searchType = search_type;

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if(ids.isEmpty()) {
                endOfList = true;
                return false;

            }

            runOnUiThread(new Runnable() {
                    public void run() {
                if (spb != null) {
                    spb.setVisibility(View.VISIBLE);
                    spb.progressiveStart();
                }
            }
        });

            mEncodedVideoId = ids.get(0);
            for (int i = 1; i < ids.size(); i++) {
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
                            searchResult.add(currentItem);

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
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    if(spb != null)
                        spb.progressiveStop();
                }
            }, 800);
            mAdapter.notifyDataSetChanged();
            myListView.invalidateViews();

            if (success) {
                for (int i = 0; i < ids.size(); i++) {
                    onLoadSucceed(ids.get(i), mType);
                }


            } else {
                for (int i = 0; i < ids.size(); i++) {
                    onLoadFailed(ids.get(i), mType);
                }
                if(endOfList) {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            if(spb != null)
                                spb.setVisibility(View.GONE);
                        }
                    }, 0);
                    //Toast.makeText(mContext, "暂时没有可以加载的信息了哦！", Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(mContext, "载入失败，请重试！", Toast.LENGTH_SHORT).show();
                }
            }

            new Handler().postDelayed(new Runnable() {

                public void run() {
                    myListView.setVisibility(View.VISIBLE);
                    pb.setVisibility(View.GONE);
                    if(searchResult.isEmpty()) {
                        findViewById(R.id.search_result_empty).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.search_result_empty).setVisibility(View.GONE);
                    }
                }
            }, 0);
            if(searchType != null) {
                if (searchType.equals("actor")) {
                    getSupportActionBar().setTitle(Query + getString(R.string.videos_of_actor));
                } else {
                    getSupportActionBar().setTitle(getString(R.string.search_results));
                }
            }

        }

        @Override
        protected void onCancelled() {
            for (int i = 0; i < ids.size(); i++) {
                onLoadFailed(ids.get(i), mType);
            }
            //mMileAccrualHistoryTask = null;
        }
    }

    public void onLoadSucceed(String video_id, JavLibApplication.VideoType type) {
        switch (type) {
            default:
                searchPendingIDs.remove(video_id);
                searchLoadedIDs.add(video_id);
                //mostWantedIDs.remove(video_id);
                break;
        }
    }

    public void onLoadFailed(String video_id, JavLibApplication.VideoType type) {
        switch (type) {
            default:
                searchPendingIDs.remove(video_id);
                break;
        }
    }

    public ArrayList<String> getVideoIDs(JavLibApplication.VideoType type,int number) {
        ArrayList<String> list_to_load = new ArrayList<>();
        ArrayList<String> pending_pool = null;
        ArrayList<String> loaded_pool = null;
        ArrayList<String> id_pool = null;
        switch (type) {
            default:
                pending_pool = searchPendingIDs;
                id_pool = searchIDsResult;
                loaded_pool = searchLoadedIDs;
                break;
        }
        if(!id_pool.isEmpty())  {
            Log.d(TAG, "ID pool size = " + id_pool.size());
            if(id_pool.size() - loaded_pool.size()<number) {
                number = id_pool.size() - loaded_pool.size();
            }
            Log.d(TAG, "number final = " + number);
            for (int i = 0;  ; i++) {
                if(i == id_pool.size() || list_to_load.size() == number) {
                    break;
                }
                if (!pending_pool.contains(id_pool.get(i)) && !loaded_pool.contains(id_pool.get(i))) {
                    list_to_load.add(id_pool.get(i));
                    pending_pool.add(id_pool.get(i));
                }
            }
        } else {
            Log.d(TAG, "id pool is empty");
        }
        return list_to_load;
    }

}
