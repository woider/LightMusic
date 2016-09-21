package com.example.dudon.lightmusic;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by dudon on 2016/5/1.
 */
public class FavorAdapter extends ArrayAdapter<Music> {
    private int resource;

    public FavorAdapter(Context context, int resource, List<Music> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Music favor = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resource, null);
            viewHolder = new ViewHolder();
            viewHolder.favorImage = (ImageView) view.findViewById(R.id.favor_image);
            viewHolder.favorName = (TextView) view.findViewById(R.id.favor_name);
            viewHolder.favorSinger = (TextView) view.findViewById(R.id.favor_singer);
            viewHolder.favorLength = (TextView) view.findViewById(R.id.favor_length);
            view.setTag(viewHolder);    //将ViewHolder存储在View中
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag(); //重新获取ViewHolder
        }
        initMusic(viewHolder, favor);
        return view;
    }


    class ViewHolder {
        ImageView favorImage;
        TextView favorName;
        TextView favorSinger;
        TextView favorLength;
    }

    private void initMusic(ViewHolder viewHolder, Music favor) {
        //设置圆形专辑图片
        viewHolder.favorImage.setImageBitmap(favor.getRound());
        //设置歌曲名和歌手
        viewHolder.favorName.setText(favor.getName());
        viewHolder.favorSinger.setText(favor.getSinger());
        //设置歌曲时长
        viewHolder.favorLength.setText(favor.getTime());
    }
}
