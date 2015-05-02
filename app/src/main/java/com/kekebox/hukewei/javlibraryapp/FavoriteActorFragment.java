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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import java.util.ArrayList;
import java.util.Comparator;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;


public class FavoriteActorFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout mSwipeRefreshLayout;
    ListView myListView;
    ProgressBar pb;
    SmoothProgressBar spb;


    private ActorsManageAdapter mAdapter;

    // Declare Variable
    private ArrayList<String> ActorItemList;
    private final static String TAG = "BasicOfferFragment";
    private static final String ARG_VIDEO_TYPE = "section_number";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity faActivity  = (FragmentActivity)    super.getActivity();
        // Replace LinearLayout by the type of the root element of the layout you're trying to load
        LinearLayout llLayout    = (LinearLayout)    inflater.inflate(R.layout.actor_manage_view, container, false);
        pb = (ProgressBar) llLayout.findViewById(R.id.video_detail_progress);
        spb = (SmoothProgressBar) llLayout.findViewById(R.id.smooth_progressbar);
        mSwipeRefreshLayout = (SwipeRefreshLayout) llLayout.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        mAdapter = new ActorsManageAdapter(getActivity());
        myListView = ((ListView)llLayout.findViewById(R.id.video_list));
        myListView.setAdapter(mAdapter);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        myListView.setVisibility(View.VISIBLE);
        pb.setVisibility(View.GONE);
        spb.setVisibility(View.GONE);



        return llLayout;
    }

    @Override
    public void onRefresh() {
        myListView.invalidateViews();
        mAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onResume() {
        Log.d(TAG, "current favorite actors = " + JavUser.getCurrentUser().getFavoriteActors());
        Log.d(TAG, "current notified actors = " + JavUser.getCurrentUser().getNotifiedActorList());
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }



}