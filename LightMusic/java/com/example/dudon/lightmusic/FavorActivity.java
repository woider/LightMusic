package com.example.dudon.lightmusic;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dudon on 2016/5/1.
 */
public class FavorActivity extends Activity implements View.OnClickListener {
    //资源对象
    public static final int COMPLETE = 1;       //播放完毕
    private MediaPlayer mediaPlayer;            //音乐实例
    private static List<Music> favors;          //音乐集合
    private static int lastIndex;               //播放位置
    //控件对象
    private static ListView listView;           //音乐列表
    private LinearLayout backLayout;            //返回按钮
    private LinearLayout wholeLayout;           //循环播放
    private ImageView wholeSign;                //播放标志
    private static TextView wholeTitle;                //播放标题
    private TextView loading;                   //加载列表
    private TextView favorNum;                  //喜爱数量

    //工具对象
    private static boolean quit = false;        //是否退出
    private Player player;                      //播放工具
    private FavorAdapter adapter;               //喜爱适配器
    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COMPLETE) {
                lastIndex++;
                if (favors.size() > 0 && lastIndex == favors.size()) {
                    lastIndex = 0;
                }
                changeMusic(lastIndex);
                listView.smoothScrollToPosition(lastIndex);         //自动滚动到当前位置
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favor);
        init();                     //初始化控件
        lastIndex = -1;             //初始化静态变量
        player = new Player();      //获取播放器实例
        favors = new ArrayList<>(); //建立喜爱列表
        mediaPlayer = player.getMediaPlayer();
        /* 子线程加载列表 */
        new Thread(new Runnable() {
            @Override
            public void run() {

                LoadingThread();                                   //子线程加载喜爱列表

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {                            //完成剩余初始化工作
                        initUI();
                    }
                });

            }
        }).start();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (lastIndex == position) {

                    if (mediaPlayer.isPlaying()) {
                        wholeSign.setImageResource(R.mipmap.favor_start);
                    } else {
                        wholeSign.setImageResource(R.mipmap.favor_pause);
                    }

                } else {
                    wholeSign.setImageResource(R.mipmap.favor_pause);
                }

                player.clickMusic(position);
                lastIndex = position;
                changeMusic(lastIndex);
            }
        });
    }

    /**
     * 初始化控件
     */
    private void init() {
        backLayout = (LinearLayout) findViewById(R.id.back_layout);
        wholeLayout = (LinearLayout) findViewById(R.id.whole_layout);
        wholeSign = (ImageView) findViewById(R.id.whole_sign);
        wholeTitle = (TextView) findViewById(R.id.whole_title);
        listView = (ListView) findViewById(R.id.favor_list);
        loading = (TextView) findViewById(R.id.loading);
        favorNum = (TextView) findViewById(R.id.favor_num);
        backLayout.setOnClickListener(this);
        wholeLayout.setOnClickListener(this);
    }

    /**
     * UI线程初始化
     */
    private void initUI() {
        loading.setVisibility(View.INVISIBLE);
        listView.setVisibility(View.VISIBLE);
        favorNum.setText(favors.size() + "首");       //更新歌曲数量
        player.setMusicList(favors);              //为播放器添加数据源
        adapter = new FavorAdapter(FavorActivity.this, R.layout.favor_item, favors);
        listView.setAdapter(adapter);             //适配列表
    }

    /**
     * 子线程加载喜爱列表
     */
    private void LoadingThread() {
        favors = GlobalApplication.getFavorList();
//        List<Music> musics = GlobalApplication.getMusicList();
//        for (Music music : musics) {
//            favors.add(music);
//        }
    }

    /**
     * 注册点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout: {
                finish();               //结束当前活动
            }
            break;
            case R.id.whole_layout: {   //循环播放全部音乐
                if (lastIndex == -1 && favors.size() > 0) {
                    lastIndex = 0;
                }
                if (favors.size() > 0 && lastIndex >= 0) {
                    listView.performItemClick(listView.getChildAt(lastIndex),
                            lastIndex, listView.getItemIdAtPosition(lastIndex));      //模拟点击列表
                }
            }
            break;
            default:
                break;
        }
    }

    /**
     * 歌曲改变时更新歌曲名称
     */
    private static void changeMusic(int position) {
        wholeTitle.setText(favors.get(position).getName());
    }

    /**
     * Back键拦截，返回桌面
     */
    public void onBackPressed() {
        if (quit == false) {
            Toast.makeText(FavorActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
            alarmAtTime();                          //唤醒计时器
            quit = true;
        } else {
            super.onBackPressed();
            quit = false;
            FavorActivity.this.finish();            //退出当前Activity
        }
    }

    /**
     * 计时器
     */
    private void alarmAtTime() {
        Intent intent = new Intent(this, AlarmReceiver.class);              //启动接收器
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);  //设置延迟启动
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long delay = System.currentTimeMillis() + 2000;
        manager.set(AlarmManager.RTC_WAKEUP, delay, pi);        //延迟2秒后启动广播接收器
    }

    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //重置退出标记
            quit = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.close();
        }
    }
}
