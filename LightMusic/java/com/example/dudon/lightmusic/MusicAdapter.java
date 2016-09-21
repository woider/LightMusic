package com.example.dudon.lightmusic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by dudon on 2016/4/25.
 */
public class MusicAdapter extends ArrayAdapter<Music> {
    private int resource;
    private Context context;
    private int position;

    public MusicAdapter(Context context, int resource, List<Music> objects) {
        super(context, resource, objects);
        this.resource = resource;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Music music = getItem(position);
        this.position = position;
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resource, null);
            viewHolder = new ViewHolder();
            viewHolder.musicNum = (TextView) view.findViewById(R.id.music_num);
            viewHolder.musicName = (TextView) view.findViewById(R.id.music_name);
            viewHolder.musicSinger = (TextView) view.findViewById(R.id.music_singer);
            viewHolder.musicState = (TextView) view.findViewById(R.id.music_state);
            view.setTag(viewHolder);    //将ViewHolder存储在View中
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag(); //重新获取ViewHolder
        }
        initMusic(viewHolder, music);
        return view;
    }

    class ViewHolder {
        TextView musicNum;
        TextView musicName;
        TextView musicSinger;
        TextView musicState;
    }

    private void initMusic(ViewHolder viewHolder, Music music) {
        //设置歌曲编号
        viewHolder.musicNum.setText("" + (position + 1));
        //设置歌曲名称
        viewHolder.musicName.setText(music.getName());
        //设置演唱歌手
        viewHolder.musicSinger.setText(music.getSinger());
        //设置歌曲状态
        switch (music.state) {
            case Music.NONE:
                viewHolder.musicState.setText("");
                break;
            case Music.PLAY:
                viewHolder.musicState.setText("正在播放...");
                break;
            case Music.PAUSE:
                viewHolder.musicState.setText("暂停");
                break;
            default:
                break;
        }
    }
}
