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

/**
 * Created by hukewei on 02/05/15.
 */
public class SearchResultsActivity extends ActionBarActivity {

    private static final String TAG = "SearchResultsActivity";
    private static final int MAX_LOAD_IDS = 150;
    String Query;
    VideoPictureAdapter mAdapter;
    ArrayList<VideoInfoItem> searchResult;
    ListView myListView;
    private ProgressBar pb;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.searching));
        searchResult = new ArrayList<>();
        mAdapter = new VideoPictureAdapter(this, searchResult);
        pb = (ProgressBar)findViewById(R.id.video_detail_progress);
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
            new VideoIDsRetrieveTask(this, getString(R.string.all_videos_feed_url), searchResult, "title").execute((Void) null);
        } else if(Intent.ACTION_SEARCH_LONG_PRESS.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
            query = processQuery(query);
            Log.d(TAG, "query = " + query);
            Query = query;
            new VideoIDsRetrieveTask(this, getString(R.string.all_videos_feed_url), searchResult, "actor").execute((Void) null);
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
        ArrayList<VideoInfoItem> mResultReference;
        String searchType;


        public VideoIDsRetrieveTask(Context context, String req_url, ArrayList<VideoInfoItem> result, String type) {
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
                mFeedURL = mFeedURL + "?regex[actor]=/"+ Query +"/&limit="+MAX_LOAD_IDS;
            } else {
                mFeedURL = mFeedURL + "?regex[title]=/"+ Query +"/&limit="+MAX_LOAD_IDS;
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
                                mResultReference.clear();
                                int ub = results.length()>MAX_LOAD_IDS?MAX_LOAD_IDS:results.length();
                                for (int i = 0; i <ub; i++) {
                                    JSONObject current_record = results.getJSONObject(i);
                                    VideoInfoItem item = new VideoInfoItem(current_record);
                                    mResultReference.add(item);
                                    Log.d(TAG, "add one item into the list = " + current_record.toString());
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
            if (success) {

                mAdapter.notifyDataSetChanged();
                myListView.invalidateViews();

            } else {

            }
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    myListView.setVisibility(View.VISIBLE);
                    pb.setVisibility(View.GONE);
                    if(searchResult.isEmpty()) {
                        findViewById(R.id.search_result_empty).setVisibility(View.VISIBLE);
                    }
                }
            }, 0);
            if(searchType.equals("actor")) {
                getSupportActionBar().setTitle(Query + getString(R.string.videos_of_actor));
            } else {
                getSupportActionBar().setTitle(getString(R.string.search_results));
            }
        }

        @Override
        protected void onCancelled() {
            //mMileAccrualHistoryTask = null;
        }
    }
}
