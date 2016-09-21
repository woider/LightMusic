package com.example.dudon.lightmusic;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.List;

/**
 * Created by dudon on 2016/4/29.
 */
public class MusicService extends Service {
    //广播地址
    public static final String BCAST_HANDLE = "com.example.lightmusic.LOCAL_BROADCAST_HANDLE";
    //处理事件
    public static final int START = 1;  //播放
    public static final int PAUSE = 2;  //暂停
    public static final int NEXT = 3;   //下一首
    public static final int SEEK = 4;   //定位
    //工具对象
    private MediaPlayer mediaPlayer;
    private List<Music> musics;
    private LocalBroadcastManager broadcastManager;
    private LocalReceiver localReceiver;
    private MusicBinder binder = new MusicBinder();

    class MusicBinder extends Binder {
        public MediaPlayer getMediaPlayer() {
            return mediaPlayer;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer = new MediaPlayer();                //初始化播放器
        musics = GlobalApplication.getMusicList();      //获取音乐列表
        /* 注册广播接收器 */
        localReceiver = new LocalReceiver();    //广播接收器处理操作
        IntentFilter intentFilter = new IntentFilter(); //广播过滤器
        intentFilter.addAction(MusicService.BCAST_HANDLE);     //添加过滤规则
        broadcastManager = LocalBroadcastManager.getInstance(MusicService.this);    //获取广播管理
        broadcastManager.registerReceiver(localReceiver, intentFilter);     //注册广播
        sendProgress();     //发送进度广播
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Intent intent = new Intent(MusicActivity.BCAST_INFO);
                intent.putExtra("tonext", true);
                broadcastManager.sendBroadcast(intent);
            }
        });
        //初始化播放器
        SharedPreferences preferences = getSharedPreferences("data", MODE_PRIVATE);
        int position = preferences.getInt("position", -1);
        String name = preferences.getString("name", "");
        if (position != -1 && musics.size() > position && name.equals(musics.get(position).getName())) {
            try {
                String path = musics.get(position).getPath();
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        broadcastManager.unregisterReceiver(localReceiver);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    /**
     * 发送进度广播
     */
    private void sendProgress() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    if (mediaPlayer == null) break;    //当服务退出后线程结束
                    try {
                        if (mediaPlayer.isPlaying()) {
                            /* 广播发送播放进度 */
                            double position = mediaPlayer.getCurrentPosition();
                            int progress = (int) ((position / mediaPlayer.getDuration()) * 1000);
                            Intent intent = new Intent(MusicActivity.BCAST_INFO);
                            intent.putExtra("progress", progress);
                            broadcastManager.sendBroadcast(intent);
                        }
                    } catch (Exception e) {
                        break;              //线程异常则关闭线程
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    //接收广播操作指令
    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int handle = intent.getIntExtra("handle", 0);
            int position = intent.getIntExtra("position", 0);
            int progress = intent.getIntExtra("progress", 0);
            boolean reset = intent.getBooleanExtra("reset", false);
//            Log.d("woider", "Handle:" + handle + " Position:" + position + " Reset:" + reset);
            if (reset) {
                try {
                    mediaPlayer.reset();
                    String path = musics.get(position).getPath();
                    mediaPlayer.setDataSource(path);        //设置播放源
                    mediaPlayer.prepare();                  //播放器准备
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            switch (handle) {
                case START: {    //开始播放
                    if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                    }
                }
                break;
                case PAUSE: {    //暂停播放
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    }
                }
                break;
                case NEXT: {     //下一曲
                    if (mediaPlayer != null) {
                        mediaPlayer.start();
                    }
                }
                break;
                case SEEK: {     //调整进度
                    if (mediaPlayer != null) {
                        double percent = progress / 1000.0;
                        int current = (int) (mediaPlayer.getDuration() * percent);
                        mediaPlayer.seekTo(current);
                    }
                }
                break;
                default:
                    break;
            }
        }
    }

}
