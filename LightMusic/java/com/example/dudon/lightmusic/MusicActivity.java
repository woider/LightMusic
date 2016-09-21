package com.example.dudon.lightmusic;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.spec.PSource;

/**
 * Created by dudon on 2016/4/29.
 */
public class MusicActivity extends Activity implements View.OnClickListener {
    public static final int START = 1;  //播放按钮
    public static final int NEXT = 2;   //下一曲按钮
    public static final int CHOOSE = 3; //选择某一曲
    public static final int SHOW = 4;   //展示喜爱数目
    //广播地址
    public static final String BCAST_INFO = "com.example.lightmusic.LOCAL_BROADCAST_INFO";
    //资源对象
    private List<Music> musics = null;  //音乐集合
    private Integer lastIndex = -1;     //播放位置
    private boolean playing = false;    //播放状态
    private List<Integer> history;      //历史纪录
    private boolean saveRecord = true;  //是否记录位置
    //工具对象
    private MusicAdapter adapter;       //音乐适配器
    private ServiceReceiver serviceReceiver;        //服务广播接收器
    private ActivityReceiver activityReceiver;      //活动广播接收器
    private LocalBroadcastManager broadcastManager; //广播管理器

    //控件对象
    private ListView listView;          //音乐列表
    private ProgressBar progressBar;    //播放进度
    private ImageView controlImage;     //歌曲图片
    private TextView controlName;       //歌曲名称
    private TextView controlSinger;     //演唱歌手
    private ImageView controlStart;     //播放控件
    private ImageView controlNext;      //下一曲
    private LinearLayout controlLayout; //切换区域
    private LinearLayout favorLayout;   //跳转区域
    private TextView favorCount;        //喜爱数量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        /* 启动服务 */
        Intent startIntent = new Intent(this, MusicService.class);
        startService(startIntent);
         /* 注册广播接收器 */
        serviceReceiver = new ServiceReceiver();
        activityReceiver = new ActivityReceiver();
        IntentFilter serviceFilter = new IntentFilter();
        IntentFilter activityFilter = new IntentFilter();
        serviceFilter.addAction(MusicActivity.BCAST_INFO);
        activityFilter.addAction(PlayerActivity.BCAST_CLICK);
        broadcastManager = LocalBroadcastManager.getInstance(MusicActivity.this);
        broadcastManager.registerReceiver(serviceReceiver, serviceFilter);
        broadcastManager.registerReceiver(activityReceiver, activityFilter);
        /* 获取上次的信息 */
        SharedPreferences preferences = getSharedPreferences("data", MODE_PRIVATE);
        int position = preferences.getInt("position", -1);
        String name = preferences.getString("name", "");
        String singer = preferences.getString("singer", "");

        init(); //初始化控件
        history = new ArrayList<>();                    //初始化历史纪录
        GlobalApplication.setHistoryList(history);      //传递历史记录
        musics = GlobalApplication.getMusicList();      //获取音乐集合
        favorCount.setText(GlobalApplication.getFavorList().size() + "首");
        adapter = new MusicAdapter(MusicActivity.this, R.layout.music_item, musics);
        listView.setAdapter(adapter);
        clickListItem();    //列表点击事件
        //更新同步播放记录
        if (position != -1 && musics.size() > position && name.equals(musics.get(position).getName())) {
            controlName.setText(name);
            controlSinger.setText(singer);
            lastIndex = position;
        }

    }

    /**
     * 控件初始化
     */
    private void init() {
        listView = (ListView) findViewById(R.id.music_list);
        progressBar = (ProgressBar) findViewById(R.id.control_progress);
        controlImage = (ImageView) findViewById(R.id.control_image);
        controlName = (TextView) findViewById(R.id.control_name);
        controlSinger = (TextView) findViewById(R.id.control_singer);
        controlStart = (ImageView) findViewById(R.id.control_start);
        controlNext = (ImageView) findViewById(R.id.control_next);
        controlLayout = (LinearLayout) findViewById(R.id.control_layout);
        favorLayout = (LinearLayout) findViewById(R.id.favor_layout);
        favorCount = (TextView) findViewById(R.id.favor_count);
        controlStart.setOnClickListener(this);
        controlNext.setOnClickListener(this);
        controlLayout.setOnClickListener(this);
        favorLayout.setOnClickListener(this);
    }

    /**
     * 点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.control_start: {  //点击播放按钮
                if (musics.size() == 0) return;
                if (playing) {          //播放-->暂停
                    playing = false;
                    controlStart.setImageResource(R.mipmap.start);
                    Intent intent = new Intent(MusicService.BCAST_HANDLE);
                    intent.putExtra("handle", MusicService.PAUSE);
                    broadcastManager.sendBroadcast(intent);
                    if (lastIndex >= 0 && lastIndex < musics.size()) {
                        musics.get(lastIndex).state = Music.PAUSE;
                    }
                } else {                //暂停-->播放
                    playing = true;
                    controlStart.setImageResource(R.mipmap.pause);
                    Intent intent = new Intent(MusicService.BCAST_HANDLE);
                    intent.putExtra("handle", MusicService.START);
                    broadcastManager.sendBroadcast(intent);
                    if (lastIndex >= 0 && lastIndex < musics.size()) {
                        musics.get(lastIndex).state = Music.PLAY;
                    }
                }
            }
            break;
            case R.id.control_next: {       //播放下一曲
                if (musics.size() == 0) return;
                if (lastIndex >= 0 && lastIndex < musics.size()) {
                    musics.get(lastIndex).state = Music.NONE;
                }
                playing = true;             //直接播放
                lastIndex++;                //移动播放位置
                if (lastIndex == musics.size()) {
                    lastIndex = 0;
                }
                controlStart.setImageResource(R.mipmap.pause);
                changeMusic(lastIndex);     //更新状态栏
                Intent intent = new Intent(MusicService.BCAST_HANDLE);
                intent.putExtra("reset", true);
                intent.putExtra("position", lastIndex);
                intent.putExtra("handle", MusicService.NEXT);
                broadcastManager.sendBroadcast(intent);
//                listView.smoothScrollToPosition(lastIndex);     //滚动到当前位置
                if (saveRecord) {
                    history.add(new Integer(lastIndex));            //存储播放记录
                } else {
                    saveRecord = true;
                }
                if (lastIndex >= 0 && lastIndex < musics.size()) {
                    musics.get(lastIndex).state = Music.PLAY;
                }
            }
            break;
            case R.id.control_layout: {      //切换至播放界面
                Intent intent = new Intent(MusicActivity.this, PlayerActivity.class);
                int progress = progressBar.getProgress();
                intent.putExtra("progress", progress);
                intent.putExtra("position", lastIndex);
                intent.putExtra("playing", playing);
                startActivity(intent);
            }
            break;
            case R.id.favor_layout: {        //跳转至喜爱界面
                if (playing) {
                    controlStart.performClick();        //如果处于播放状态，则暂停播放
                }
                Intent intent = new Intent(MusicActivity.this, FavorActivity.class);
                startActivity(intent);
            }
            break;
            default:
                break;
        }
        adapter.notifyDataSetChanged();     //刷新适配器
    }

    /**
     * 列表点击事件
     */
    private void clickListItem() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == lastIndex) {    //切换状态
                    controlStart.performClick();
                } else {                         //切换歌曲
                    playing = false;
                    if (lastIndex >= 0 && lastIndex < musics.size()) {
                        musics.get(lastIndex).state = Music.NONE;   //重置歌曲状态
                    }
                    lastIndex = position;
                    changeMusic(position);          //更改封面图片...
                    /* 重置播放状态 */
                    Intent intent = new Intent(MusicService.BCAST_HANDLE);
                    intent.putExtra("reset", true);
                    intent.putExtra("position", lastIndex);
                    broadcastManager.sendBroadcast(intent);
                    controlStart.performClick();    //模拟按钮点击事件
                    if (saveRecord) {
                        history.add(new Integer(lastIndex));            //存储播放记录
                    } else {
                        saveRecord = true;
                    }
                }
            }
        });
    }

    /**
     * 更改专辑图片
     *
     * @param position
     */
    private void changeMusic(int position) {
        if (musics.size() > position) {
            String imagePath = musics.get(position).getImage();
            if (!TextUtils.isEmpty(imagePath)) {      //歌曲有图片时更换图片
                Bitmap bitmap = musics.get(position).getBitmap();
                controlImage.setImageBitmap(bitmap);
            } else {
                controlImage.setImageResource(R.drawable.book);
            }
            controlName.setText(musics.get(position).getName());        //设置音乐
            controlSinger.setText(musics.get(position).getSinger());    //设置歌手
        }
    }

    /**
     * 服务广播接收器
     */
    class ServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra("progress", 0);
            boolean tonext = intent.getBooleanExtra("tonext", false);
            progressBar.setProgress(progress);
            if (tonext) {
                controlNext.performClick();     //自动播放下一曲
            }
        }
    }

    /**
     * 活动广播接收器
     */
    class ActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int click = intent.getIntExtra("click", 0);
            int position = intent.getIntExtra("position", 0);
            switch (click) {
                case START: //模拟点击播放按钮
                    controlStart.performClick();
                    break;
                case NEXT:  //模拟点击下一首按钮
                    controlNext.performClick();
                    break;
                case CHOOSE:
                    saveRecord = false;                                             //上一曲不存储记录
                    listView.smoothScrollToPosition(position);
                    listView.performItemClick(listView.getChildAt(position),
                            position, listView.getItemIdAtPosition(position));      //模拟点击列表
                    break;
                case SHOW:
                    favorCount.setText(GlobalApplication.getFavorList().size() + "首");
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 存储歌曲信息以便初始化时调用
     */
    private void saveMusic() {
        //SharePreferences储存
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putInt("position", lastIndex);
        editor.putString("name", musics.get(lastIndex).getName());
        editor.putString("singer", musics.get(lastIndex).getSinger());
        editor.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveMusic();                //储存播放信息
        Intent intent = new Intent(this, MusicService.class);
        stopService(intent);        //关闭服务
    }

    /**
     * 双击返回键退出程序
     */
    private boolean quit = false;   //设置退出标识

    @Override
    public void onBackPressed() {
        Toast toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        if (playing) {        //音乐播放时，询问是否返回桌面

            if (quit == false) {        //询问退出程序
                toast.setText("再按一次返回桌面");
                toast.show();
                new Timer(true).schedule(new TimerTask() {      //启动定时任务
                    @Override
                    public void run() {
                        quit = false;   //重置退出标识
                    }
                }, 2000);               //2秒后运行run()方法
                quit = true;
            } else {                    //确认返回桌面
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                startActivity(homeIntent);
            }


        } else {             //音乐暂停时，询问是否退出程序

            if (quit == false) {        //询问退出程序
                toast.setText("再按一次退出程序");
                toast.show();
                new Timer(true).schedule(new TimerTask() {      //启动定时任务
                    @Override
                    public void run() {
                        quit = false;   //重置退出标识
                    }
                }, 2000);               //2秒后运行run()方法
                quit = true;
            } else {                    //确认退出程序
                super.onBackPressed();
                finish();
            }

        }
    }
}
