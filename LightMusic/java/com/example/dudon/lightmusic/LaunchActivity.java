package com.example.dudon.lightmusic;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dudon on 2016/4/29.
 */

public class LaunchActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //加载启动图片
        setContentView(R.layout.activity_launch);
        //异步加载歌曲数据
        new Thread(new Runnable() {
            @Override
            public void run() {

                /* 加载音乐列表 */
//                List<Music> musics = new Player().searchMusic(getContentResolver());
//                GlobalApplication.setMusicList(musics);

                List<Music> musics = new ArrayList<Music>();
                GlobalApplication.setMusicList(musics);

                /* 加载喜爱列表 */
                List<Music> favors = new ArrayList<Music>();
                DatabaseHelper dbHelper = new DatabaseHelper(LaunchActivity.this, "LightMusic.db", null, 1);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Cursor cursor = db.query("favor", null, null, null, null, null, null);
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    String cname = cursor.getString(cursor.getColumnIndex("name"));
                    String csinger = cursor.getString(cursor.getColumnIndex("singer"));
                    boolean exist = false;
                    for (Music music : musics) {
                        if (music.getName().equals(cname) && music.getSinger().equals(csinger)) {
                            music.favor = true;           //更改喜爱标记
                            favors.add(music);            //添加到喜爱列表中
                            exist = true;
                        }
                    }
                    if (exist == false) {                 //如果音乐列表中没有这项纪录则删除
                        db.delete("favor", "name = ? AND singer = ?", new String[]{cname, csinger});
                    }
                }
                GlobalApplication.setFavorList(favors);

                 cursor = db.query("favor", null, null, null, null, null, null);
                for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
                    String id = cursor.getString(0);
                    String name = cursor.getString(1);
                    String singer = cursor.getString(2);
                    String path = cursor.getString(3);
                    String date = cursor.getString(4);
                    Log.d("woider",id+"\t"+name+"\t"+singer+"\t"+path+"\t"+date);
                }

                //歌曲搜索完毕启动主界面
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(LaunchActivity.this, MusicActivity.class));
                        LaunchActivity.this.finish();
                    }
                });
            }
        }).start();

    }
}
