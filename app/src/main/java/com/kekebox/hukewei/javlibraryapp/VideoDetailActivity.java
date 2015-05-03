package com.kekebox.hukewei.javlibraryapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.gc.materialdesign.widgets.SnackBar;
import com.github.clans.fab.FloatingActionButton;
import com.jmpergar.awesometext.AwesomeTextHandler;
import com.kekebox.hukewei.javlibraryapp.jav.JavLibApplication;
import com.kekebox.hukewei.javlibraryapp.jav.PreferenceType;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

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
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

/**
 * Created by hukewei on 25/04/15.
 */
public class VideoDetailActivity extends ActionBarActivity {
    private static final String TAG = "VideoDetailActivity";
    ImageView photo;
    TextView title;
    TextView designation;
    TextView release_date;
    TextView duration;
    TextView categories;
    TextView actors;
    FloatingActionButton fabShare;
    FloatingActionButton fabWeb;
    VideoInfoItem item;
    ImageViewTouch mImage;
    private DisplayImageOptions options;
    private static final String MENTION_PATTERN = "(@[\\p{L}0-9-_]+)";
    private static final String HASHTAG_PATTERN = "(#[\\p{L}0-9-_]+)";
    AwesomeTextHandler awesomeTextViewHandler;

    BootstrapButton favoriteVideo;
    BootstrapButton wantedVideo;
    BootstrapButton watchedVideo;
    boolean isFavoriteVideo;
    boolean isWatchedVideo;
    boolean isWantedVideo;

    SnackBar snackbar;
    private String videoId = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //photo = (ImageView) findViewById(R.id.iv_photo);
        mImage = (ImageViewTouch) findViewById(R.id.iv_photo);
        title = (TextView) findViewById(R.id.title);
        designation = (TextView) findViewById(R.id.designation);
        release_date = (TextView) findViewById(R.id.release_date);
        duration = (TextView) findViewById(R.id.duration);
        categories = (TextView) findViewById(R.id.categories);
        actors = (TextView) findViewById(R.id.actors);
        fabWeb = (FloatingActionButton) findViewById(R.id.menu_item_web);
        fabShare = (FloatingActionButton) findViewById(R.id.menu_item_share);
        favoriteVideo = (BootstrapButton) findViewById(R.id.favorite_video);
        wantedVideo = (BootstrapButton) findViewById(R.id.wanted_video);
        watchedVideo = (BootstrapButton) findViewById(R.id.watched_video);





        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.placeholder)
                .showImageForEmptyUri(R.drawable.placeholder)
                .showImageOnFail(R.drawable.placeholder)
                .cacheOnDisk(true)
                .build();

        Intent intent = getIntent();
        videoId = intent.getStringExtra("VideoID");
        if(JavLibApplication.getCurrentVideoItem() == null && videoId == null) {
            Log.d(TAG, "no current video item, finish");
            finish();
        } else {
            if(videoId == null) {

                findViewById(R.id.main_layout).setVisibility(View.VISIBLE);
                findViewById(R.id.load_notification).setVisibility(View.GONE);
                inflateView();
            } else {
                new VideoDetailRetrieveTask(this, videoId, JavLibApplication.VideoType.All).execute((Void) null);
            }
        }

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
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        inflateView();
                    }
                }, 0);

            } else {
                Log.d(TAG, "currentItem is null");
                Toast.makeText(mContext,"载入失败", Toast.LENGTH_SHORT).show();
                finish();
            }

        }

        @Override
        protected void onCancelled() {
            //mMileAccrualHistoryTask = null;
        }
    }

    public void inflateView() {
        item = JavLibApplication.getCurrentVideoItem();
        isFavoriteVideo = JavUser.getCurrentUser().getFavoriteVideos().contains(item.getId());
        isWatchedVideo = JavUser.getCurrentUser().getWatchedVideos().contains(item.getId());
        isWantedVideo = JavUser.getCurrentUser().getWantedVideos().contains(item.getId());

        if (isFavoriteVideo) {
            favoriteVideo.setBootstrapType("danger");
        } else {
            favoriteVideo.setBootstrapType("default");
        }

        if (isWatchedVideo) {
            watchedVideo.setBootstrapType("danger");
        } else {
            watchedVideo.setBootstrapType("default");
        }

        if (isWantedVideo) {
            wantedVideo.setBootstrapType("danger");
        } else {
            wantedVideo.setBootstrapType("default");
        }

        favoriteVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!JavUser.getCurrentUser().isLogin()) {
                    snackbar = new SnackBar(VideoDetailActivity.this, getString(R.string.please_login), "返回", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                    snackbar.show();
                    return;
                }
                String action_type;
                if (!isFavoriteVideo) {
                    //to add favorite action
                    action_type = "PUSH";
                    JavUser.getCurrentUser().getFavoriteVideosItemList().add(item);
                    JavUser.getCurrentUser().getLoadedFavoriteVideos().add(item.getId());
                } else {
                    action_type = "PULL";
                    JavUser.getCurrentUser().getFavoriteVideosItemList().remove(item);
                    JavUser.getCurrentUser().getLoadedFavoriteVideos().remove(item.getId());
                }
                favoriteVideo.setBootstrapButtonEnabled(false);

                new PreferenceUpdateTask(JavUser.getCurrentUser().getUserId(),
                        PreferenceType.favorite_videos.toString(), action_type,
                        item.getId(), "").execute((Void) null);
            }
        });

        watchedVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!JavUser.getCurrentUser().isLogin()) {
                    snackbar = new SnackBar(VideoDetailActivity.this, getString(R.string.please_login), "返回", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                    snackbar.show();
                    return;
                }
                String action_type;
                if (!isWatchedVideo) {
                    //to add favorite action
                    action_type = "PUSH";
                    JavUser.getCurrentUser().getWatchedVideosItemList().add(item);
                    JavUser.getCurrentUser().getLoadedWatchedVideos().add(item.getId());
                } else {
                    action_type = "PULL";
                    JavUser.getCurrentUser().getWatchedVideosItemList().remove(item);
                    JavUser.getCurrentUser().getLoadedWatchedVideos().remove(item.getId());
                }
                watchedVideo.setBootstrapButtonEnabled(false);
                new PreferenceUpdateTask(JavUser.getCurrentUser().getUserId(),
                        PreferenceType.watched_videos.toString(), action_type,
                        item.getId(), "").execute((Void) null);
            }
        });

        wantedVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!JavUser.getCurrentUser().isLogin()) {
                    snackbar = new SnackBar(VideoDetailActivity.this, getString(R.string.please_login), "返回", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                    snackbar.show();
                    //Toast.makeText(VideoDetailActivity.this, "该功能仅注册用户可用", Toast.LENGTH_SHORT).show();
                    return;
                }
                String action_type;
                if (!isWantedVideo) {
                    //to add favorite action
                    action_type = "PUSH";
                    JavUser.getCurrentUser().getWantedVideosItemList().add(item);
                    JavUser.getCurrentUser().getLoadedWantedVideos().add(item.getId());
                } else {
                    action_type = "PULL";
                    JavUser.getCurrentUser().getWantedVideosItemList().remove(item);
                    JavUser.getCurrentUser().getLoadedWantedVideos().remove(item.getId());
                }
                wantedVideo.setBootstrapButtonEnabled(false);
                new PreferenceUpdateTask(JavUser.getCurrentUser().getUserId(),
                        PreferenceType.wanted_videos.toString(), action_type,
                        item.getId(), "").execute((Void) null);
            }
        });


        getSupportActionBar().setTitle(item.getDesignation());
        designation.setText("识别码：\t" + item.getDesignation());
        title.setText("片名：\t" + item.getTitle());
        release_date.setText("发行：\t" + item.getReleaseDate());
        duration.setText("长度：\t" + item.getDuration() + " 分钟");
        String category = "分类：\t";
        for (int i = 0; i < item.getCategories().size(); i++) {
            category += " " + item.getCategories().get(i);
        }
        categories.setText(category);
        String actor = "演员：\t";
        for (int i = 0; i < item.getActors().size(); i++) {
            String isLiked = "";
            String current_actor = item.getActors().get(i);
            if (JavUser.getCurrentUser().getFavoriteActors().contains(current_actor)) {
                isLiked = "#";
            } else {
                isLiked = "@";
            }

            actor += " " + isLiked + item.getActors().get(i);
        }
        actors.setText(actor);

        awesomeTextViewHandler = new AwesomeTextHandler();
        awesomeTextViewHandler
                .addViewSpanRenderer(HASHTAG_PATTERN, new LikedTagsSpanRenderer())
                .addViewSpanRenderer(MENTION_PATTERN, new ToLikeSpanRenderer())
                .setView(actors);

        ImageLoader.getInstance().displayImage(item.getImageUrls(), mImage, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                findViewById(R.id.pic_progressbar).setVisibility(View.GONE);
            }
        });


        fabWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(item.getWebUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, item.getTitle() + ":" + item.getWebUrl());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "分享至.."));
            }
        });
        findViewById(R.id.main_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.load_notification).setVisibility(View.GONE);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                JavLibApplication.setCurrentVideoItem(null);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class ToLikeSpanRenderer implements AwesomeTextHandler.ViewSpanRenderer, AwesomeTextHandler.ViewSpanClickListener {

        private final static int textSizeInDips = 18;

        @Override
        public View getView(String text, Context context) {
            TextView view = new TextView(context);
            view.setText(text.substring(1));
            view.setBackgroundResource(R.drawable.common_mentions_background);
            view.setTextSize(dipsToPixels(context, textSizeInDips));
            int textColor = Color.parseColor("#a6a6a8");
            view.setTextColor(textColor);
            return view;
        }

        @Override
        public void onClick(String text, Context context) {
            onClickTagAction(text, context);
        }
    }

    class LikedTagsSpanRenderer implements AwesomeTextHandler.ViewSpanRenderer, AwesomeTextHandler.ViewSpanClickListener {

        private final static int textSizeInDips = 18;
        private final static int backgroundResource = R.drawable.common_hashtags_background;
        private final static int textColorResource = android.R.color.white;

        @Override
        public View getView(String text, Context context) {
            TextView view = new TextView(context);
            view.setText(text.substring(1));
            view.setTextSize(dipsToPixels(context, textSizeInDips));
            view.setBackgroundResource(backgroundResource);
            int textColor = context.getResources().getColor(textColorResource);
            view.setTextColor(textColor);
            return view;
        }

        @Override
        public void onClick(String text, Context context) {
            onClickTagAction(text, context);

        }
    }

    private  void onClickTagAction(String text, Context context) {
        if(!JavUser.getCurrentUser().isLogin()) {
            return;
        }
        String new_text = "";
        String action_type;
        String actor;
        if(text.startsWith("#")) {
            //to unliked action
            actor = text.replace("#", "");
            new_text = actors.getText().toString().replace(text, "@"+actor);
            //JavUser.getCurrentUser().getFavoriteActors().remove(actor);
            //Toast.makeText(context, "已取消关注 " + actor, Toast.LENGTH_SHORT).show();
            action_type = "PULL";

        } else {
            //to like action
            actor = text.replace("@", "");
            new_text = actors.getText().toString().replace(text, "#"+actor);
            //JavUser.getCurrentUser().getFavoriteActors().add(actor);
            //Toast.makeText(context, "已关注 " + actor, Toast.LENGTH_SHORT).show();
            action_type = "PUSH";
        }
        awesomeTextViewHandler.setText(new_text);
        new PreferenceUpdateTask(JavUser.getCurrentUser().getUserId(), PreferenceType.favorite_actors.toString(), action_type,actor,new_text).execute((Void) null);
    }


    public static int dipsToPixels(Context ctx, float dips) {
        final float scale = ctx.getResources().getDisplayMetrics().density;
        int px = (int) (dips * scale + 0.5f);
        return px;
    }

    public class PreferenceUpdateTask extends AsyncTask<Void, Void, Boolean> {

        private static final String TAG = "PreferenceUpdateTask";
        private final String userID;
        private final String preference_type;
        private final String ActionType;
        private final String Content;
        private final String Extra;


        PreferenceUpdateTask(String uid, String preference_type, String action_type, String data, String extra) {
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


            if (success) {
                switch(preference_type) {
                    case  "favorite_actors":
                        if(ActionType =="PUSH") {
                            Toast.makeText(VideoDetailActivity.this, "已关注 " + Content, Toast.LENGTH_SHORT).show();
                            JavUser.getCurrentUser().getFavoriteActors().add(Content);

                        } else {
                            Toast.makeText(VideoDetailActivity.this, "已取消关注 " + Content, Toast.LENGTH_SHORT).show();
                            JavUser.getCurrentUser().getFavoriteActors().removeAll(Arrays.asList(Content));
                        }

                        break;
                    case "wanted_videos":
                        wantedVideo.setBootstrapButtonEnabled(true);
                        if(ActionType =="PUSH") {
                            wantedVideo.setBootstrapType("danger");
                            JavUser.getCurrentUser().getWantedVideos().add(Content);

                        } else {
                            wantedVideo.setBootstrapType("default");
                            JavUser.getCurrentUser().getWantedVideos().removeAll(Arrays.asList(Content));
                        }
                        isWantedVideo = JavUser.getCurrentUser().getWantedVideos().contains(item.getId());
                        break;
                    case "favorite_videos":
                        favoriteVideo.setBootstrapButtonEnabled(true);
                        if(ActionType.equals("PUSH")) {
                            favoriteVideo.setBootstrapType("danger");
                            JavUser.getCurrentUser().getFavoriteVideos().add(Content);

                        } else {
                            favoriteVideo.setBootstrapType("default");
                            JavUser.getCurrentUser().getFavoriteVideos().removeAll(Arrays.asList(Content));
                        }
                        isFavoriteVideo = JavUser.getCurrentUser().getFavoriteVideos().contains(item.getId());
                        break;
                    case "watched_videos":
                        watchedVideo.setBootstrapButtonEnabled(true);
                        if(ActionType =="PUSH") {
                            watchedVideo.setBootstrapType("danger");
                            JavUser.getCurrentUser().getWatchedVideos().add(Content);

                        } else {
                            watchedVideo.setBootstrapType("default");
                            JavUser.getCurrentUser().getWatchedVideos().removeAll(Arrays.asList(Content));
                        }
                        isWatchedVideo = JavUser.getCurrentUser().getWatchedVideos().contains(item.getId());
                        break;
                }


            } else {
                Toast.makeText(VideoDetailActivity.this, "更新失败，请稍后再试", Toast.LENGTH_SHORT).show();
            }
        }



        @Override
        protected void onCancelled() {
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        JavLibApplication.setCurrentVideoItem(null);
    }
}
