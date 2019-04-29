package com.sbys.zhaojian.mobiledoctorchartview.chatview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Shader;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.sbys.loggerlib.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by zhaojian on 2018/4/26.
 */

public class ChartView extends View
{
    private static final String TAG = "ChartView";
    public static final int COLOR_TEXT = Color.parseColor("#999999");
    public static final int COLOR_DASH_LINE = Color.parseColor("#CCCCCC");
    public static final int WRAPPER_LINE = Color.parseColor("#E3E3E3");

    /*血压*/
    public static final int CHART_TYPE_PRESSURE = 0;
    /*血糖*/
    public static final int CHART_TYPE_SUGAR = 1;
    /*体温*/
    public static final int CHART_TYPE_TEMP = 3;
    /*心率*/
    public static final int CHART_TYPE_HEART = 2;
    /*肺活量*/
    public static final int CHART_TYPE_LUNG = 4;

    private Paint paint = new Paint();
    private Path mPath = new Path();
    private Path mAssistPath;
    /*赛贝尔曲线的控制*/
    private float lineSmoothness = 0.13f;
    private PathMeasure mPathMeasure;
    /*多条线的数据*/
    private List<List<ChartItem>> mChartItemListList = new ArrayList<>();
    private DefaultValueEntity mValueEntity = new DefaultValueEntity();
    /*当前绘制中的多条数据*/
    private List<List<ChartItem>> currentDrawingItemsList = new ArrayList<>();
    private ChartConfig mChartConfig = new ChartConfig();
    private int type;
    private int xShowCount = 0;
    List<ChartItem> showingItems;
    private ChartDetailDrawable detailTextDrawable = new ChartDetailDrawable();

    public ChartView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void setEmpty(int type)
    {
        setMultiData(new ArrayList<List<ChartItem>>(), type);
    }

    public void setConfig(ChartConfig config)
    {
        mChartConfig = config;
    }

    public void setData(List<ChartItem> chartItemList, int type)
    {
        if (chartItemList == null)
        {
            Logger.d("data is null");
        }
        List<List<ChartItem>> chartItemListList = new ArrayList<>();
        chartItemListList.add(chartItemList);
        setMultiData(chartItemListList, type);

    }

    public void setMultiData(List<List<ChartItem>> chartItemListList)
    {
        setMultiData(chartItemListList, CHART_TYPE_PRESSURE);
    }

    private void setMultiData(List<List<ChartItem>> chartItemListList, int type)
    {
        if (checkValidate(chartItemListList, type))
        {
            Logger.d("list size:" + chartItemListList.size() + "|type:" + type);
            this.type = type;
            initData(chartItemListList);
        }
    }

    private void initData(List<List<ChartItem>> chartItemListList)
    {
        currentDrawingItemsList.clear();
        mChartItemListList.clear();
        mChartConfig.isInit = false;
        xShowCount = 0;

        mChartItemListList.addAll(chartItemListList);
        resetValueEntity();
        for (List<ChartItem> chartItems : chartItemListList)
        {
            if (chartItems.size() <= mChartConfig.countX)
            {
                currentDrawingItemsList.add(chartItems);
                if (xShowCount < chartItems.size())
                {
                    xShowCount = chartItems.size();
                }
            }
            else
            {
                currentDrawingItemsList.add(chartItems.subList(chartItems.size() - mChartConfig.countX, chartItems.size()));
                xShowCount = mChartConfig.countX;
            }
        }
        updateValueEntity();
        invalidate();
    }

    /**
     * 更新最大值最小值
     */
    private void updateValueEntity()
    {
        List<ChartItem> allDrawingItems = new ArrayList<>();
        for (List<ChartItem> chartItems : currentDrawingItemsList)
        {
            allDrawingItems.addAll(chartItems);
        }
        if (!allDrawingItems.isEmpty())
        {
            ChartItem realMax = Collections.max(allDrawingItems);
            ChartItem realMin = Collections.min(allDrawingItems);
            if (realMax.getValue() > mValueEntity.max)
            {
                mValueEntity.max = getRealMaxByType(realMax.getValue(), type);
            }
            if (realMin.getValue() < mValueEntity.min)
            {
                mValueEntity.min = getRealMinByType(realMin.getValue(), type);
            }
        }
    }

    /**
     * 数据校验
     *
     * @param chartItemListList
     * @param type
     * @return
     */
    private boolean checkValidate(List<List<ChartItem>> chartItemListList, int type)
    {
        if (chartItemListList == null)
        {
            Logger.d("data is null");
            return false;
        }
        if (type == CHART_TYPE_PRESSURE)
        {
            if (chartItemListList.isEmpty())
            {
                return true;
            }
            if (chartItemListList.size() != 2)
            {
                Logger.d("checkValidate: 血压数据必须为2条" + "|size:" + chartItemListList.size());
                return false;
            }
            else if (chartItemListList.get(0).size() != chartItemListList.get(1).size())
            {
                Logger.d("checkValidate: 血压大的两条数据长度必须一致");
                return false;
            }
            return true;
        }
        return true;
    }

    private void updateData()
    {
        resetValueEntity();
        int outCount = (int) Math.ceil((mChartConfig.xDistance + 0.001) / mChartConfig.unitXDistance);
        for (int i = 0; i < mChartItemListList.size(); i++)
        {
            List<ChartItem> chartItems = mChartItemListList.get(i);
            if (chartItems.size() > mChartConfig.countX + outCount)
            {
                currentDrawingItemsList.remove(i);
                currentDrawingItemsList.add(i, chartItems.subList(outCount - 1, outCount + mChartConfig.countX));
            }
            else
            {
                currentDrawingItemsList.remove(i);
                currentDrawingItemsList.add(i, chartItems.subList(outCount - 1, chartItems.size()));
            }
        }

        updateValueEntity();
    }

    private void resetValueEntity()
    {
        switch (type)
        {
            case CHART_TYPE_PRESSURE:
                mValueEntity.max = 150;
                mValueEntity.min = 50;
                mValueEntity.normalHigh = 139;
                mValueEntity.normalLow = 89;
                break;
            case CHART_TYPE_SUGAR:
                mValueEntity.max = 9;
                mValueEntity.min = 3;
                mValueEntity.normalHigh = 7.8f;
                mValueEntity.normalLow = 6.1f;
                break;
            case CHART_TYPE_TEMP:
                mValueEntity.max = 37.5f;
                mValueEntity.min = 35;
                mValueEntity.normalHigh = 37;
                mValueEntity.normalLow = 35.8f;
                break;
            case CHART_TYPE_HEART:
                mValueEntity.max = 110;
                mValueEntity.min = 50;
                mValueEntity.normalHigh = 100;
                mValueEntity.normalLow = 60;
                break;
            case CHART_TYPE_LUNG:
                mValueEntity.max = 100;
                mValueEntity.min = 60;
                mValueEntity.normalHigh = 90;
                mValueEntity.normalLow = 70;
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        mChartConfig.init(mValueEntity, this);
        drawWrapper(canvas);
        drawYUnit(canvas);
        drawXUnit(canvas);
        drawLine(canvas);
        drawPoint(canvas);
        drawPointText(canvas);
    }

    /*画点上的详细信息*/
    private void drawPointText(Canvas canvas)
    {
        int unitX = (int) mChartConfig.unitXDistance;
        float startX = -mChartConfig.xDistance % unitX;
        canvas.saveLayer(-14, -ChartConfig.DEFAULT_PADDING - mChartConfig.chartHeight - mChartConfig.fontHeightX,
                mChartConfig.chartWidth + 14, mChartConfig.fontHeightX + ChartConfig.DEFAULT_PADDING, null, Canvas.ALL_SAVE_FLAG);
        for (List<ChartItem> currentDrawingItems : currentDrawingItemsList)
        {
            if (currentDrawingItems == null || currentDrawingItems.isEmpty())
            {
                return;
            }

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            float deltaValue = mValueEntity.max - mValueEntity.min;
            if (xShowCount == 1 && mChartConfig.supportVerticalLine)
            {
                ChartItem current = currentDrawingItems.get(0);
                float valueY = mChartConfig.chartHeight * (current.getValue() - mValueEntity.min) / deltaValue;
                if (drawVerticalLine(current.getIndex()))
                {
                    drawDetailText(canvas, startX + unitX, -valueY);
                }
                return;
            }

            for (int i = 0; currentDrawingItems.size() > i; i++)
            {
                ChartItem current = currentDrawingItems.get(i);
                float valueY = mChartConfig.chartHeight * (current.getValue() - mValueEntity.min) / deltaValue;
                if (drawVerticalLine(current.getIndex()))
                {
                    drawDetailText(canvas, startX + i * unitX, -valueY);
                }
            }
        }
    }

    /*画点及竖线指示*/
    private void drawPoint(Canvas canvas)
    {
        int unitX = (int) mChartConfig.unitXDistance;
        float startX = -mChartConfig.xDistance % unitX;
        canvas.saveLayer(-14, -ChartConfig.DEFAULT_PADDING - mChartConfig.chartHeight - mChartConfig.fontHeightX,
                mChartConfig.chartWidth + 14, mChartConfig.fontHeightX + ChartConfig.DEFAULT_PADDING, null, Canvas.ALL_SAVE_FLAG);
        for (List<ChartItem> currentDrawingItems : currentDrawingItemsList)
        {
            //画X轴单位及点
            if (currentDrawingItems == null || currentDrawingItems.isEmpty())
            {
                return;
            }

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            drawPointNext(currentDrawingItems, canvas, unitX, startX, mValueEntity.max - mValueEntity.min);
        }
        canvas.restore();
    }

    /*画x轴上的单位*/
    private void drawXUnit(Canvas canvas)
    {
        paint.setPathEffect(null);
        if (mChartItemListList == null || mChartItemListList.isEmpty())
        {
            return;
        }

        canvas.translate(0, mChartConfig.chartHeight);
        paint.setStyle(Paint.Style.FILL);

        int unitX = (int) mChartConfig.unitXDistance;
        float startX = -mChartConfig.xDistance % unitX;

        drawDate(canvas, unitX, startX);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                MotionEventHelper.preX = MotionEventHelper.downX = event.getX();
                MotionEventHelper.downY = event.getY();
                if (mChartConfig.mMoveType == MoveType.TYPE_VERTIAL_LINE)
                {
                    mChartConfig.verticalIndex = getVerticalIndex(event.getX());
                    mChartConfig.drawingVerticalIndex = getDrawingVerticalIndex(event.getX());
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                MotionEventHelper.scrolling = true;
                float pre = MotionEventHelper.preX;
                MotionEventHelper.preX = event.getX();
                if (mChartConfig.mMoveType == MoveType.TYPE_LINE)
                {
                    scrollLine(pre - event.getX());
                }
                else if (mChartConfig.mMoveType == MoveType.TYPE_VERTIAL_LINE)
                {
                    if (mChartConfig.verticalIndex != getVerticalIndex(event.getX()))
                    {
                        mChartConfig.verticalIndex = getVerticalIndex(event.getX());
                        mChartConfig.drawingVerticalIndex = getDrawingVerticalIndex(event.getX());
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                MotionEventHelper.scrolling = false;
                if (MotionEventHelper.isclick(event.getX(), event.getY()))
                {
                    int index = getVerticalIndex(event.getX());
                    int drawingIndx = getDrawingVerticalIndex(event.getX());
                    Log.d(TAG, "onTouchEvent: click point--------------->" + index + "|drawing:" + drawingIndx);
                    if (onClickPointListener != null)
                    {
                        onClickPointListener.onClick(mChartItemListList.get(0).get(index));
                    }
                    mChartConfig.verticalIndex = index;
                    mChartConfig.drawingVerticalIndex = getDrawingVerticalIndex(event.getX());
                    invalidate();
                }
                break;
            default:
                break;
        }
        return true;
    }

    private int getVerticalIndex(float x)
    {
        return (int) ((mChartConfig.xDistance + x - mChartConfig.startX) / mChartConfig.unitXDistance + 0.5);
    }

    private int getDrawingVerticalIndex(float x)
    {
        float rest = mChartConfig.xDistance % mChartConfig.unitXDistance;
        return (int) ((rest + x - mChartConfig.startX) / mChartConfig.unitXDistance + 0.5);
    }

    private void scrollLine(float distanceX)
    {
        //如果向右滑动，并且已经滑动到最后一个，则不让滑动
        if (distanceX > 0 && isEnd())
        {
            Logger.d("onScroll-----------------> is end");
            return;
        }
        //如果向左滑动，并且已经滑动到第一个，则不让滑动
        if (distanceX < 0 && isStart())
        {
            Logger.d("onScroll------------------> is start");
            return;
        }
        mChartConfig.xDistance = mChartConfig.xDistance + distanceX;
        if (mChartConfig.xDistance < 0)
        {
            mChartConfig.xDistance = 0;
        }
        updateData();
        postInvalidate();
    }

    private boolean isEnd()
    {
        if (mChartItemListList == null || mChartItemListList.isEmpty())
        {
            return true;
        }
        List<ChartItem> chartItems = mChartItemListList.get(0);
        List<ChartItem> currentDrawingItems = currentDrawingItemsList.get(0);

        return chartItems.size() <= mChartConfig.countX
                || currentDrawingItems.get(currentDrawingItems.size() - 1).equals(chartItems.get(chartItems.size() - 1)) && mChartConfig.xDistance >= mChartConfig.chartWidth * (chartItems.size() - mChartConfig.countX) / (mChartConfig.countX - 1.0);
    }

    private boolean isStart()
    {
        if (mChartItemListList == null || mChartItemListList.isEmpty())
        {
            return true;
        }
        List<ChartItem> chartItems = mChartItemListList.get(0);
        List<ChartItem> currentDrawingItems = currentDrawingItemsList.get(0);
        return currentDrawingItems.get(0).equals(chartItems.get(0)) && mChartConfig.xDistance <= 0;
    }


    /**
     * 画单位
     */
    private void drawYUnit(Canvas canvas)
    {
        paint.setFakeBoldText(false);
        paint.setTextSize(ChartConfig.DEFAULT_FONT_SIZE);
        paint.setStrokeWidth(0);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setColor(COLOR_TEXT);
        //画Y轴单位
        float startY = mValueEntity.min;
        float unitY = (mValueEntity.max - mValueEntity.min) / (mChartConfig.countY - 1);
        for (int i = 0; i < mChartConfig.countY; i++)
        {
            String stringY = String.format("%.1f", startY + i * unitY);
            canvas.drawText(stringY, -10, (float) (mChartConfig.chartHeight * (1 - i / (mChartConfig.countY - 1.0))), paint);
        }

        if (!TextUtils.isEmpty(mChartConfig.unitYText))
        {
            canvas.drawText(mChartConfig.unitYText, -10, -unitY - ChartConfig.FONT_PADDING, paint);
        }

        //画正常值基准线
        paint.setColor(COLOR_DASH_LINE);
        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{12, 6, 12, 6}, 0);
        paint.setPathEffect(dashPathEffect);
        float lowY = mChartConfig.chartHeight * (1 - ((mValueEntity.normalLow - mValueEntity.min) / (mValueEntity.max - mValueEntity.min)));
        Path path1 = new Path();
        path1.moveTo(0, lowY);
        path1.lineTo(mChartConfig.chartWidth, lowY);
        canvas.drawPath(path1, paint);
        float heightY = mChartConfig.chartHeight * (1 - ((mValueEntity.normalHigh - mValueEntity.min) / (mValueEntity.max - mValueEntity.min)));
        Path path2 = new Path();
        path2.moveTo(0, heightY);
        path2.lineTo(mChartConfig.chartWidth, heightY);
        canvas.drawPath(path2, paint);
    }

    /**
     * 画外壳
     */
    private void drawWrapper(Canvas canvas)
    {
        paint.reset();
        paint.setColor(WRAPPER_LINE);
        paint.setFakeBoldText(false);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        canvas.translate(mChartConfig.startX, mChartConfig.startY);
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(0, mChartConfig.chartHeight);
        path.lineTo(mChartConfig.chartWidth, mChartConfig.chartHeight);
        canvas.drawPath(path, paint);
    }

    @SuppressLint("WrongConstant")
    private void drawLine(Canvas canvas)
    {
        canvas.saveLayer(0, -ChartConfig.DEFAULT_PADDING - mChartConfig.chartHeight - mChartConfig.fontHeightX,
                mChartConfig.chartWidth, mChartConfig.fontHeightX + ChartConfig.DEFAULT_PADDING, null, Canvas.ALL_SAVE_FLAG);
        for (List<ChartItem> currentDrawingItems : currentDrawingItemsList)
        {
            //画X轴单位及点
            if (currentDrawingItems == null || currentDrawingItems.isEmpty())
            {
                return;
            }

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            List<List<ChartItem>> itemsList = createLists(currentDrawingItems);
            if (itemsList == null || itemsList.isEmpty())
            {
                return;
            }
            for (List<ChartItem> subChartItems : itemsList)
            {
                mPath.reset();
                measurePath(subChartItems);
                float distance = mPathMeasure.getLength();
                if (mPathMeasure.getSegment(0, distance, mPath, true))
                {
                    setPaintColorByType(subChartItems.get(0).getType());
                    canvas.drawPath(mPath, paint);
                }
            }

        }
        canvas.restore();
        paint.setColor(Color.WHITE);
    }

    @SuppressLint("WrongConstant")
    private void drawPointNext(List<ChartItem> currentDrawingItems, Canvas canvas, int unitX, float startX, float deltaValue)
    {
        paint.setStyle(Paint.Style.FILL);
        showingItems = currentDrawingItems;
        float scale;

        //如果只有一条，画在中间
        if (xShowCount == 1 && mChartConfig.supportVerticalLine)
        {
            scale = 1.5f;
            ChartItem current = currentDrawingItems.get(0);
            float valueY = mChartConfig.chartHeight * (current.getValue() - mValueEntity.min) / deltaValue;
            drawPointDetail(canvas, startX + unitX, -valueY, scale, current.getType(), current.getIndex());
            return;
        }

        //否则从最左侧开始画
        for (int i = 0; currentDrawingItems.size() > i; i++)
        {
            ChartItem current = currentDrawingItems.get(i);
            float valueY = mChartConfig.chartHeight * (current.getValue() - mValueEntity.min) / deltaValue;
            scale = 1f;

            if (scalePoint(currentDrawingItems.get(i).getIndex()))
            {
                scale = 1.5f;
            }
            drawPointDetail(canvas, startX + i * unitX, -valueY, scale, current.getType(), current.getIndex());
        }
        paint.setColor(Color.BLACK);
    }

    private void drawPointDetail(Canvas canvas, float x, float y, float scale, int dataType, int index)
    {
        if (drawVerticalLine(index))
        {
            drawVerticalLine(canvas, x);
        }
        setPaintColorByType(dataType);
        canvas.drawCircle(x, y, 12 * scale, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, 6 * scale, paint);
    }

    /*坐标的圆点是否要放大*/
    private boolean scalePoint(int index)
    {
        if (mChartConfig.drawingVerticalIndex != index)
        {
            return false;
        }
        if (mChartConfig.mMoveType == MoveType.TYPE_VERTIAL_LINE)
        {
            return true;
        }
        else
        {
            return !MotionEventHelper.scrolling;
        }
    }

    /*是否要画竖直线*/
    private boolean drawVerticalLine(int index)
    {
        return mChartConfig.drawingVerticalIndex == index && mChartConfig.supportVerticalLine && scalePoint(index);
    }

    private void drawVerticalLine(Canvas canvas, float x)
    {
        LinearGradient linearGradient = new LinearGradient(x, 0, x, -mChartConfig.chartHeight, new int[]{
                0xfff7fcf9, 0xff3acdf3}, null,
                Shader.TileMode.REPEAT);
        canvas.save();
        paint.setStyle(Paint.Style.STROKE);
        paint.setShader(linearGradient);
        Path path = new Path();
        path.moveTo(x, -mChartConfig.chartHeight);
        path.lineTo(x, 0);
        canvas.drawPath(path, paint);
        canvas.restore();
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(null);
    }

    private void drawDetailText(Canvas canvas, float x, float y)
    {
        detailTextDrawable.setLocation((int) x, (int) y, (int) mChartConfig.chartWidth, (int) mChartConfig.chartHeight);
        detailTextDrawable.draw(canvas);
    }

    private void drawDate(Canvas canvas, int unitX, float startX)
    {
        List<ChartItem> currentDrawingItems = currentDrawingItemsList.get(0);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(COLOR_TEXT);

        canvas.saveLayer(-mChartConfig.fontWidthX / 2, -ChartConfig.DEFAULT_PADDING - mChartConfig.chartHeight - mChartConfig.fontHeightX,
                mChartConfig.chartWidth + mChartConfig.fontWidthX / 2 + mChartConfig.unitXWidth + ChartConfig.FONT_PADDING,
                mChartConfig.fontHeightX + ChartConfig.DEFAULT_PADDING, null, Canvas.ALL_SAVE_FLAG);
        if (!TextUtils.isEmpty(mChartConfig.unitXText))
        {
            canvas.drawText(mChartConfig.unitXText, mChartConfig.chartWidth + +mChartConfig.fontWidthX / 2 + ChartConfig.FONT_PADDING, mChartConfig.fontHeightX / 2 + 30, paint);
        }
        canvas.restore();
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.saveLayer(-mChartConfig.fontWidthX / 2, -ChartConfig.DEFAULT_PADDING - mChartConfig.chartHeight - mChartConfig.fontHeightX,
                mChartConfig.chartWidth + mChartConfig.fontWidthX / 2,
                mChartConfig.fontHeightX + ChartConfig.DEFAULT_PADDING, null, Canvas.ALL_SAVE_FLAG);

        //如果只有一条，画在中间
        if (xShowCount == 1)
        {
            ChartItem current = currentDrawingItems.get(0);
            //画日期
            if (mChartConfig.xUnitType == UnitType.TYPE_NUM)
            {
                canvas.drawText(current.getDate(), startX + unitX, mChartConfig.fontHeightX / 2, paint);
            }
            else
            {
                canvas.drawText(DateUtil.getDateDay(current.getDate()), startX + unitX, mChartConfig.fontHeightX / 2, paint);
                canvas.drawText(DateUtil.getDateHour(current.getDate()), startX + unitX, mChartConfig.fontHeightX, paint);
            }
            return;
        }

        //否则从最左侧开始画
        for (int i = 0; currentDrawingItems.size() > i; i++)
        {
            ChartItem current = currentDrawingItems.get(i);
            //画日期
            if (mChartConfig.xUnitType == UnitType.TYPE_NUM)
            {
                canvas.drawText(current.getDate(), startX + i * unitX, mChartConfig.fontHeightX / 2 + 30, paint);
            }
            else
            {
                canvas.drawText(DateUtil.getDateDay(current.getDate()), startX + i * unitX, mChartConfig.fontHeightX / 2 + 20, paint);
                canvas.drawText(DateUtil.getDateHour(current.getDate()), startX + i * unitX, mChartConfig.fontHeightX + 30, paint);
            }
        }

    }

    private List<List<ChartItem>> createLists(List<ChartItem> currentDrawingItems)
    {
        if (currentDrawingItems == null || currentDrawingItems.isEmpty())
        {
            return null;
        }
        List<List<ChartItem>> lists = new ArrayList<>();
        List<ChartItem> normalList = new ArrayList<>();
        List<ChartItem> typeOneList = new ArrayList<>();
        List<ChartItem> typeTwoList = new ArrayList<>();
        List<ChartItem> typeThreeList = new ArrayList<>();
        for (int i = 0; i < currentDrawingItems.size(); i++)
        {
            ChartItem currentDrawingItem = currentDrawingItems.get(i);
            currentDrawingItem.setIndex(i);
            switch (currentDrawingItem.getType())
            {
                case ChartItem.TYPE_NORMAL:
                    normalList.add(currentDrawingItem);
                    break;
                case ChartItem.TYPE_SINGLE_ONE:
                    typeOneList.add(currentDrawingItem);
                    break;
                case ChartItem.TYPE_SINGLE_TWO:
                    typeTwoList.add(currentDrawingItem);
                    break;
                case ChartItem.TYPE_SINGLE_THREE:
                    typeThreeList.add(currentDrawingItem);
                    break;
                default:
                    break;
            }
        }
        if (!normalList.isEmpty())
        {
            lists.add(normalList);
        }
        if (!typeOneList.isEmpty())
        {
            lists.add(typeOneList);
        }
        if (!typeTwoList.isEmpty())
        {
            lists.add(typeTwoList);
        }
        if (!typeThreeList.isEmpty())
        {
            lists.add(typeThreeList);
        }
        return lists;
    }

    private void measurePath(List<ChartItem> items)
    {
        mPath = new Path();
        mAssistPath = new Path();
        float prePreviousPointX = Float.NaN;
        float prePreviousPointY = Float.NaN;
        float previousPointX = Float.NaN;
        float previousPointY = Float.NaN;
        float currentPointX = Float.NaN;
        float currentPointY = Float.NaN;
        float nextPointX;
        float nextPointY;

        final int lineSize = items.size();
        int unitX = (int) mChartConfig.unitXDistance;
        float deltaValue = mValueEntity.max - mValueEntity.min;
        float startX = -mChartConfig.xDistance % unitX;
        for (int valueIndex = 0; valueIndex < lineSize; ++valueIndex)
        {
            ChartItem currentItem = items.get(valueIndex);

            if (Float.isNaN(currentPointX))
            {
                float valueY = mChartConfig.chartHeight * (currentItem.getValue() - mValueEntity.min) / deltaValue;

                currentPointX = startX + currentItem.getIndex() * unitX;
                currentPointY = -valueY;
            }
            if (Float.isNaN(previousPointX))
            {
                //是否是第一个点
                if (valueIndex > 0)
                {
                    ChartItem preItem = items.get(valueIndex - 1);
                    previousPointX = startX + (currentItem.getIndex() - preItem.getIndex()) * unitX;
                    previousPointY = -mChartConfig.chartHeight * (preItem.getValue() - mValueEntity.min) / deltaValue;
                }
                else
                {
                    //是的话就用当前点表示上一个点
                    previousPointX = currentPointX;
                    previousPointY = currentPointY;
                }
            }

            if (Float.isNaN(prePreviousPointX))
            {
                //是否是前两个点
                if (valueIndex > 1)
                {
                    ChartItem prePreItem = items.get(valueIndex - 2);
                    prePreviousPointX = startX + (currentItem.getIndex() - prePreItem.getIndex()) * unitX;
                    prePreviousPointY = -mChartConfig.chartHeight * (prePreItem.getValue() - mValueEntity.min) / deltaValue;
                }
                else
                {
                    //是的话就用当前点表示上上个点
                    prePreviousPointX = previousPointX;
                    prePreviousPointY = previousPointY;
                }
            }

            // 判断是不是最后一个点了
            if (valueIndex < lineSize - 1)
            {
                ChartItem lastItem = items.get(valueIndex + 1);
                float valueY = mChartConfig.chartHeight * (lastItem.getValue() - mValueEntity.min) / deltaValue;

                nextPointX = startX + (lastItem.getIndex()) * unitX;
                nextPointY = -valueY;
            }
            else
            {
                //是的话就用当前点表示下一个点
                nextPointX = currentPointX;
                nextPointY = currentPointY;
            }

            if (valueIndex == 0)
            {
                // 将Path移动到开始点
                mPath.moveTo(currentPointX, currentPointY);
                mAssistPath.moveTo(currentPointX, currentPointY);
            }
            else
            {
                // 求出控制点坐标
                final float firstDiffX = (currentPointX - prePreviousPointX);
                final float firstDiffY = (currentPointY - prePreviousPointY);
                final float secondDiffX = (nextPointX - previousPointX);
                final float secondDiffY = (nextPointY - previousPointY);
                final float firstControlPointX = previousPointX + (lineSmoothness * firstDiffX);
                final float firstControlPointY = previousPointY + (lineSmoothness * firstDiffY);
                final float secondControlPointX = currentPointX - (lineSmoothness * secondDiffX);
                final float secondControlPointY = currentPointY - (lineSmoothness * secondDiffY);
                //画出曲线
                mPath.cubicTo(firstControlPointX, firstControlPointY, secondControlPointX, secondControlPointY,
                        currentPointX, currentPointY);
                //将控制点保存到辅助路径上
                mAssistPath.lineTo(firstControlPointX, firstControlPointY);
                mAssistPath.lineTo(secondControlPointX, secondControlPointY);
                mAssistPath.lineTo(currentPointX, currentPointY);
            }

            // 更新值,
            prePreviousPointX = previousPointX;
            prePreviousPointY = previousPointY;
            previousPointX = currentPointX;
            previousPointY = currentPointY;
            currentPointX = nextPointX;
            currentPointY = nextPointY;
        }
        mPathMeasure = new PathMeasure(mPath, false);
    }

    private float getRealMaxByType(float value, int type)
    {
        //心率取10的倍数
        if (type == CHART_TYPE_HEART)
        {
            return (float) (Math.ceil(value / 10) * 10);
        }
        /*其他取整*/
        else
        {
            return (float) Math.ceil(value);
        }
    }

    private float getRealMinByType(float value, int type)
    {
        //心率取10的倍数
        if (type == CHART_TYPE_HEART)
        {
            return (float) (Math.floor(value / 10) * 10);
        }
        /*其他取整*/
        else
        {
            return (float) Math.floor(value);
        }
    }


    public void setPaintColorByType(int type)
    {
        switch (type)
        {
            case ChartItem.TYPE_NORMAL:
                paint.setColor(Color.rgb(86, 189, 114));//绿
                break;
            case ChartItem.TYPE_SINGLE_ONE:
                paint.setColor(Color.rgb(86, 189, 114));//绿
                break;
            case ChartItem.TYPE_SINGLE_TWO:
                paint.setColor(Color.rgb(78, 193, 242));//蓝
                break;
            case ChartItem.TYPE_SINGLE_THREE:
                paint.setColor(Color.RED);
                break;
            default:
                paint.setColor(Color.WHITE);
                break;
        }
    }

    private static class DefaultValueEntity
    {
        float max;
        float min;
        float normalHigh;
        float normalLow;
    }

    public enum UnitType
    {
        TYPE_NUM, TYPE_DATE
    }

    public enum MoveType
    {
        /*曲线*/
        TYPE_LINE,
        /*垂直线*/
        TYPE_VERTIAL_LINE
    }

    static class ChartConfig
    {

        /*默认x轴刻度数量*/
        public static final int DEFAULT_X_COUNT = 7;
        /*默认y轴刻度数量*/
        public static final int DEFAULT_Y_COUNT = 6;

        /*默认字体大小*/
        private static final int DEFAULT_FONT_SIZE = 30;
        /*边距*/
        public static final int DEFAULT_PADDING = 50;

        public static final int FONT_PADDING = 10;

        /*是否已经初始化（只初始化一次）*/
        boolean isInit = false;

        /*X轴上的单位*/
        String unitXText;
        /*X轴上的单位宽度*/
        float unitXWidth;
        /*Y轴上的单位*/
        String unitYText;
        /*Y轴上的单位高度*/
        float unitYHeight = 0;
        /*x轴刻度数量*/
        int countX = DEFAULT_X_COUNT;
        /*y轴刻度数量*/
        int countY = DEFAULT_Y_COUNT;
        /*X轴上的单位类型*/
        private UnitType xUnitType = UnitType.TYPE_DATE;
        /*支持竖线提示*/
        private boolean supportVerticalLine = false;
        /*滑动模式*/
        private MoveType mMoveType = MoveType.TYPE_LINE;

        /*总宽度*/
        float totalWidth;
        /*总高度*/
        float totalHeight;
        /*Y坐标文字宽度*/
        float fontWidthY;
        /*X坐标文字宽度*/
        float fontWidthX;
        /*X坐标文字高度*/
        float fontHeightX;
        /*x起始位置*/
        float startX;
        /*Y起始位置*/
        float startY;
        /*图表宽度（不包括单位和日期的区域）*/
        float chartWidth;
        /*图表高度（不包括单位和日期的区域）*/
        float chartHeight;
        /*x轴距*/
        float unitXDistance;
        /*y轴距*/
        float unitYDistance;

        /*-----------------滑动时实时变动的数据--------------------*/
        /*X轴滑动的距离*/
        private float xDistance = 0;
        /*竖线在所有数据中的位置*/
        private int verticalIndex;
        /*竖线在正在的数据中的位置*/
        private int drawingVerticalIndex;

        void init(DefaultValueEntity valueEntity, ChartView chartView)
        {
            totalHeight = chartView.getHeight();
            totalWidth = chartView.getWidth();

            Paint paint = new Paint();
            paint.setTextSize(DEFAULT_FONT_SIZE);
            float fontHeight = CommonUtil.calculateFontHeight(paint);
            fontWidthY = CommonUtil.calculateFontWidth(paint, valueEntity.max + "");
            if (xUnitType == UnitType.TYPE_DATE)
            {
                fontWidthX = CommonUtil.calculateFontWidth(paint, "00:00:00");
                fontHeightX = 2 * fontHeight;
            }
            else
            {
                fontWidthX = CommonUtil.calculateFontWidth(paint, "99");
                fontHeightX = fontHeight;
            }
            if (!TextUtils.isEmpty(unitYText))
            {
                unitYHeight = fontHeight;
            }

            if (!TextUtils.isEmpty(unitXText))
            {
                unitXWidth = CommonUtil.calculateFontWidth(paint, unitXText + FONT_PADDING);
            }

            startX = fontWidthY + DEFAULT_PADDING;
            startY = DEFAULT_PADDING + unitYHeight;
            chartWidth = totalWidth - fontWidthY - 2 * DEFAULT_PADDING - unitXWidth;
            chartHeight = totalHeight - fontHeightX - 2 * DEFAULT_PADDING - unitYHeight;

            //如果只有一条数据，则分成两段，值放中间
            if (chartView.xShowCount <= 1)
            {
                unitXDistance = (int) chartWidth / 2;
                chartWidth = unitXDistance * 2;
            }
            //否则安正常流程处理
            else
            {
                //X的每个刻度取整
                unitXDistance = (int) chartWidth / (chartView.xShowCount - 1);
                //X刻度取整后，图表宽度需根据刻度重新计算，取整
                chartWidth = unitXDistance * (chartView.xShowCount - 1);
            }

            unitYDistance = chartHeight / (countY - 1);
            if (!isInit && chartView.mChartItemListList != null && !chartView.mChartItemListList.isEmpty())
            {
                isInit = true;
                if (chartView.xShowCount >= countX)
                {
                    xDistance = (float) (chartWidth * (chartView.mChartItemListList.get(0).size() - chartView.xShowCount) / (chartView.xShowCount - 1.0));
                }
                else
                {
                    xDistance = 0;
                }
            }
        }
    }

    public static class ChartConfigBuilder
    {
        private ChartConfig mChartConfig;

        public ChartConfigBuilder()
        {
            mChartConfig = new ChartConfig();
        }

        /*设置X轴上的单位*/
        public ChartConfigBuilder setUnitX(String unitX)
        {
            mChartConfig.unitXText = unitX;
            return this;
        }

        /*设置Y轴上的单位*/
        public ChartConfigBuilder setUnitY(String unitY)
        {
            mChartConfig.unitYText = unitY;
            return this;
        }

        /*设置X轴的刻度数量*/
        public ChartConfigBuilder setCountX(int countX)
        {
            mChartConfig.countX = countX;
            return this;
        }

        /*设置Y轴的刻度数量*/
        public ChartConfigBuilder setCountY(int countY)
        {
            mChartConfig.countY = countY;
            return this;
        }

        /*设置X轴上的单位类型*/
        public ChartConfigBuilder setUnitXType(UnitType unitType)
        {
            mChartConfig.xUnitType = unitType;
            return this;
        }

        public ChartConfigBuilder showVertialLine(boolean show)
        {
            mChartConfig.supportVerticalLine = show;
            return this;
        }

        /*设置滑动模式*/
        public ChartConfigBuilder setMoveType(MoveType moveType)
        {
            mChartConfig.mMoveType = moveType;
            return this;
        }

        public ChartConfig build()
        {
            return mChartConfig;
        }
    }

    public interface OnClickPointListener
    {
        void onClick(ChartItem chartItem);
    }

    public OnClickPointListener onClickPointListener;

    public void setOnClickPointListener(OnClickPointListener onClickPointListener)
    {
        this.onClickPointListener = onClickPointListener;
    }

}
