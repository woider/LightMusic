package com.example.dudon.lightmusic;

import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by dudon on 2016/4/25.
 */
public class AudioInfo {

    //音乐表URI地址
    public static final Uri MUSIC_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    //专辑表URI地址
    public static final Uri ALBUM_URI = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;


    //歌曲名称
    public static final int NAME = 0;
    //演唱歌手
    public static final int SINGER = 1;
    //音乐路径
    public static final int PATH = 2;
    //专辑名称
    public static final int ALBUM = 3;
    //文件大小
    public static final int SIZE = 4;
    //持续时间
    public static final int LENGTH = 5;


    public static final String[] MUSIC_COLUMNS = new String[]{
            MediaStore.Audio.AudioColumns.TITLE,    //音乐名称
            MediaStore.Audio.AudioColumns.ARTIST,   //演唱者信息
            MediaStore.Audio.AudioColumns.DATA,     //音乐路径
            MediaStore.Audio.AudioColumns.ALBUM,    //专辑名称
            MediaStore.Audio.AudioColumns.SIZE,     //音乐文件大小(B)
            MediaStore.Audio.AudioColumns.DURATION //音乐文件时长(ms)
    };

    //专辑标题
    public static final int TITLE = 0;
    //专辑图片
    public static final int IMAGE = 1;

    public static final String[] ALBUM_COLUMNS = new String[]{
            MediaStore.Audio.Albums.ALBUM,          //专辑标题
            MediaStore.Audio.Albums.ALBUM_ART       //专辑图片
    };
}
