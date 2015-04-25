package com.kekebox.hukewei.javlibraryapp;

/**
 * Created by hukewei on 25/04/15.
 */
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.koushikdutta.ion.Ion;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by khu on 20/02/2015.
 */
public class VideoPictureAdapter extends ArrayAdapter implements Filterable {
    private Context context;
    VideoInfoItem item;

    public static final int HISTORY = 1;
    public static final int FEATURED = 2;
    public static final int SUBSCRIBED = 3;
    public static final int NEARBY = 4;
    private int currentType = 0;
    private List OriginList = new ArrayList();
    private List FilteredList = new ArrayList();
    private static final String TAG = "OPActivityAdapter";


    public VideoPictureAdapter(Context context, List items) {
        super(context, android.R.layout.simple_list_item_1, items);
        this.context = context;
        FilteredList.addAll(items);
        OriginList.addAll(items);

    }

    public VideoPictureAdapter(Context context, List items, int type) {
        super(context, android.R.layout.simple_list_item_1, items);
        this.context = context;
        currentType = type;
        OriginList = items;
        FilteredList = items;
    }


    /**
     * Holder for the list items.
     */
    private static class ViewHolder {
        TextView tvDesignation;
        TextView tvTitle;
        ImageView ivPicture;
    }
    /**
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        item = (VideoInfoItem)getItem(position);

        // This block exists to inflate the settings list item conditionally based on whether
        // we want to support a grid or list view.
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.video_picture_item, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.ivPicture = (ImageView) convertView.findViewById(R.id.picture);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.title);
            viewHolder.tvDesignation = (TextView) convertView.findViewById(R.id.designation);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvTitle.setText(item.getTitle());
        viewHolder.tvDesignation.setText(item.getDesignation());

        ImageLoader.getInstance().displayImage(item.getImagesThumbUrl(), viewHolder.ivPicture);


        return convertView;
    }


    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                ArrayList<VideoInfoItem> tempList=new ArrayList<VideoInfoItem>();
                //constraint is the result from text you want to filter against.
                //objects is your data set you will filter from
                if(constraint != null && OriginList!=null) {
                    Log.d(TAG, "constrain = " + constraint.toString());
                    int upperBound = Integer.valueOf(constraint.toString());
                    Log.d(TAG, "upperBound = " + upperBound);
                    int length=OriginList.size();
                    Log.d(TAG, "OriginList size = " +length);
                    int i=0;
                    while(i<length){
                        VideoInfoItem item= (VideoInfoItem) OriginList.get(i);
                        //do whatever you wanna do here
                        //adding result set output array
                        if(10000 < upperBound) {
                            tempList.add(item);
                        }
                        i++;
                    }
                    //following two lines is very important
                    //as publish result can only take FilterResults objects
                    filterResults.values = tempList;
                    filterResults.count = tempList.size();
                }
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                FilteredList = (ArrayList<VideoInfoItem>) results.values;
                Log.d(TAG, "results counts = " + results.count);
                if (results.count > 0) {
                    Log.d(TAG, "Adding filtered results");
                    clear();
                    addAll(FilteredList);
                    notifyDataSetChanged();
                } else {
                    clear();
                    notifyDataSetInvalidated();
                }
                sort(BaseVideoFragment.sortComparator);
            }
        };
        return filter;
    }

}