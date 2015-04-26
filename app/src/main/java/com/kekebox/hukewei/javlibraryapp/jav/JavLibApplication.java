package com.kekebox.hukewei.javlibraryapp.jav;

import android.app.Application;

import com.kekebox.hukewei.javlibraryapp.VideoInfoItem;

import java.util.ArrayList;

/**
 * Created by hukewei on 25/04/15.
 */
public class JavLibApplication extends Application {

    public static enum VideoType {
        MostWanted,
        BestRated,
        NewReleases,
        NewEntries;
    }
    public static ArrayList<String> mostWantedIDs = new ArrayList<>();
    public ArrayList<String> mostWantedOriginIDs = new ArrayList<>();
    public static ArrayList<String> mostWantedPendingIDs= new ArrayList<>();
    public static ArrayList<String> mostWantedLoadedIDs= new ArrayList<>();
    public static ArrayList<String> bestRatedIDs= new ArrayList<>();
    public ArrayList<String> bestRatedOriginIDs= new ArrayList<>();

    public static ArrayList<String> bestRatedPendingIDs= new ArrayList<>();
    public static ArrayList<String> bestRatedLoadedIDs= new ArrayList<>();
    public static ArrayList<String> newReleasesIDs= new ArrayList<>();
    public ArrayList<String> newReleasesOriginIDs= new ArrayList<>();
    public static ArrayList<String> newReleasesPendingIDs= new ArrayList<>();
    public static ArrayList<String> newReleasesLoadedIDs= new ArrayList<>();
    public static ArrayList<String> newEntriesIDs= new ArrayList<>();
    public ArrayList<String> newEntriesOriginIDs= new ArrayList<>();
    public static ArrayList<String> newEntriesPendingIDs= new ArrayList<>();
    public static ArrayList<String> newEntriesLoadedIDs= new ArrayList<>();

    public static ArrayList<VideoInfoItem> mostWantedItemList = new ArrayList<>();
    public static ArrayList<VideoInfoItem> bestRatedItemList = new ArrayList<>();
    public static ArrayList<VideoInfoItem> newReleasesItemList = new ArrayList<>();
    public static ArrayList<VideoInfoItem> newEntriesItemList = new ArrayList<>();


    public static VideoInfoItem currentVideoItem;

    public ArrayList<String> AllIDs;



    public static VideoInfoItem getCurrentVideoItem() {
        return currentVideoItem;
    }

    public static void  setCurrentVideoItem(VideoInfoItem item) {
        currentVideoItem = item;
    }
    public static ArrayList<VideoInfoItem> getNewEntriesItemList() {
        return newEntriesItemList;
    }

    public static void setNewEntriesItemList(ArrayList<VideoInfoItem> newEntriesItemList) {
        JavLibApplication.newEntriesItemList = newEntriesItemList;
    }

    public static ArrayList<VideoInfoItem> getNewReleasesItemList() {
        return newReleasesItemList;
    }

    public static void setNewReleasesItemList(ArrayList<VideoInfoItem> newReleasesItemList) {
        JavLibApplication.newReleasesItemList = newReleasesItemList;
    }

    public static ArrayList<VideoInfoItem> getBestRatedItemList() {
        return bestRatedItemList;
    }

    public static void setBestRatedItemList(ArrayList<VideoInfoItem> bestRatedItemList) {
        JavLibApplication.bestRatedItemList = bestRatedItemList;
    }

    public static ArrayList<VideoInfoItem> getMostWantedItemList() {
        return mostWantedItemList;
    }

    public static void setMostWantedItemList(ArrayList<VideoInfoItem> mostWantedItemList) {
        JavLibApplication.mostWantedItemList = mostWantedItemList;
    }

    public ArrayList<String> getVideoIDs(VideoType type,int number) {
        ArrayList<String> list_to_load = new ArrayList<>();
        ArrayList<String> pending_pool = null;
        ArrayList<String> id_pool = null;
        switch (type) {
            case MostWanted:
                pending_pool = mostWantedPendingIDs;
                id_pool = mostWantedIDs;
                break;
            case BestRated:
                pending_pool = bestRatedPendingIDs;
                id_pool = bestRatedIDs;
                break;
            case NewReleases:
                pending_pool = newReleasesPendingIDs;
                id_pool = newReleasesIDs;
                break;
            case NewEntries:
                pending_pool = newEntriesPendingIDs;
                id_pool = newEntriesIDs;
                break;
        }
        if(!id_pool.isEmpty())  {
            if(id_pool.size()<number) {
                number = id_pool.size();
            }
            for (int i = 0; i <number ; i++) {
                if (!pending_pool.contains(id_pool.get(i))) {
                    list_to_load.add(id_pool.get(i));
                    pending_pool.add(id_pool.get(i));
                }
            }
        }
        return list_to_load;
    }

    public static void onLoadSucceed(String video_id, VideoType type) {
        switch (type) {
            case MostWanted:
                mostWantedPendingIDs.remove(video_id);
                mostWantedLoadedIDs.add(video_id);
                mostWantedIDs.remove(video_id);
                break;
            case BestRated:
                bestRatedPendingIDs.remove(video_id);
                bestRatedLoadedIDs.add(video_id);
                bestRatedIDs.remove(video_id);
                break;
            case NewReleases:
                newReleasesPendingIDs.remove(video_id);
                newReleasesLoadedIDs.add(video_id);
                newReleasesIDs.remove(video_id);
                break;
            case NewEntries:
                newEntriesPendingIDs.remove(video_id);
                newEntriesLoadedIDs.add(video_id);
                newEntriesIDs.remove(video_id);
                break;
        }
    }

    public static void onLoadFailed(String video_id, VideoType type) {
        switch (type) {
            case MostWanted:
                mostWantedPendingIDs.remove(video_id);
                break;
            case BestRated:
                bestRatedPendingIDs.remove(video_id);
                break;
            case NewReleases:
                newReleasesPendingIDs.remove(video_id);
                break;
            case NewEntries:
                newEntriesPendingIDs.remove(video_id);
                break;
        }
    }

    public ArrayList<String> getMostWantedIDs() {
        return mostWantedIDs;
    }

    public void setMostWantedIDs(ArrayList<String> mostWantedIDs) {
        this.mostWantedIDs = mostWantedIDs;
    }

    public ArrayList<String> getMostWantedOriginIDs() {
        return mostWantedOriginIDs;
    }

    public void setMostWantedOriginIDs(ArrayList<String> mostWantedOriginIDs) {
        this.mostWantedOriginIDs = mostWantedOriginIDs;
    }

    public ArrayList<String> getMostWantedPendingIDs() {
        return mostWantedPendingIDs;
    }

    public void setMostWantedPendingIDs(ArrayList<String> mostWantedPendingIDs) {
        this.mostWantedPendingIDs = mostWantedPendingIDs;
    }

    public ArrayList<String> getMostWantedLoadedIDs() {
        return mostWantedLoadedIDs;
    }

    public void setMostWantedLoadedIDs(ArrayList<String> mostWantedLoadedIDs) {
        this.mostWantedLoadedIDs = mostWantedLoadedIDs;
    }

    public ArrayList<String> getBestRatedIDs() {
        return bestRatedIDs;
    }

    public void setBestRatedIDs(ArrayList<String> bestRatedIDs) {
        this.bestRatedIDs = bestRatedIDs;
    }

    public ArrayList<String> getBestRatedOriginIDs() {
        return bestRatedOriginIDs;
    }

    public void setBestRatedOriginIDs(ArrayList<String> bestRatedOriginIDs) {
        this.bestRatedOriginIDs = bestRatedOriginIDs;
    }

    public ArrayList<String> getBestRatedPendingIDs() {
        return bestRatedPendingIDs;
    }

    public void setBestRatedPendingIDs(ArrayList<String> bestRatedPendingIDs) {
        this.bestRatedPendingIDs = bestRatedPendingIDs;
    }

    public ArrayList<String> getBestRatedLoadedIDs() {
        return bestRatedLoadedIDs;
    }

    public void setBestRatedLoadedIDs(ArrayList<String> bestRatedLoadedIDs) {
        this.bestRatedLoadedIDs = bestRatedLoadedIDs;
    }

    public ArrayList<String> getNewReleasesIDs() {
        return newReleasesIDs;
    }

    public void setNewReleasesIDs(ArrayList<String> newReleasesIDs) {
        this.newReleasesIDs = newReleasesIDs;
    }

    public ArrayList<String> getNewReleasesOriginIDs() {
        return newReleasesOriginIDs;
    }

    public void setNewReleasesOriginIDs(ArrayList<String> newReleasesOriginIDs) {
        this.newReleasesOriginIDs = newReleasesOriginIDs;
    }

    public ArrayList<String> getNewReleasesPendingIDs() {
        return newReleasesPendingIDs;
    }

    public void setNewReleasesPendingIDs(ArrayList<String> newReleasesPendingIDs) {
        this.newReleasesPendingIDs = newReleasesPendingIDs;
    }

    public ArrayList<String> getNewReleasesLoadedIDs() {
        return newReleasesLoadedIDs;
    }

    public void setNewReleasesLoadedIDs(ArrayList<String> newReleasesLoadedIDs) {
        this.newReleasesLoadedIDs = newReleasesLoadedIDs;
    }

    public ArrayList<String> getNewEntriesIDs() {
        return newEntriesIDs;
    }

    public void setNewEntriesIDs(ArrayList<String> newEntriesIDs) {
        this.newEntriesIDs = newEntriesIDs;
    }

    public ArrayList<String> getNewEntriesOriginIDs() {
        return newEntriesOriginIDs;
    }

    public void setNewEntriesOriginIDs(ArrayList<String> newEntriesOriginIDs) {
        this.newEntriesOriginIDs = newEntriesOriginIDs;
    }

    public ArrayList<String> getNewEntriesPendingIDs() {
        return newEntriesPendingIDs;
    }

    public void setNewEntriesPendingIDs(ArrayList<String> newEntriesPendingIDs) {
        this.newEntriesPendingIDs = newEntriesPendingIDs;
    }

    public ArrayList<String> getNewEntriesLoadedIDs() {
        return newEntriesLoadedIDs;
    }

    public void setNewEntriesLoadedIDs(ArrayList<String> newEntriesLoadedIDs) {
        this.newEntriesLoadedIDs = newEntriesLoadedIDs;
    }

    public ArrayList<String> getAllIDs() {
        return AllIDs;
    }

    public void setAllIDs(ArrayList<String> allIDs) {
        AllIDs = allIDs;
    }



}
