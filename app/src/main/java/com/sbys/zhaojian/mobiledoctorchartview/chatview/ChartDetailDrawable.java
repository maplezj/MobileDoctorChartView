package com.sbys.zhaojian.mobiledoctorchartview.chatview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

/**
 * @author zhaojian
 * @time 2019/4/28 11:18
 * @describe
 */
public class ChartDetailDrawable extends Drawable
{
    private static final int PADDING_ARROUND = 20;
    private Paint mPaint = new Paint();
    private int pointX;
    private int pointY;
    private int chartWidth;
    private int chartHeight;

    @Override
    public void draw(@NonNull Canvas canvas)
    {
        drawText(canvas);
    }

    public void setLocation(int pointX, int pointY, int chartWidth, int chartHeight)
    {
        this.pointX = pointX;
        this.pointY = pointY;
        this.chartWidth = chartWidth;
        this.chartHeight = chartHeight;
    }

    private int calculateLeft(float x, int width)
    {
        if (Math.abs(x) > chartWidth / 2)
        {
            return (int) (x - width) - 20 - PADDING_ARROUND;
        }
        return (int) x + 20 + PADDING_ARROUND;
    }

    private int calculateTop(float y, int height)
    {
        if (Math.abs(y) < chartHeight / 2)
        {
            return (int) (y - height) - PADDING_ARROUND;
        }
        return (int) y + PADDING_ARROUND;
    }


    private void drawText(Canvas canvas)
    {
        TextPaint paint = new TextPaint();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(34);
        paint.setColor(Color.WHITE);
        String message = "随访日期：2019-02-12\n血         压：110/88mmHg\n用药信息：头孢、埃斯皮里、感冒药、发烧药";
        StaticLayout myStaticLayout = new StaticLayout(message, paint, chartWidth/2, Layout.Alignment.ALIGN_NORMAL, 1.2f, 0.0f, false);

        mPaint.setColor(Color.BLACK);
        mPaint.setAlpha(178);
        mPaint.setStyle(Paint.Style.FILL);
        int width = myStaticLayout.getWidth();
        int height = myStaticLayout.getHeight();
        RectF rectF = new RectF(calculateLeft(pointX, width) - PADDING_ARROUND, calculateTop(pointY, height) - PADDING_ARROUND,
                calculateLeft(pointX, width) + width + PADDING_ARROUND, calculateTop(pointY, height) + height + PADDING_ARROUND);
        canvas.drawRoundRect(rectF, 10, 10, mPaint);

        canvas.save();
        canvas.translate(calculateLeft(pointX, width), calculateTop(pointY, height));
        myStaticLayout.draw(canvas);
        canvas.restore();
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
