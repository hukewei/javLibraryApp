package com.kekebox.hukewei.javlibraryapp;

/**
 * Created by hukewei on 25/04/15.
 */

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.kekebox.hukewei.javlibraryapp.jav.PreferenceType;
import com.kyleduo.switchbutton.SwitchButton;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by khu on 20/02/2015.
 */
public class ActorsManageAdapter extends BaseSwipeAdapter {
    private Context mContext;
    ArrayList<String> actorItem;
    private static final String TAG = "OPActivityAdapter";
    Animation anim;
    //SwitchButton sb;

    public ActorsManageAdapter(Context mContext) {
        this.mContext = mContext;
        actorItem = JavUser.getCurrentUser().getFavoriteActors();
    }


    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public View generateView(final int position, ViewGroup viewGroup) {
        final View v = LayoutInflater.from(mContext).inflate(R.layout.favorite_actor_item, null);
        final SwipeLayout swipeLayout = (SwipeLayout)v.findViewById(getSwipeLayoutResourceId(position));
        final TextView bigName = (TextView) v.findViewById(R.id.big_name);
        SwitchButton sb = (SwitchButton) v.findViewById(R.id.sb_use_listener);

        swipeLayout.setOnDoubleClickListener(new SwipeLayout.DoubleClickListener() {
            @Override
            public void onDoubleClick(SwipeLayout swipeLayout, boolean b) {
                Intent mainIntent = new Intent(mContext, SearchResultsActivity.class);
                mainIntent.setAction(Intent.ACTION_SEARCH_LONG_PRESS);
                mainIntent.putExtra(SearchManager.QUERY, actorItem.get(position));
                mContext.startActivity(mainIntent);
            }
        });

        swipeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int[] androidColors = mContext.getResources().getIntArray(R.array.androidcolors);
                int randomAndroidColor = androidColors[new Random().nextInt(androidColors.length)];
                bigName.setBackgroundColor(randomAndroidColor);
                return true;
            }
        });
        v.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                anim = AnimationUtils.loadAnimation(
                        mContext, android.R.anim.slide_out_right
                );

                new PreferenceUpdateTask(JavUser.getCurrentUser().getUserId(),
                        PreferenceType.favorite_actors.toString(), "PULL",
                        actorItem.get(position), "", null).execute((Void) null);

                anim.setDuration(1000);
                swipeLayout.startAnimation(anim);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        closeAllItems();
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
            }
        });

        sb.setOnCheckedChangeListener(new checkButtonListener(actorItem.get(position)));

        return v;
    }

    @Override
    public void fillValues(final int position, View view) {
        SwitchButton sb = (SwitchButton) view.findViewById(R.id.sb_use_listener);
        TextView t = (TextView)view.findViewById(R.id.position);
        t.setText((position + 1) + ".");
        TextView name = (TextView)view.findViewById(R.id.text_data);
        name.setText(actorItem.get(position));
        TextView f_name = (TextView)view.findViewById(R.id.big_name);
        f_name.setText(stringAt(actorItem.get(position), 0));
        int[] androidColors = mContext.getResources().getIntArray(R.array.androidcolors);
        int randomAndroidColor = androidColors[new Random().nextInt(androidColors.length)];
        TextView bigName = (TextView) view.findViewById(R.id.big_name);
        bigName.setBackgroundColor(randomAndroidColor);
        sb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
        sb.setChecked(JavUser.getCurrentUser().getNotifiedActorList().contains(actorItem.get(position)));
        sb.setOnCheckedChangeListener(new checkButtonListener(actorItem.get(position)));

    }

    public static String stringAt( String str, int index )
    {
        int codePoint = Character.codePointAt(str, index);
        return new String( Character.toChars(codePoint));
    }

    class checkButtonListener implements CompoundButton.OnCheckedChangeListener {
        String actor;

        checkButtonListener(String actor) {
            this.actor = actor;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String action = null;
            buttonView.setEnabled(false);
            if (isChecked) {
                //get notified for the current actor
                action = "PUSH";
            } else {
                action = "PULL";
            }
            new PreferenceUpdateTask(JavUser.getCurrentUser().getUserId(),
                    PreferenceType.notified_actors.toString(), action,
                    actor, "", (SwitchButton)buttonView).execute((Void) null);
        }
    }

    @Override
    public int getCount() {
        return actorItem.size();
    }

    @Override
    public Object getItem(int position) {
        return actorItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class PreferenceUpdateTask extends AsyncTask<Void, Void, Boolean> {

        private static final String TAG = "PreferenceUpdateTask";
        private final String userID;
        private final String preference_type;
        private final String ActionType;
        private final String Content;
        private final String Extra;
        SwitchButton button;


        PreferenceUpdateTask(String uid, String preference_type, String action_type, String data, String extra, SwitchButton buttonView) {
            userID = uid;
            this.preference_type = preference_type;
            ActionType = action_type;
            Content = data;
            Extra = extra;
            button = buttonView;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), MainActivity.CONNECTION_TIMEOUT); //Timeout Limit
            HttpResponse response;
            JSONObject json = new JSONObject();

            try {

                HttpPut put = new HttpPut(mContext.getString(R.string.preference_url) + userID + "?action="+ActionType);
                json.put(preference_type, Content);
                StringEntity se = new StringEntity( json.toString(), HTTP.UTF_8);
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
                if(preference_type.equals(PreferenceType.favorite_actors.toString())) {

                    Toast.makeText(mContext, "已取消关注 " + Content, Toast.LENGTH_SHORT).show();
                    JavUser.getCurrentUser().getFavoriteActors().removeAll(Arrays.asList(Content));
                    actorItem.removeAll(Arrays.asList(Content));
                    JavUser.getCurrentUser().getNotifiedActorList().removeAll(Arrays.asList(Content));
                    notifyDataSetChanged();
                } else if (preference_type.equals(PreferenceType.notified_actors.toString())) {
                    if(ActionType.equals("PULL")) {
                        if(!Extra.equals("QUITE")) {
                            Toast.makeText(mContext, "已取消推送 " + Content, Toast.LENGTH_SHORT).show();
                        }
                        JavUser.getCurrentUser().getNotifiedActorList().removeAll(Arrays.asList(Content));
                    } else {
                        Toast.makeText(mContext, "已注册推送 " + Content, Toast.LENGTH_SHORT).show();
                        JavUser.getCurrentUser().getNotifiedActorList().add(Content);
                    }
                }
            } else {
                Toast.makeText(mContext, "更新失败，请稍后再试", Toast.LENGTH_SHORT).show();
            }
            if (preference_type.equals("notified_actors")) {
                if(button != null) {
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            button.setEnabled(true);
                            button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                                }
                            });
                            button.setChecked(JavUser.getCurrentUser().getNotifiedActorList().contains(Content));
                            button.setOnCheckedChangeListener(new checkButtonListener(Content));
                        }
                    });
                }

            }
        }

        @Override
        protected void onCancelled() {
        }
    }

}