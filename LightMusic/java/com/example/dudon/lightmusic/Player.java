package com.example.dudon.lightmusic;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

//音乐查找器，传入ContentResolver，返回歌曲集合
public class Player {
    //歌曲列表，完全适配ListView
    private List<Music> musicList = new ArrayList<>();
    private MediaPlayer mediaPlayer = new MediaPlayer();
    //记录播放位置
    private int position = -1;

    public void setMusicList(List<Music> musicList) {
        this.musicList = musicList;
    }

    //查询所有音乐信息并放置在List中
    public List<Music> searchMusic(ContentResolver resolver) {
        Cursor musicCursor = resolver.query(AudioInfo.MUSIC_URI, AudioInfo.MUSIC_COLUMNS, null, null, null, null);
        //循环遍历所有游标
        for (musicCursor.moveToFirst(); !musicCursor.isAfterLast(); musicCursor.moveToNext()) {
            Music music = new Music();
            music.setName(musicCursor.getString(AudioInfo.NAME));
            music.setSinger(musicCursor.getString(AudioInfo.SINGER));
            music.setPath(musicCursor.getString(AudioInfo.PATH));
            music.setAlbum(musicCursor.getString(AudioInfo.ALBUM));
            //查找相匹配的专辑
            Cursor albumCursor = resolver.query(AudioInfo.ALBUM_URI, AudioInfo.ALBUM_COLUMNS,
                    AudioInfo.ALBUM_COLUMNS[AudioInfo.TITLE] + " = ?", new String[]{music.getAlbum()}, null);
            for (albumCursor.moveToFirst(); !albumCursor.isAfterLast(); albumCursor.moveToNext()) {
                String path = albumCursor.getString(AudioInfo.IMAGE);
                if (TextUtils.isEmpty(path)) {
                    music.setImage(new String());
                } else {
                    music.setImage(path);
                }
            }
            albumCursor.close();
            music.setSize(musicCursor.getInt(AudioInfo.SIZE));
            music.setLength(musicCursor.getInt(AudioInfo.LENGTH));
            musicList.add(music);
        }
        musicCursor.close();
        return musicList;
    }




    //ListView点击事件
    public void clickMusic(int index) {
        if (index == position) {
            //如果是暂停状态播放音乐，否则暂停音乐
            if (mediaPlayer.isPlaying()) {
                pauseMusic(index);
            } else {
                playMusic(index);
            }
        } else {
            //循环播放音乐
            resetMusic(index);
            loopMusic(index);
            position = index;
        }
    }

    //播放音乐
    public void playMusic(int index) {
        //判断当前MediaPlayer是否正在播放
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    //暂停播放
    public void pauseMusic(int index) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    //重置状态
    public void resetMusic(int index) {
        //重置MediaPlayer为原始状态
        mediaPlayer.reset();
        //初始化MediaPlayer
        String path = musicList.get(index).getPath();
        initPlayer(path);
    }

    //停止播放
    public void close() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    //循环播放
    public void loopMusic(final int index) {
        position = index;
        resetMusic(index);
        playMusic(index);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                int nextIndex = index + 1;
                if (nextIndex == musicList.size()) {
                    nextIndex = 0;
                }
                //发送完成消息
                sendCompleteMessage();
                //通过递归方式循环播放
                loopMusic(nextIndex);
            }
        });
    }
    //发送音乐播放完成消息
    public void sendCompleteMessage(){
        Message message = new Message();
        message.what = FavorActivity.COMPLETE;
        FavorActivity.handler.sendMessage(message);
    }

    //初始化播放器
    public void initPlayer(String path) {
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //获取媒体实例
    public MediaPlayer getMediaPlayer(){
        return this.mediaPlayer;
    }
}
