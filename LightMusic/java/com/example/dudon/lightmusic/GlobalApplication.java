package com.example.dudon.lightmusic;

import android.app.Application;
import android.content.Context;


import java.util.List;

/**
 * Created by dudon on 2016/4/25.
 */
public class GlobalApplication extends Application {

    private static Context context;             //全局Context
    private static List<Music> musicList;       //音乐列表
    private static List<Music> favorList;       //喜爱列表
    private static List<Integer> historyList;   //历史纪录


    public static List<Music> getFavorList() {

        return favorList;
    }

    public static void setFavorList(List<Music> favorList) {
        GlobalApplication.favorList = favorList;
    }

    public static List<Integer> getHistoryList() {
        return historyList;
    }

    public static void setHistoryList(List<Integer> historyList) {
        GlobalApplication.historyList = historyList;
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

    public static List<Music> getMusicList() {
        return musicList;
    }

    public static void setMusicList(List<Music> list) {
        musicList = list;
    }

}
