package com.example.dudon.lightmusic;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by dudon on 2016/4/30.
 */
public class PlayerActivity extends Activity implements View.OnClickListener {
    public static final String BCAST_CLICK = "com.example.lightmusic.LOCAL_BROADCAST_CLICK";
    public static final int ROTATE = 1; //播放动画
    //资源对象
    private List<Music> musics;         //音乐列表
    private Integer lastIndex = -1;     //播放位置
    private boolean playing = false;    //播放状态
    private MediaPlayer mediaPlayer;    //播放器
    private MusicService.MusicBinder binder;
    private List<Integer> history;      //历史记录
    //控件对象
    private ImageView albumImage;       //专辑图片
    private TextView titleName;         //歌曲标题
    private TextView titleSinger;       //歌手标题
    private TextView timeBegin;         //开始时间
    private TextView timeEnd;           //结束时间
    private SeekBar seekBar;            //播放进度
    private ImageView playerStart;      //播放按钮
    private Button playerNext;          //下一曲
    private Button playerLast;          //上一曲
    private ImageView favoriteIcon;     //喜爱图标
    //工具对象
    private LocalBroadcastManager broadcastManager;     //广播管理器
    private LocalReceiver localReceiver;                //广播接收器
    private DatabaseHelper dbHelper;                    //数据库工具

    Handler handler = new Handler() {   //动画控制
        static final int RotateDegree = 10;
        int startRotateDegree = 0;
        int endRotateDegree = 0;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ROTATE:
                    endRotateDegree = startRotateDegree + RotateDegree;
                    rotateView(startRotateDegree, endRotateDegree);
                    startRotateDegree = endRotateDegree;
                    break;
                default:
                    break;
            }
        }
    };
    Thread thread = new Thread(new Runnable() { //动画线程
        @Override
        public void run() {
            while (true) {
                if (playing) {
                    Message message = new Message();
                    message.what = ROTATE;
                    handler.sendMessage(message);
                }
                //每100毫秒运行一次
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });
    private ServiceConnection connection = new ServiceConnection() {    //服务绑定工具
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (MusicService.MusicBinder) service;     //获取binder实例
            mediaPlayer = binder.getMediaPlayer();          //获取服务播放器
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        lastIndex = getIntent().getIntExtra("position", 0);             //初始化播放位置
        playing = getIntent().getBooleanExtra("playing", false);        //初始化播放状态
        musics = GlobalApplication.getMusicList();          //获取音乐列表
        history = GlobalApplication.getHistoryList();       //获取历史记录
        dbHelper = new DatabaseHelper(PlayerActivity.this, "LightMusic.db", null, 1);
        init();                 //初始化控件
        changeMusic(lastIndex); //初始化标题
        /* 初始化播放进度 */
        int progress = getIntent().getIntExtra("progress", 0);
        seekBar.setProgress(progress);
        if (lastIndex >= 0 && lastIndex < musics.size()) {
            double percent = progress / 1000.0;       //计算百分比
            int current = (int) (musics.get(lastIndex).getLength() * percent);
            String time = (current / 1000) / 60 + ":" + (current / 1000) % 60;
            timeBegin.setText(time);            //更新播放位置
        }
        if (playing) {            //播放状态显示暂停按钮，暂停状态显示播放按钮
            playerStart.setImageResource(R.mipmap.music_pause);
        } else {
            playerStart.setImageResource(R.mipmap.music_start);
        }
        /* 注册广播接收器 */
        localReceiver = new LocalReceiver();    //广播接收器处理操作
        IntentFilter intentFilter = new IntentFilter(); //广播过滤器
        intentFilter.addAction(MusicActivity.BCAST_INFO);     //添加过滤规则
        broadcastManager = LocalBroadcastManager.getInstance(PlayerActivity.this);
        broadcastManager.registerReceiver(localReceiver, intentFilter);     //注册广播
        /* 绑定服务 */
        Intent bindIntent = new Intent(PlayerActivity.this, MusicService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);    //绑定服务并获取播放权限
        thread.start();         //启动动画线程
        dragSeekBar();          //拖动进度条
    }

    /**
     * 控件初始化
     */
    private void init() {
        albumImage = (ImageView) findViewById(R.id.album_image);
        titleName = (TextView) findViewById(R.id.title_name);
        titleSinger = (TextView) findViewById(R.id.title_singer);
        timeBegin = (TextView) findViewById(R.id.time_begin);
        timeEnd = (TextView) findViewById(R.id.time_end);
        seekBar = (SeekBar) findViewById(R.id.player_seekbar);
        playerStart = (ImageView) findViewById(R.id.player_start);
        playerNext = (Button) findViewById(R.id.player_next);
        playerLast = (Button) findViewById(R.id.player_last);
        favoriteIcon = (ImageView) findViewById(R.id.favorite_icon);
        playerStart.setOnClickListener(this);
        playerNext.setOnClickListener(this);
        playerLast.setOnClickListener(this);
        favoriteIcon.setOnClickListener(this);
    }

    /**
     * 播放旋转动画
     *
     * @param start
     * @param end
     */
    private void rotateView(int start, int end) {
        RotateAnimation animation = new RotateAnimation
                (start, end, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator lin = new LinearInterpolator();
        //设置动画效果，记录状态，持续时间，匀速旋转
        animation.setFillAfter(true);
        animation.setDuration(500);
        animation.setInterpolator(lin);
        albumImage.startAnimation(animation);
    }

    /**
     * 拖动进度条
     */
    private void dragSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.progress = progress;
                /* 计算时间 */
                if (lastIndex >= 0 && lastIndex < musics.size()) {
                    double percent = progress / 1000.0;       //计算百分比
                    int current = (int) (musics.get(lastIndex).getLength() * percent);
                    String time = (current / 1000) / 60 + ":" + (current / 1000) % 60;
                    timeBegin.setText(time);            //更新播放位置
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Intent intent = new Intent(MusicService.BCAST_HANDLE);
                if (mediaPlayer != null) {
                    double percent = progress / 1000.0;
                    int current = (int) (mediaPlayer.getDuration() * percent);
                    mediaPlayer.seekTo(current);
                }
//                intent.putExtra("handle", MusicService.SEEK);
//                intent.putExtra("progress", progress);
//                broadcastManager.sendBroadcast(intent);
            }
        });
    }

    /**
     * 控件点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.player_start: {    //点击播放按钮
                if (musics.size() == 0) {
                    Toast toast = Toast.makeText(this, "空的播放列表", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 250);
                    toast.show();
                    return;
                }
                if (playing) {          //播放-->暂停
                    playing = false;
                    playerStart.setImageResource(R.mipmap.music_start);
                    Intent intent = new Intent(PlayerActivity.BCAST_CLICK);   //发送播放指令
                    intent.putExtra("click", MusicActivity.START);
                    broadcastManager.sendBroadcast(intent);
                    if (lastIndex >= 0 && lastIndex < musics.size()) {
                        musics.get(lastIndex).state = Music.PAUSE;
                    }
                } else {                //暂停-->播放
                    playing = true;
                    playerStart.setImageResource(R.mipmap.music_pause);
                    Intent intent = new Intent(PlayerActivity.BCAST_CLICK);   //发送播放指令
                    intent.putExtra("click", MusicActivity.START);
                    broadcastManager.sendBroadcast(intent);
                    if (lastIndex >= 0 && lastIndex < musics.size()) {
                        musics.get(lastIndex).state = Music.PLAY;
                    }
                }
            }
            break;
            case R.id.player_next: {     //播放下一曲
                if (musics.size() == 0) return;
                if (lastIndex >= 0 && lastIndex < musics.size()) {
                    musics.get(lastIndex).state = Music.NONE;
                }
                playing = true;             //直接播放
                lastIndex++;                //移动播放位置
                if (lastIndex == musics.size()) {
                    lastIndex = 0;
                }
                playerStart.setImageResource(R.mipmap.music_pause);
                changeMusic(lastIndex);     //更新状态栏

                Intent intent = new Intent(PlayerActivity.BCAST_CLICK);
                intent.putExtra("click", MusicActivity.NEXT);
                broadcastManager.sendBroadcast(intent);

            }
            break;
            case R.id.player_last: {        //播放上一曲
                if (musics.size() == 0) return;
                /* 定位上一曲位置 */
                if (history.size() > 0) {
                    int record = history.get(history.size() - 1);        //获取最近位置
                    if (lastIndex == record) {
                        history.remove(history.size() - 1);              //移除当前位置
                        if (history.size() > 0) {
                            record = history.get(history.size() - 1);
                        } else {
                            record = lastIndex - 1;
                            if (record < 0) {
                                record = musics.size() - 1;
                            }
                        }
                    }
                    lastIndex = record;                                 //同步播放位置
                    if (history.size() > 0) {
                        history.remove(history.size() - 1);                 //移除最后位置
                    }
                } else {
                    lastIndex--;
                    if (lastIndex < 0) {
                        lastIndex = musics.size() - 1;
                    }
                }
                playing = true;
                playerStart.setImageResource(R.mipmap.music_pause);
                Intent intent = new Intent(PlayerActivity.BCAST_CLICK);
                intent.putExtra("click", MusicActivity.CHOOSE);
                intent.putExtra("position", lastIndex);
                broadcastManager.sendBroadcast(intent);
                changeMusic(lastIndex);
            }
            break;
            case R.id.favorite_icon: {               //设置喜爱标记
                Music music = musics.get(lastIndex);
                /* 设置喜爱图标 */
                if (music.favor) {
                    music.favor = false;
                    favoriteIcon.setImageResource(R.mipmap.love_a);
                } else {
                    music.favor = true;
                    favoriteIcon.setImageResource(R.mipmap.love_b);
                }
                String name = music.getName();
                String singer = music.getSinger();
                String path = music.getPath();
                /* 数据库操作 */
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                if (music.favor) {          //添加到数据库
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String date = format.format(new Date());
                    ContentValues values = new ContentValues();
                    values.put("name", name);                //组装数据
                    values.put("singer", singer);
                    values.put("path", path);
                    values.put("date", date);
                    db.insert("favor", null, values);         //插入数据库
                    values.clear();
                    GlobalApplication.getFavorList().add(music);        //添加到喜爱列表
                } else {                             //从数据库中删除
                    db.delete("favor", "name = ? AND singer = ?", new String[]{name, singer});
                    GlobalApplication.getFavorList().remove(music);     //从喜爱列表移除
                }
                /* 同步喜爱数目 */
                Intent intent = new Intent(PlayerActivity.BCAST_CLICK);
                intent.putExtra("click", MusicActivity.SHOW);
                broadcastManager.sendBroadcast(intent);
            }
            break;
            default:
                break;
        }
    }

    /**
     * 更新歌曲图片
     *
     * @param position
     */
    private void changeMusic(int position) {
        if (musics.size() == 0) return;
        if (musics.size() > position) {
            String imagePath = musics.get(position).getImage();
            if (!TextUtils.isEmpty(imagePath)) {      //歌曲有图片时更换图片
                Bitmap bitmap = musics.get(position).getRound();
                albumImage.setImageBitmap(bitmap);
            } else {
                albumImage.setImageResource(R.drawable.music);
            }
            if (musics.get(position).favor) {
                favoriteIcon.setImageResource(R.mipmap.love_b);
            } else {
                favoriteIcon.setImageResource(R.mipmap.love_a);
            }
            titleName.setText(musics.get(position).getName());        //设置音乐
            titleSinger.setText(musics.get(position).getSinger());    //设置歌手
            timeBegin.setText("0:0");
            timeEnd.setText(musics.get(position).getTime());          //结束时间
        }

    }

    class LocalReceiver extends BroadcastReceiver {               //自动下一曲
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra("progress", 0);
            boolean tonext = intent.getBooleanExtra("tonext", false);
            seekBar.setProgress(progress);
            if (tonext) {

                if (lastIndex >= 0 && lastIndex < musics.size()) {
                    musics.get(lastIndex).state = Music.NONE;
                }
                playing = true;             //直接播放
                lastIndex++;                //移动播放位置
                if (lastIndex == musics.size()) {
                    lastIndex = 0;
                }

                changeMusic(lastIndex);         //更新歌曲信息
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);  //解绑服务
    }
}
