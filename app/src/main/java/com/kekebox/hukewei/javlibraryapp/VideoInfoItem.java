package com.kekebox.hukewei.javlibraryapp;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by hukewei on 25/04/15.
 */
public class VideoInfoItem {
    private static final String TAG = "VideoInfoItem";
    private String id;
    private ArrayList<String> categories = new ArrayList<>();
    private String designation;
    private String duration;
    private String webUrl;
    private String title;
    private String releaseDate;
    private ArrayList<String> actors = new ArrayList<>();
    private String imageUrls;
    private String imagesThumbUrl;
    private boolean isLiked = false;
    private static final boolean USE_EXTERNAL_THUMBS = true;


/*    {
        "_id": "55396a2c08516809dcc988df",
        "category": [
            "多P",
            "制服",
            "女上位",
            "女优按摩棒"
        ],
        "designation": "EMP-001",
        "title": "EMP-001 部活帰りの美少女",
        "url": "http://www.javlibrary.com/cn/?v=javlipjv6a",
        "release_date": [
            "2010-05-01"
        ],
        "actor": [
            "中野ひなた"
        ],
        "duration": [
            "133"
        ],
        "image_urls": [
            "http://pics.dmm.co.jp/mono/movie/adult/h_446emp001/h_446emp001pl.jpg"
        ],
        "images": [
            {
                "url": "http://pics.dmm.co.jp/mono/movie/adult/h_446emp001/h_446emp001pl.jpg",
                "path": "full/1f9db582278f8af530504df97a622bd25e8da58a.jpg",
                "checksum": "7f751add8cab01a190bb46661aa7e20e"
            }
        ]
    }
*/


    public String getId() {
        return id;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public String getDesignation() {
        return designation;
    }

    public String getDuration() {
        return duration;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public ArrayList<String> getActors() {
        return actors;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public String getImagesThumbUrl() {
        return imagesThumbUrl;
    }

    public VideoInfoItem(JSONObject jso) throws JSONException {
        this.id = jso.getString("_id");
        this.designation = jso.getString("designation");
        this.title = jso.getString("title");
        this.webUrl = jso.getString("url");
        this.releaseDate = jso.getJSONArray("release_date").getString(0);
        this.duration = jso.getJSONArray("duration").getString(0);
        this.imageUrls = jso.getJSONArray("image_urls").getString(0);
        Log.d(TAG, "image url = " + this.imageUrls);
        Log.d(TAG, "web url = " + this.webUrl);
        if(USE_EXTERNAL_THUMBS) {
            this.imagesThumbUrl = this.imageUrls.replace("pl.jpg", "ps.jpg");
        } else {
            this.imagesThumbUrl = jso.getJSONArray("images").getJSONObject(0).getString("path");
            this.imagesThumbUrl = this.imagesThumbUrl.replace("full", "http://vpn.kekebox.com/javlib/small");
        }
        JSONArray categories = jso.getJSONArray("category");
        for(int i = 0, count = categories.length(); i< count; i++)
        {
            try {
                String category = categories.getString(i);
                this.categories.add(category);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        JSONArray actors = jso.getJSONArray("actor");
        for(int i = 0, count = actors.length(); i< count; i++)
        {
            try {
                String actor = actors.getString(i);
                this.actors.add(actor);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
