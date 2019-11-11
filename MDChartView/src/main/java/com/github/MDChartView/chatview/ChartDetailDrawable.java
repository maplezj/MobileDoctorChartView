package com.github.MDChartView.chatview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;

import java.util.List;

/**
 * @author zhaojian
 * @time 2019/4/28 11:18
 * @describe
 */
public abstract class ChartDetailDrawable extends Drawable
{
    private static final int PADDING_ARROUND = 20;
    private Paint mPaint = new Paint();
    protected int pointX;
    protected int pointY;
    private int chartWidth;
    private int chartHeight;
    protected List<ChartView.Point> mChartItemList;
    private Context mContext;

    public ChartDetailDrawable(Context context)
    {
        super();
        mContext = context;
    }

    @Override
    public void draw(Canvas canvas)
    {
        drawText(canvas);
    }

    public void setData(List<ChartView.Point> chartItemList, int chartWidth, int chartHeight)
    {
        mChartItemList = chartItemList;
        this.chartWidth = chartWidth;
        this.chartHeight = chartHeight;
        setPointXY();
    }

    protected void setPointXY()
    {
        if (mChartItemList == null || mChartItemList.isEmpty())
        {
            return;
        }

        pointX = (int) mChartItemList.get(0).getX();
        pointY = (int) mChartItemList.get(0).getY();
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

    protected abstract String createContent();


    private void drawText(Canvas canvas)
    {
        if (TextUtils.isEmpty(createContent()))
        {
            return;
        }
        TextPaint paint = new TextPaint();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(ChartUtils.dpToPx(mContext,ChartView.ChartConfig.DEFAULT_FONT_SIZE));
        paint.setColor(Color.WHITE);
        //String message = "随访日期：2019-02-12\n血         压：110/88mmHg\n用药信息：头孢、埃斯皮里、感冒药、发烧药";
        StaticLayout myStaticLayout = new StaticLayout(createContent(), paint, calculateWidth(), Layout.Alignment.ALIGN_NORMAL, 1.2f, 0.0f, false);

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

    protected int calculateWidth()
    {
        return (int) ChartUtils.dpToPx(mContext, ChartUtils.dpToPx(mContext, 140));
    }

    @Override
    public void setAlpha(int alpha)
    {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter)
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
