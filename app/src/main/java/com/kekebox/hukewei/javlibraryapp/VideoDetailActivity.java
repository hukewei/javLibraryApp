package com.kekebox.hukewei.javlibraryapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.jmpergar.awesometext.AwesomeTextHandler;
import com.kekebox.hukewei.javlibraryapp.jav.JavLibApplication;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

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
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.placeholder)
                .showImageForEmptyUri(R.drawable.placeholder)
                .showImageOnFail(R.drawable.placeholder)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        if(JavLibApplication.getCurrentVideoItem() == null) {
            finish();
        } else {
            item = JavLibApplication.getCurrentVideoItem();
            designation.setText("识别码：\t" + item.getDesignation());
            title.setText("片名：\t" + item.getTitle());
            release_date.setText("发行：\t" + item.getReleaseDate());
            duration.setText("长度：\t" + item.getDuration() + " 分钟");
            String category = "分类：\t";
            for(int i = 0; i< item.getCategories().size();i++) {
                category+= " "+item.getCategories().get(i);
            }
            categories.setText(category);
            String actor = "演员：\t";
            for(int i = 0; i< item.getActors().size();i++) {
                String isLiked = "";
                String current_actor = item.getActors().get(i);
                if(JavUser.getCurrentUser().getFavoriteActors().contains(current_actor)) {
                    isLiked = "#";
                } else {
                    isLiked = "@";
                }

                actor+= " " + isLiked +item.getActors().get(i);
            }
            actors.setText(actor);

            awesomeTextViewHandler = new AwesomeTextHandler();
            awesomeTextViewHandler
                    .addViewSpanRenderer(HASHTAG_PATTERN, new LikedTagsSpanRenderer())
                    .addViewSpanRenderer(MENTION_PATTERN, new ToLikeSpanRenderer())
                    .setView(actors);

            ImageLoader.getInstance().displayImage(item.getImageUrls(), mImage, options,new SimpleImageLoadingListener() {
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

        }
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
        new PreferenceUpdateTask(JavUser.getCurrentUser().getUserId(), "favorite_actors", action_type,actor,new_text).execute((Void) null);
    }


    public static int dipsToPixels(Context ctx, float dips) {
        final float scale = ctx.getResources().getDisplayMetrics().density;
        int px = (int) (dips * scale + 0.5f);
        return px;
    }

    public class PreferenceUpdateTask extends AsyncTask<Void, Void, Boolean> {

        private static final String TAG = "PreferenceUpdateTask";
        private final String userID;
        private final String PreferenceType;
        private final String ActionType;
        private final String Content;
        private final String Extra;


        PreferenceUpdateTask(String uid, String preference_type, String action_type, String data, String extra) {
            userID = uid;
            PreferenceType = preference_type;
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
                json.put(PreferenceType, Content);
                StringEntity se = new StringEntity( json.toString(),HTTP.UTF_8);
                Log.i(TAG, json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                put.setHeader("Accept-Charset","utf-8");
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
                awesomeTextViewHandler.setText(Extra);
                if(ActionType =="PUSH") {
                    Toast.makeText(VideoDetailActivity.this, "已关注 " + Content, Toast.LENGTH_SHORT).show();
                    JavUser.getCurrentUser().getFavoriteActors().add(Content);

                } else {
                    Toast.makeText(VideoDetailActivity.this, "已取消关注 " + Content, Toast.LENGTH_SHORT).show();
                    JavUser.getCurrentUser().getFavoriteActors().remove(Content);
                }
            } else {
                Toast.makeText(VideoDetailActivity.this, "跟新失败，请稍后再试", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
        }
    }
}
