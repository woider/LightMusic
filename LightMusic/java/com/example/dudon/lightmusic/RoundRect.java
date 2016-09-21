package com.example.dudon.lightmusic;

/**
 * Created by dudon on 2016/4/29.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;


public class RoundRect {

    private int width;
    private int height;
    private float cornerRadius;

    /**
     * 用于初始化圆角矩形基本参数
     *
     * @param width        图片宽度
     * @param height       图片高度
     * @param cornerRadius 圆角半径
     */
    public RoundRect(int width, int height, float cornerRadius) {
        this.width = width;
        this.height = height;
        this.cornerRadius = cornerRadius;
    }

    /**
     * 用于把普通图片转换为圆角矩形图像
     *
     * @param path 图片路径
     * @return output 转换后的圆角矩形图像
     */
    Bitmap toRoundRect(String path) {
        //创建位图对象
        Bitmap photo = lessenUriImage(path);
        return Transformation(photo);
    }

    /**
     * 用于把普通图片转换为圆角矩形图像
     *
     * @param imageID 图片资源ID
     * @param context 上下文对象
     * @return output 转换后的圆角矩形图像
     */
    Bitmap toRoundRect(Context context, int imageID) {
        //创建位图对象
        Bitmap photo = BitmapFactory.decodeResource(context.getResources(), imageID);
        return Transformation(photo);
    }

    /**
     * 用于把Uri图片转换为Bitmap对象
     *
     * @param path 图片URI地址
     * @return 生成的Bitmap对象
     */
    public final static Bitmap lessenUriImage(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options); //此时返回 bm 为空
        options.inJustDecodeBounds = false; //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = (int) (options.outHeight / (float) 320);
        if (be <= 0) be = 1;
        options.inSampleSize = be; //重新读入图片，注意此时已经把 options.inJustDecodeBounds 设回 false 了
        bitmap = BitmapFactory.decodeFile(path, options);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
//        System.out.println(w + " " + h); //after zoom
        return bitmap;
    }

    /**
     * 用于把Bitmap图像转换为圆角图像
     *
     * @param photo 需要转换的Bitmap对象
     * @return 转换成圆角的Bitmap对象
     */
    public Bitmap Transformation(Bitmap photo) {

        //根据源文件新建一个darwable对象
        Drawable imageDrawable = new BitmapDrawable(photo);

        // 新建一个新的输出图片
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        // 新建一个矩形
        RectF outerRect = new RectF(0, 0, width, height);

        // 产生一个红色的圆角矩形
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        canvas.drawRoundRect(outerRect, cornerRadius, cornerRadius, paint);

        // 将源图片绘制到这个圆角矩形上
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        imageDrawable.setBounds(0, 0, width, height);
        canvas.saveLayer(outerRect, paint, Canvas.ALL_SAVE_FLAG);
        imageDrawable.draw(canvas);
        canvas.restore();

        return output;
    }

}
