package com.kekebox.hukewei.javlibraryapp;

/**
 * Created by hukewei on 25/04/15.
 */

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.kekebox.hukewei.javlibraryapp.jav.JavLibApplication;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;


public class BaseVideoFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout mSwipeRefreshLayout;
    ListView myListView;
    ArrayList<String> pendingList;
    ProgressBar pb;
    int nbTaskOnGoing = 0;
    int nbTaskLoaded = 0;
    static final int NB_FIRST_LOAD_TASK = 20;
    static final int NB_TASK_LOAD_SCROLL = 15;
    SmoothProgressBar spb;



    private VideoPictureAdapter mAdapter;

    // Declare Variable
    private ArrayList<VideoInfoItem> VideoItemList;
    private final static String TAG = "BasicOfferFragment";
    private static final String ARG_VIDEO_TYPE = "section_number";
    private JavLibApplication.VideoType type;


    public static final Comparator sortComparator = new Comparator() {
        @Override
        public int compare(Object lhs, Object rhs) {
            return 1;
        }
    };

    public static BaseVideoFragment newInstance(String video_type) {
        BaseVideoFragment fragment = new BaseVideoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_TYPE, video_type);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity faActivity  = (FragmentActivity)    super.getActivity();
        // Replace LinearLayout by the type of the root element of the layout you're trying to load
        type = JavLibApplication.VideoType.valueOf(getArguments().getString(ARG_VIDEO_TYPE));
        LinearLayout llLayout    = (LinearLayout)    inflater.inflate(R.layout.fragment_base_view, container, false);
        pb = (ProgressBar) llLayout.findViewById(R.id.video_detail_progress);
        spb = (SmoothProgressBar) llLayout.findViewById(R.id.smooth_progressbar);
        spb.setVisibility(View.GONE);
        mSwipeRefreshLayout = (SwipeRefreshLayout) llLayout.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        switch (type) {
            case MostWanted:
                VideoItemList = JavLibApplication.getMostWantedItemList();
                //pendingList = ((JavLibApplication)getActivity().getApplication()).getMostWantedPendingIDs();
                break;
            case BestRated:
                VideoItemList = JavLibApplication.getBestRatedItemList();
                //pendingList = ((JavLibApplication)getActivity().getApplication()).getBestRatedPendingIDs();
                break;
            case NewEntries:
                VideoItemList = JavLibApplication.getNewEntriesItemList();
                //pendingList = ((JavLibApplication)getActivity().getApplication()).getNewEntriesPendingIDs();
                break;
            case NewReleases:
                VideoItemList = JavLibApplication.getNewReleasesItemList();
                //pendingList = ((JavLibApplication)getActivity().getApplication()).getNewReleasesPendingIDs();
                break;
            case FavoriteVideos:
                VideoItemList = JavUser.getCurrentUser().getFavoriteVideosItemList();
                //pendingList = JavUser.getCurrentUser().getFavoriteVideosPendingIDs();
                break;
            case WantedVideos:
                VideoItemList = JavUser.getCurrentUser().getWantedVideosItemList();
                //pendingList = JavUser.getCurrentUser().getWantedVideosPendingIDs();
                break;
            case WatchedVideos:
                VideoItemList = JavUser.getCurrentUser().getWatchedVideosItemList();
                //pendingList = JavUser.getCurrentUser().getWantedVideosPendingIDs();
                break;
        }

        mAdapter = new VideoPictureAdapter(getActivity(), VideoItemList);
        myListView = ((ListView)llLayout.findViewById(R.id.video_list));
        myListView.setAdapter(mAdapter);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VideoInfoItem item = VideoItemList.get(position);
                JavLibApplication.setCurrentVideoItem(item);
                Intent intent = new Intent(getActivity(), VideoDetailActivity.class);
                startActivity(intent);
            }
        });
        myListView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                Log.d(TAG, "onLoadMore");
                final ArrayList<String> videos_to_load = ((JavLibApplication) getActivity().getApplication()).getVideoIDs(type, NB_TASK_LOAD_SCROLL);
                Log.d(TAG,"VIDEOS TO LOADS = " + videos_to_load);
                new VideoDetailMultipleRetrieveTask(getActivity(), videos_to_load, type, false).execute((Void) null);
//                ArrayList<String> videos_to_load = ((JavLibApplication) getActivity().getApplication()).getVideoIDs(type, NB_TASK_LOAD_SCROLL);
//                for (int i = 0; i < videos_to_load.size(); i++) {
//                    VideoDetailRetrieveTask atask = new VideoDetailRetrieveTask(getActivity(), videos_to_load.get(i), type);
//                    atask.execute((Void) null);
//                }
                // or customLoadMoreDataFromApi(totalItemsCount);
            }
        });

        if(VideoItemList.isEmpty()) {
            Log.d(TAG, "video item list is empty");
            final ArrayList<String> videos_to_load = ((JavLibApplication) getActivity().getApplication()).getVideoIDs(type, NB_FIRST_LOAD_TASK);
            if(!videos_to_load.isEmpty()) {
                new VideoDetailMultipleRetrieveTask(getActivity(), videos_to_load, type, false).execute((Void) null);
            } else {
                myListView.setVisibility(View.VISIBLE);
                pb.setVisibility(View.GONE);
            }

//            for (int i = 0; i < videos_to_load.size(); i++) {
//                Handler handler = new Handler();
//                final int finalI = i;
//                handler.postDelayed(new Runnable() {
//                    public void run() {
//
//                        VideoDetailRetrieveTask atask = new VideoDetailRetrieveTask(getActivity(), videos_to_load.get(finalI), type);
//                        atask.execute((Void) null);
//
//                    }
//                }, 0);
//
//            }
        } else {
            Log.d(TAG, "item = " + VideoItemList.toString());
            myListView.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);

        }



        return llLayout;
    }

    @Override
    public void onRefresh() {
        if(VideoItemList.isEmpty()) {
            final ArrayList<String> videos_to_load = ((JavLibApplication) getActivity().getApplication()).getVideoIDs(type, NB_FIRST_LOAD_TASK);
            if(!videos_to_load.isEmpty()) {
                new VideoDetailMultipleRetrieveTask(getActivity(), videos_to_load, type, false).execute((Void) null);
            }
        } else {
            switch (type) {
                case MostWanted:
                    new VideoIDsRetrieveTask(getActivity(), getString(R.string.most_wanted_feed_url), type).execute((Void) null);
                    break;
                case NewEntries:
                    new VideoIDsRetrieveTask(getActivity(), getString(R.string.new_entries_feed_url), type).execute((Void) null);
                    break;
                case NewReleases:
                    new VideoIDsRetrieveTask(getActivity(), getString(R.string.new_releases_feed_url), type).execute((Void) null);
                    break;
                case BestRated:
                    new VideoIDsRetrieveTask(getActivity(), getString(R.string.best_rated_feed_url), type).execute((Void) null);
                    break;
                default:
                    mSwipeRefreshLayout.setRefreshing(false);
                    break;
            }
        }
        myListView.invalidateViews();
        mAdapter.notifyDataSetChanged();
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            public void run() {
//
//                Toast.makeText(getActivity(), "别着急，暂时没有新片哦！", Toast.LENGTH_LONG).show();
//                mSwipeRefreshLayout.setRefreshing(false);
//
//            }
//        }, 2000);
        //mSwipeRefreshLayout.setRefreshing(false);

    }

    public void updateList() {
        ArrayList<String> videos_to_load = ((JavLibApplication) getActivity().getApplication()).getVideoIDs(type, NB_FIRST_LOAD_TASK);
        new VideoDetailMultipleRetrieveTask(getActivity(), videos_to_load, type, false).execute((Void) null);
    }
//
//    @Override
//    public void onMoreAsked(int numberOfItems, int numberBeforeMore, int currentItemPos) {
//        ArrayList<String> videos_to_load = ((JavLibApplication) getActivity().getApplication()).getVideoIDs(type, 3);
//        for (int i = 0; i < videos_to_load.size(); i++) {
//            VideoDetailRetrieveTask atask = new VideoDetailRetrieveTask(getActivity(), videos_to_load.get(i), type);
//            atask.execute((Void) null);
//        }
//    }



    public class VideoDetailMultipleRetrieveTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "VideoDetailRetrieveTask";
        Context mContext;
        String mFeedURL;
        JavLibApplication.VideoType mType;
        String mEncodedVideoId;
        ArrayList<String> ids;
        boolean endOfList = false;
        boolean isAppend;


        public VideoDetailMultipleRetrieveTask(Context context,  ArrayList<String> ids, JavLibApplication.VideoType type, boolean is_append) {
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
            this.ids = ids;
            mContext = context;
            mType = type;
            isAppend = is_append;

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if(getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (spb != null && !isAppend) {
                            spb.setVisibility(View.VISIBLE);
                            spb.progressiveStart();
                        }
                    }
                });
            }
            if(ids.isEmpty()) {
                endOfList = true;
                return false;

            }
            mEncodedVideoId = ids.get(0);
            for (int i = 1; i < ids.size(); i++) {
                mEncodedVideoId += "@" + ids.get(i);
            }
            nbTaskOnGoing++;
            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), MainActivity.CONNECTION_TIMEOUT); //Timeout Limit
            HttpResponse response;

            try {
                Log.d(TAG, "sending "+ mFeedURL+ "/" + mEncodedVideoId);
                HttpGet get = new HttpGet(mFeedURL+ "/" + mEncodedVideoId);
                response = client.execute(get);

                if(response!=null && response.getStatusLine().getStatusCode() == 200){
                    String json_string = EntityUtils.toString(response.getEntity());
                    JSONArray results =  new JSONArray(json_string);
                    if (results.length()>0) {
                        for (int i = 0; i <results.length(); i++) {
                            final VideoInfoItem currentItem = new VideoInfoItem(results.getJSONObject(i));
                            if (isAppend) {
                                VideoItemList.add(0,currentItem);
                            } else {
                                VideoItemList.add(currentItem);
                                Log.d(TAG, "adding " + currentItem);
                            }
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

            if (success) {
                if(isAppend) {
                    Toast.makeText(mContext, "新增" + ids.size() + "部影片。", Toast.LENGTH_SHORT).show();
                }
                for (int i = 0; i < ids.size(); i++) {
                    JavLibApplication.onLoadSucceed(ids.get(i), mType);
                }


            } else {
                for (int i = 0; i < ids.size(); i++) {
                    JavLibApplication.onLoadFailed(ids.get(i), mType);
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
                    if(isAppend) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                }
            }, 50);
            mAdapter.notifyDataSetChanged();
            myListView.invalidateViews();

        }

        @Override
        protected void onCancelled() {
            for (int i = 0; i < ids.size(); i++) {
                JavLibApplication.onLoadFailed(ids.get(i), mType);
            }
            //mMileAccrualHistoryTask = null;
        }
    }


    public class VideoIDsRetrieveTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "VideoIDsRetrieveTask";
        Context mContext;
        String mFeedURL;
        ArrayList<String> mResultReference = new ArrayList<>();


        public VideoIDsRetrieveTask(Context context, String req_url,  JavLibApplication.VideoType type) {
            mContext = context;
            mFeedURL = req_url + "?only_id=1&limit="+SplashScreenActivity.MAX_LOAD_IDS_PER_CATEGORY;
            if(type == JavLibApplication.VideoType.NewEntries || type == JavLibApplication.VideoType.NewReleases) {
                mFeedURL += "&sort[release_date]=-1";
            }
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
                                for (int i = 0; i <SplashScreenActivity.MAX_LOAD_IDS_PER_CATEGORY; i++) {
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
            if(success) {
                ArrayList<String> previous_ids_list = null;
                ArrayList<String> pending_pool = null;
                switch (type) {
                    case MostWanted:
                        previous_ids_list = JavLibApplication.mostWantedIDs;
                        pending_pool = JavLibApplication.mostWantedPendingIDs;
                        break;
                    case NewEntries:
                        previous_ids_list = JavLibApplication.newEntriesIDs;
                        pending_pool = JavLibApplication.newEntriesPendingIDs;
                        break;
                    case NewReleases:
                        previous_ids_list = JavLibApplication.newReleasesIDs;
                        pending_pool = JavLibApplication.newReleasesPendingIDs;
                        break;
                    case BestRated:
                        previous_ids_list = JavLibApplication.bestRatedIDs;
                        pending_pool = JavLibApplication.bestRatedPendingIDs;
                        break;
                }
                mResultReference.removeAll(previous_ids_list);
                if (!mResultReference.isEmpty()) {
                    previous_ids_list.addAll(mResultReference);
                    pending_pool.addAll(mResultReference);
                    new VideoDetailMultipleRetrieveTask(getActivity(), mResultReference, type, true).execute((Void) null);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), "别着急，暂时没有新片哦！", Toast.LENGTH_LONG).show();
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }, 0);
                }
            }
            //Things to do when Task finished with success or not
            //mMileAccrualHistoryTask = null;

        }

        @Override
        protected void onCancelled() {
            //mMileAccrualHistoryTask = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

}