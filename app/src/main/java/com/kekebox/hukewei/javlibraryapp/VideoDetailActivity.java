package com.kekebox.hukewei.javlibraryapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.kekebox.hukewei.javlibraryapp.jav.JavLibApplication;
import com.koushikdutta.ion.Ion;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

/**
 * Created by hukewei on 25/04/15.
 */
public class VideoDetailActivity extends ActionBarActivity {
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
                actor+= " "+item.getActors().get(i);
            }
            actors.setText(actor);

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
            Toast.makeText(this,item.getId(),Toast.LENGTH_SHORT).show();

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
}
