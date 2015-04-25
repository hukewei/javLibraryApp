package com.kekebox.hukewei.javlibraryapp;

import android.app.Activity;
import android.content.Intent;
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

        if(JavLibApplication.getCurrentVideoItem() == null) {
            finish();
        } else {
            item = JavLibApplication.getCurrentVideoItem();
            designation.setText("识别码：\t" + item.getDesignation());
            title.setText("片名：\t" + item.getTitle());
            release_date.setText("发行日期：\t" + item.getReleaseDate());
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


            Ion.with(mImage)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .centerCrop()
                    .load(item.getImageUrls());

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
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
