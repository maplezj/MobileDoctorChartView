package com.sbys.zhaojian.mobiledoctorchartview.chatview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author zhaojian
 * @time 2019/4/28 11:18
 * @describe
 */
public class ChartDetailDrawable extends Drawable
{
    private Paint mPaint = new Paint();

    @Override
    public void draw(@NonNull Canvas canvas)
    {

        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.FILL);
        Rect rect = getBounds();
        Path path = new Path();
       // path.moveTo(0, 0);
        path.moveTo(rect.left, rect.top);
        path.lineTo(rect.right,  rect.top);
        path.lineTo(rect.right, rect.bottom);
        path.lineTo(rect.left, rect.bottom);
        canvas.drawPath(path, mPaint);
    }

    @Override
    public void setAlpha(int alpha)
    {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter)
    {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity()
    {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth()
    {
        return 400;
    }

    @Override
    public int getIntrinsicHeight()
    {
        return 400;
    }
}
