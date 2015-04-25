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
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kekebox.hukewei.javlibraryapp.jav.JavLibApplication;
import com.quentindommerc.superlistview.OnMoreListener;
import com.quentindommerc.superlistview.SuperListview;
import com.quentindommerc.superlistview.SwipeDismissListViewTouchListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;


public class BaseVideoFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, OnMoreListener {


    private SuperListview mList;
    private VideoPictureAdapter mAdapter;

    // Declare Variable
    private ArrayList<VideoInfoItem> VideoItemList = new ArrayList<>();
    private Spinner spinner;
    private int filterListResID;

    private int filterIconResID;
    private int Type;
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
        RelativeLayout llLayout    = (RelativeLayout)    inflater.inflate(R.layout.fragment_base_view, container, false);
        // Empty list view demo, just pull to add more items
        mAdapter = new VideoPictureAdapter(getActivity(), VideoItemList);


        // This is what you're looking for
        mList = (SuperListview)llLayout.findViewById(R.id.list);

        type = JavLibApplication.VideoType.valueOf(getArguments().getString(ARG_VIDEO_TYPE));

        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VideoInfoItem item = VideoItemList.get(position);
                JavLibApplication.setCurrentVideoItem(item);
                Intent intent = new Intent(getActivity(), VideoDetailActivity.class);
                startActivity(intent);
            }
        });


        // Setting the refresh listener will enable the refresh progressbar
        mList.setRefreshListener(this);

        // Wow so beautiful
//        mList.setRefreshingColor(getResources().getColor(android.R.color.holo_orange_light),
//                getResources().getColor(android.R.color.holo_blue_light),
//                getResources().getColor(android.R.color.holo_green_light), getResources().getColor(android.R.color.holo_red_light));

        // I want to get loadMore triggered if I see the last item (1)
        mList.setupMoreListener(this, 1);

        mList.setupSwipeToDismiss(new SwipeDismissListViewTouchListener.DismissCallbacks() {
            @Override
            public boolean canDismiss(int position) {
                return true;
            }

            @Override
            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
            }
        }, false);




        return llLayout;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mAdapter.isEmpty()) {
            final ArrayList<String> videos_to_load = ((JavLibApplication) getActivity().getApplication()).getVideoIDs(type, 10);
            for (int i = 0; i < videos_to_load.size(); i++) {
                Handler handler = new Handler();
                final int finalI = i;
                handler.postDelayed(new Runnable() {
                    public void run() {

                        VideoDetailRetrieveTask atask = new VideoDetailRetrieveTask(getActivity(), videos_to_load.get(finalI), type);
                        atask.execute((Void) null);

                    }
                }, 100);

            }
        }
    }

    @Override
    public void onRefresh() {
        Toast.makeText(getActivity(), "Refresh", Toast.LENGTH_LONG).show();

        // enjoy the beaty of the progressbar
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                // demo purpose, adding to the top so you can see it
                //mAdapter.insert("New stuff", 0);

            }
        }, 2000);


    }

    @Override
    public void onMoreAsked(int numberOfItems, int numberBeforeMore, int currentItemPos) {
        ArrayList<String> videos_to_load = ((JavLibApplication) getActivity().getApplication()).getVideoIDs(type, 3);
        for (int i = 0; i < videos_to_load.size(); i++) {
            VideoDetailRetrieveTask atask = new VideoDetailRetrieveTask(getActivity(), videos_to_load.get(i), type);
            atask.execute((Void) null);
        }
    }

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
            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), MainActivity.CONNECTION_TIMEOUT); //Timeout Limit
            HttpResponse response;
            //SystemClock.sleep(1000);

            try {
                HttpGet get = new HttpGet(mFeedURL+ "/" + mVideoId);
                response = client.execute(get);

                /*Checking response */
                if(response!=null && response.getStatusLine().getStatusCode() == 200){
                    String json_string = EntityUtils.toString(response.getEntity());
                    Log.i(TAG, json_string);
                    JSONObject jsonObj = new JSONObject(json_string);
                    final VideoInfoItem currentItem = new VideoInfoItem(jsonObj);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(mAdapter!= null)
                                VideoItemList.add(currentItem);
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
            if (success) {

                ((JavLibApplication)getActivity().getApplication()).onLoadSucceed(mVideoId, mType);
            } else {
                ((JavLibApplication)getActivity().getApplication()).onLoadFailed(mVideoId, mType);
                Toast.makeText(mContext,"载入失败，请重试！", Toast.LENGTH_SHORT);
            }

        }

        @Override
        protected void onCancelled() {
            //mMileAccrualHistoryTask = null;
        }
    }

}