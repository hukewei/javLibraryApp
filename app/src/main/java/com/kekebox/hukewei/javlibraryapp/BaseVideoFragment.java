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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;


public class BaseVideoFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout mSwipeRefreshLayout;
    ListView myListView;
    ArrayList<String> pendingList;
    ProgressBar pb;
    int nbTaskOnGoing = 0;
    int nbTaskLoaded = 0;
    static final int NB_FIRST_LOAD_TASK = 20;
    static final int NB_TASK_LOAD_SCROLL = 10;



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
        mSwipeRefreshLayout = (SwipeRefreshLayout) llLayout.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        switch (type) {
            case MostWanted:
                VideoItemList = JavLibApplication.getMostWantedItemList();
                pendingList = ((JavLibApplication)getActivity().getApplication()).getMostWantedPendingIDs();
                break;
            case BestRated:
                VideoItemList = JavLibApplication.getBestRatedItemList();
                pendingList = ((JavLibApplication)getActivity().getApplication()).getBestRatedPendingIDs();
                break;
            case NewEntries:
                VideoItemList = JavLibApplication.getNewEntriesItemList();
                pendingList = ((JavLibApplication)getActivity().getApplication()).getNewEntriesPendingIDs();
                break;
            case NewReleases:
                VideoItemList = JavLibApplication.getNewReleasesItemList();
                pendingList = ((JavLibApplication)getActivity().getApplication()).getNewReleasesPendingIDs();
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
                final ArrayList<String> videos_to_load = ((JavLibApplication) getActivity().getApplication()).getVideoIDs(type, NB_TASK_LOAD_SCROLL);
                new VideoDetailMultipleRetrieveTask(getActivity(), videos_to_load, type).execute((Void) null);
//                ArrayList<String> videos_to_load = ((JavLibApplication) getActivity().getApplication()).getVideoIDs(type, NB_TASK_LOAD_SCROLL);
//                for (int i = 0; i < videos_to_load.size(); i++) {
//                    VideoDetailRetrieveTask atask = new VideoDetailRetrieveTask(getActivity(), videos_to_load.get(i), type);
//                    atask.execute((Void) null);
//                }
                // or customLoadMoreDataFromApi(totalItemsCount);
            }
        });

        if(VideoItemList.isEmpty()) {
            final ArrayList<String> videos_to_load = ((JavLibApplication) getActivity().getApplication()).getVideoIDs(type, NB_FIRST_LOAD_TASK);
            new VideoDetailMultipleRetrieveTask(getActivity(), videos_to_load, type).execute((Void) null);

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
            myListView.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);

        }



        return llLayout;
    }

    @Override
    public void onRefresh() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                Toast.makeText(getActivity(), "别着急，暂时没有新片哦！", Toast.LENGTH_LONG).show();
                mSwipeRefreshLayout.setRefreshing(false);

            }
        }, 2000);

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

    public class VideoDetailRetrieveTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "VideoDetailRetrieveTask";
        Context mContext;
        String mFeedURL;
        JavLibApplication.VideoType mType;
        String mVideoId;


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
            }
            mContext = context;
            mType = type;
            mVideoId = id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            nbTaskOnGoing++;
            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), MainActivity.CONNECTION_TIMEOUT); //Timeout Limit
            HttpResponse response;

            try {
                HttpGet get = new HttpGet(mFeedURL+ "/" + mVideoId);
                response = client.execute(get);

                if(response!=null && response.getStatusLine().getStatusCode() == 200){
                    String json_string = EntityUtils.toString(response.getEntity());
                    Log.i(TAG, json_string);
                    JSONObject jsonObj = new JSONObject(json_string);
                    final VideoInfoItem currentItem = new VideoInfoItem(jsonObj);
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            VideoItemList.add(currentItem);
                            mAdapter.notifyDataSetChanged();
                        }
                    });
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
            nbTaskLoaded++;
            if (success) {
                JavLibApplication.onLoadSucceed(mVideoId, mType);
            } else {
                JavLibApplication.onLoadFailed(mVideoId, mType);
                //Toast.makeText(mContext,"载入失败，请重试！", Toast.LENGTH_SHORT).show();
            }
            nbTaskOnGoing--;
            if(NB_FIRST_LOAD_TASK==nbTaskLoaded) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {

                        myListView.setVisibility(View.VISIBLE);
                        pb.setVisibility(View.GONE);

                    }
                }, 2000);
            }

        }

        @Override
        protected void onCancelled() {
            nbTaskLoaded++;
            nbTaskOnGoing--;
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


        public VideoDetailMultipleRetrieveTask(Context context,  ArrayList<String> ids, JavLibApplication.VideoType type) {
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
            pendingList.add(ids.get(0));
            for (int i = 1; i < ids.size(); i++) {
                pendingList.add(ids.get(i));
                mEncodedVideoId += "@" + ids.get(i);
            }
            nbTaskOnGoing++;
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
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                            }
                        });
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
            if (success) {
                for (int i = 0; i < ids.size(); i++) {
                    JavLibApplication.onLoadSucceed(ids.get(i), mType);
                }

            } else {
                for (int i = 0; i < ids.size(); i++) {
                    JavLibApplication.onLoadFailed(ids.get(i), mType);
                }
                if(endOfList) {
                    //Toast.makeText(mContext, "暂时没有可以加载的信息了哦！", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "载入失败，请重试！", Toast.LENGTH_SHORT).show();
                }
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {

                    myListView.setVisibility(View.VISIBLE);
                    pb.setVisibility(View.GONE);

                }
            }, 50);

        }

        @Override
        protected void onCancelled() {
            for (int i = 0; i < ids.size(); i++) {
                JavLibApplication.onLoadFailed(ids.get(i), mType);
            }
            //mMileAccrualHistoryTask = null;
        }
    }

}