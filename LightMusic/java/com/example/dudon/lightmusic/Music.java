package com.example.dudon.lightmusic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;


/**
 * Created by dudon on 2016/4/25.
 */
public class Music {
    //歌曲状态
    public static final int NONE = 0;   //无状态
    public static final int PLAY = 1;   //播放状态
    public static final int PAUSE = 2;  //暂停状态
    public int state = 0;
    public boolean favor = false;       //喜爱标记
    //歌曲信息
    private String name;    //音乐名称
    private String singer;  //演唱歌手
    private String path;    //文件路径
    private String album;   //音乐专辑
    private String image;   //专辑图片
    private Integer size;   //文件尺寸
    private Integer length; //持续时间
    //自动格式化界面元素
    private String time;    //分:秒 时间
    private Bitmap bitmap;  //专辑图片
    private Bitmap round;   //圆形图片
    //专辑图片属性
    RoundRect roundRect = new RoundRect(300, 300, 150);

    private void setTime(int length) {
        this.time = (length / 1000) / 60 + ":" + (length / 1000) % 60;
    }

    public String getTime() {
        return time;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Bitmap getRound() {
        return round;
    }

    public void setRound(Bitmap bitmap) {
        this.round = roundRect.Transformation(bitmap);
    }

    public void setBitmap(String image) {
        if (TextUtils.isEmpty(image)) {
            bitmap = BitmapFactory.decodeResource
                    (GlobalApplication.getContext().getResources(), R.drawable.music);
        } else {
            try {
                bitmap = RoundRect.lessenUriImage(image);
            }catch (Exception e){
            bitmap = BitmapFactory.decodeResource
                    (GlobalApplication.getContext().getResources(), R.drawable.music);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
        setBitmap(image);
        setRound(bitmap);
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
        setTime(length);
    }
}
