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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Created by zhaojian on 2018/4/26.
 */

public class ChartView extends View
{
    private static final String TAG = "ChartView";
    public static final int COLOR_TEXT = Color.parseColor("#999999");
    public static final int COLOR_DASH_LINE = Color.parseColor("#CCCCCC");
    public static final int WRAPPER_LINE = Color.parseColor("#E3E3E3");

    /*一组数据包含多条曲线*/
    public static final int LINE_TYPE_MULT = 0xffffffff;

    private Paint paint = new Paint();
    private Path mPath = new Path();
    private Path mAssistPath;
    /*赛贝尔曲线的控制*/
    private float lineSmoothness = 0.13f;
    private PathMeasure mPathMeasure;
    private ValueEntity mValueEntity = new ValueEntity();
    private ChartConfig mChartConfig = new ChartConfig();
    private int xShowCount = 0;

    private Map<Integer, List<ChartItem>> mChartItemListMap = new HashMap<>();
    private Map<Integer, List<ChartItem>> drawingListMap = new HashMap<>();
    private Map<Integer, List<Point>> pointListMap;

    public ChartView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

/*    public void setEmpty(int type)
    {
        setMultiData(new ArrayList<List<ChartItem>>(), type);
    }*/

    public void setConfig(ChartConfig config)
    {
        mChartConfig = config;
        initData(mChartConfig.sourceChartItemListMap);
    }

    public void clear()
    {
        if (mChartConfig != null)
        {
            mChartConfig.sourceChartItemListMap.clear();
        }
    }

    private void fillEmptyOnce(boolean start)
    {
        for (Integer integer : mChartConfig.sourceChartItemListMap.keySet())
        {
            if (start)
            {
                mChartConfig.sourceChartItemListMap.get(integer).add(0, new EmptyChartItem(""));
            }
            else
            {
                mChartConfig.sourceChartItemListMap.get(integer).add(new EmptyChartItem(""));
            }
        }
    }

    private void parseData(Map<Integer, List<ChartItem>> chartItemListMap)
    {
        boolean start = false;
        while (!mChartConfig.showFullScreen && getMaxSize() < mChartConfig.countX)
        {
            fillEmptyOnce(start);
            start = !start;
        }

        int maxSize = 0;
        for (Integer integer : chartItemListMap.keySet())
        {
            if (maxSize < chartItemListMap.get(integer).size())
            {
                maxSize = chartItemListMap.get(integer).size();
            }

            if (integer == LINE_TYPE_MULT)
            {
                List<ChartItem> chartItemList = chartItemListMap.get(integer);
                for (int i = 0; i < chartItemList.size(); i++)
                {
                    ChartItem item = chartItemList.get(i);
                    item.setIndex(i);
                    if (!mChartItemListMap.containsKey(item.getType()))
                    {
                        List<ChartItem> newList = new ArrayList<>();
                        mChartItemListMap.put(item.getType(), newList);
                        newList.add(item);
                    }
                    else
                    {
                        mChartItemListMap.get(item.getType()).add(item);
                    }
                }
            }
            else
            {
                if (integer == ChartItem.LINE_SOURCE && !mChartConfig.standardLineCanPoint)
                {
                    mChartConfig.sourceEndIndex = chartItemListMap.get(integer).size() - 1;
                }
                for (int i = 0; i < chartItemListMap.get(integer).size(); i++)
                {
                    ChartItem item = chartItemListMap.get(integer).get(i);
                    item.setType(integer);
                    item.setIndex(i);
                }

                if (!mChartItemListMap.containsKey(integer))
                {
                    List<ChartItem> chartItemList = new ArrayList<>();
                    mChartItemListMap.put(integer, chartItemList);
                    chartItemList.addAll(chartItemListMap.get(integer));
                }
                else
                {
                    mChartItemListMap.get(integer).addAll(chartItemListMap.get(integer));
                }
            }
        }
        if (mChartConfig.showFullScreen)
        {
            xShowCount = maxSize < mChartConfig.countX ? maxSize : mChartConfig.countX;
        }
        else
        {
            xShowCount = mChartConfig.countX;
        }
        mChartConfig.endIndex = getMaxSize() - 1;
        mChartConfig.startIndex = mChartConfig.endIndex - xShowCount+1;
        parseShowData();
    }

    private void parseShowData()
    {
        drawingListMap.clear();
        for (Integer integer : mChartItemListMap.keySet())
        {
            List<ChartItem> drawingItems = new ArrayList<>();
            for (ChartItem item : mChartItemListMap.get(integer))
            {
                if (item.getIndex() >= mChartConfig.startIndex && item.getIndex() <= mChartConfig.endIndex)
                {
                    drawingItems.add(item);
                }
            }
            drawingListMap.put(integer, drawingItems);
        }

    }

    private void initData(Map<Integer, List<ChartItem>> chartItemListMap)
    {
        mChartItemListMap.clear();
        drawingListMap.clear();
        mChartConfig.isInit = false;
        xShowCount = 0;

        parseData(chartItemListMap);
        resetValueEntity();
        updateValueEntity();
        invalidate();
    }

    /**
     * 更新最大值最小值
     */
    private void updateValueEntity()
    {
        List<ChartItem> allDrawingItems = new ArrayList<>();
        for (Integer integer : drawingListMap.keySet())
        {
            allDrawingItems.addAll(drawingListMap.get(integer));
        }
        for (Iterator iter = allDrawingItems.iterator(); iter.hasNext();)
        {
            if (iter.next() instanceof EmptyChartItem)
            {
                iter.remove();
            }
        }

        if (!allDrawingItems.isEmpty())
        {
            ChartItem realMax = Collections.max(allDrawingItems);
            ChartItem realMin = Collections.min(allDrawingItems);
            if (realMax.getValueY() > mValueEntity.max)
            {
                mValueEntity.max = getRealMaxByType(realMax.getValueY());
            }
            if (realMin.getValueY() < mValueEntity.min)
            {
                mValueEntity.min = getRealMinByType(realMin.getValueY());
            }
        }
    }

    private void updateData()
    {
        resetValueEntity();
        int outCount = (int) Math.ceil((mChartConfig.xDistance + 0.001) / mChartConfig.unitXDistance);
        mChartConfig.startIndex = outCount-1;
        mChartConfig.endIndex = outCount + mChartConfig.countX-1;
        parseShowData();
        updateValueEntity();
    }

    private void resetValueEntity()
    {
        mValueEntity.normalLow = mChartConfig.standValueEntity.normalLow;
        mValueEntity.normalHigh = mChartConfig.standValueEntity.normalHigh;
        mValueEntity.min = mChartConfig.standValueEntity.min;
        mValueEntity.max = mChartConfig.standValueEntity.max;
        /*switch (mChartConfig.type)
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
        }*/
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        mChartConfig.init(mValueEntity, this);
        createDrawingPoint();
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
        canvas.saveLayer(-14, -ChartConfig.DEFAULT_PADDING - mChartConfig.chartHeight - mChartConfig.fontHeightX,
                mChartConfig.chartWidth + 14, mChartConfig.fontHeightX + ChartConfig.DEFAULT_PADDING, null, Canvas.ALL_SAVE_FLAG);
        Map<Integer,List<Point>> transferMap = transferMap();
        if (transferMap == null || transferMap.isEmpty())
        {
            return;
        }

        if (xShowCount == 1)
        {
            int index = transferMap.keySet().iterator().next();
            List<Point> pointList = transferMap.get(index);
            List<Point> copy = new ArrayList<>();
            for (Point point : pointList)
            {
                Point pointCopy = (Point) point.clone();
                pointCopy.x = mChartConfig.chartWidth / 2;
                copy.add(pointCopy);
            }
            if (drawVerticalLine(index))
            {
                drawDetailText(canvas, copy);
            }
            return;
        }

        for (Integer integer : transferMap.keySet())
        {
            List<Point> pointList = transferMap.get(integer);
            if (pointList == null || pointList.isEmpty())
            {
                return;
            }

            if (drawVerticalLine(integer))
            {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5);
                drawDetailText(canvas, pointList);
            }

        }
    }

    /*将pointListMap转换成以index为key的map*/
    private Map<Integer, List<Point>> transferMap()
    {
          Map<Integer, List<Point>> result = new HashMap<>();
        for (Integer integer : pointListMap.keySet())
        {
            List<Point> pointList = pointListMap.get(integer);
            if (pointList == null || pointList.isEmpty())
            {
                return result;
            }

            for (Point point : pointList)
            {
                if (result.containsKey(point.index))
                {
                    result.get(point.index).add(point);
                }
                else
                {
                    List<Point> chartItemList = new ArrayList<>();
                    chartItemList.add(point);
                    result.put(point.index, chartItemList);
                }
            }
        }
        return result;
    }

    /*画点及竖线指示*/
    private void drawPoint(Canvas canvas)
    {
        canvas.saveLayer(-20, -ChartConfig.DEFAULT_PADDING - mChartConfig.chartHeight - mChartConfig.fontHeightX,
                mChartConfig.chartWidth + 20, mChartConfig.fontHeightX + ChartConfig.DEFAULT_PADDING, null, Canvas.ALL_SAVE_FLAG);
        Map<Integer,List<Point>> transfer = transferMap();
        if (transfer == null || transfer.isEmpty())
        {
            return;
        }
        for (Integer integer : transfer.keySet())
        {
            List<Point> pointList = transfer.get(integer);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            if (drawVerticalLine(integer))
            {
                float x = xShowCount == 1 ? mChartConfig.chartWidth / 2 : pointList.get(0).x;
                drawVerticalLine(canvas, x);
            }
            drawPointNext(pointList, canvas);
        }
        canvas.restore();
    }

    /*画x轴上的单位*/
    private void drawXUnit(Canvas canvas)
    {
        paint.setPathEffect(null);
        if (mChartItemListMap == null || mChartItemListMap.isEmpty())
        {
            return;
        }

        canvas.translate(0, mChartConfig.chartHeight);
        paint.setStyle(Paint.Style.FILL);

        int unitX = (int) mChartConfig.unitXDistance;
        float startX = -mChartConfig.xDistance % unitX;

        drawDate(canvas, unitX, startX);

    }

    /*是否有数据*/
    private boolean isEmpty()
    {
        for (Integer integer : mChartConfig.sourceChartItemListMap.keySet())
        {
            List<ChartItem> itemList = mChartConfig.sourceChartItemListMap.get(integer);
            if (!itemList.isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (isEmpty())
        {
            return true;
        }
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                MotionEventHelper.preX = MotionEventHelper.downX = event.getX();
                MotionEventHelper.downY = event.getY();
                if (mChartConfig.mMoveType == MoveType.TYPE_VERTIAL_LINE)
                {
                    mChartConfig.verticalIndex = getVerticalIndex(event.getX());
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                MotionEventHelper.scrolling = true;
                float pre = MotionEventHelper.preX;
                MotionEventHelper.preX = event.getX();
                if (mChartConfig.mMoveType == MoveType.TYPE_LINE)
                {
                    mChartConfig.verticalIndex = -1;
                    scrollLine(pre - event.getX());
                }
                else if (mChartConfig.mMoveType == MoveType.TYPE_VERTIAL_LINE)
                {
                    if (mChartConfig.verticalIndex != getVerticalIndex(event.getX()))
                    {
                        mChartConfig.verticalIndex = getVerticalIndex(event.getX());
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
                    if (index > getMaxIndex())
                    {
                        index = getMaxIndex();
                    }
                    //int drawingIndx = getDrawingVerticalIndex(event.getX());
                    Log.d(TAG, "onTouchEvent: click point--------------->" + index);
                    if (onClickPointListener != null && index >= 0)
                    {
                        onClickPointListener.onClick(getChartItemsByIndex(index));
                    }
                    mChartConfig.verticalIndex = index;
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
        int index = (int) ((mChartConfig.xDistance + x - mChartConfig.startX) / mChartConfig.unitXDistance + 0.5);
        boolean left = ((mChartConfig.xDistance + x - mChartConfig.startX) % mChartConfig.unitXDistance) / mChartConfig.unitXDistance > 0.5;
        if (index < 0)
        {
            return -1;
        }
        if (mChartItemListMap.containsKey(ChartItem.LINE_SOURCE) && !mChartConfig.standardLineCanPoint)
        {
            List<ChartItem> chartItemList = mChartItemListMap.get(ChartItem.LINE_SOURCE);
            return realVerticalIndex(chartItemList, index, left);
            /*if (chartItemList.size() - 1 >= index)
            {
                if (chartItemList.get(index) instanceof EmptyChartItem)
                {
                    return -1;
                }
            }*/
        }
        return index;
    }

    /*获取真正的index（用于过滤掉空数据）*/
    private int realVerticalIndex(List<ChartItem> chartItemList, int index, boolean left)
    {
        if (index >= chartItemList.size())
        {
            return -1;
        }
        if (!(chartItemList.get(index) instanceof EmptyChartItem))
        {
            return index;
        }
        int leftIndex = index;
        int rightIndex = index;
        while (leftIndex >= 0 || rightIndex < chartItemList.size())
        {
            if (left)
            {
                leftIndex--;
                left = false;
                if (leftIndex >= 0)
                {
                    ChartItem item = chartItemList.get(leftIndex);
                    if (!(item instanceof EmptyChartItem))
                    {
                        return leftIndex;
                    }
                }
            }
            else
            {
                rightIndex++;
                left = true;
                if (rightIndex < chartItemList.size())
                {
                    ChartItem item = chartItemList.get(rightIndex);
                    if (!(item instanceof EmptyChartItem))
                    {
                        return rightIndex;
                    }
                }
            }
        }
        return -1;
    }

    private void scrollLine(float distanceX)
    {
        //如果向右滑动，并且已经滑动到最后一个，则不让滑动
        if (distanceX > 0 && isEnd())
        {
            Log.d("MainActivity","onScroll-----------------> is end");
            return;
        }
        //如果向左滑动，并且已经滑动到第一个，则不让滑动
        if (distanceX < 0 && isStart())
        {
            Log.d("MainActivity","onScroll------------------> is start");
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
        if (mChartItemListMap == null || mChartItemListMap.isEmpty())
        {
            return true;
        }

        return getMaxSize() <= mChartConfig.countX
                || getLastDrawingIndex() >= getMaxIndex() && mChartConfig.xDistance >= mChartConfig.chartWidth * (getMaxSize() - mChartConfig.countX) / (mChartConfig.countX - 1.0);
    }

    private int getLastDrawingIndex()
    {
        int result = 0;
        for (Integer integer : drawingListMap.keySet())
        {
            int tem = drawingListMap.get(integer).get(drawingListMap.get(integer).size() - 1).getIndex();
            if (tem > result)
            {
                result = tem;
            }
        }
        return result;
    }

    private int getMaxIndex()
    {
        int result = 0;
        for (Integer integer : mChartItemListMap.keySet())
        {
            List<ChartItem> chartItemList = mChartItemListMap.get(integer);
            int tem = chartItemList.isEmpty() ? 0 : chartItemList.get(chartItemList.size() - 1).getIndex();
            if (tem > result)
            {
                result = tem;
            }
        }
        return result;
    }

    private int getMaxSize()
    {
        int result = 0;
        for (Integer integer : mChartConfig.sourceChartItemListMap.keySet())
        {
            int tem = mChartConfig.sourceChartItemListMap.get(integer).size();
            if (tem > result)
            {
                result = tem;
            }
        }
        return result;
    }

    private boolean isStart()
    {
        if (mChartItemListMap == null || mChartItemListMap.isEmpty())
        {
            return true;
        }
        return mChartConfig.startIndex == 0 && mChartConfig.xDistance <= 0;
    }

    private void calculateRealYCount()
    {
        if ((mChartConfig.unitYFormat.equals(ChartItem.UNIT_Y_FORMAT_INT)))
        {
            if (mValueEntity.max - mValueEntity.min < mChartConfig.countY)
            {
                mChartConfig.countY = (int) (mValueEntity.max - mValueEntity.min) + 1;
            }
        }
    }

    /**
     * 画单位
     */
    private void drawYUnit(Canvas canvas)
    {
        calculateRealYCount();
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
            String stringY = String.format(mChartConfig.unitYFormat, startY + i * unitY);
            canvas.drawText(stringY, -ChartConfig.FONT_PADDING, (float) (mChartConfig.chartHeight * (1 - i / (mChartConfig.countY - 1.0))), paint);
        }

        if (!TextUtils.isEmpty(mChartConfig.unitYText))
        {
            canvas.drawText(mChartConfig.unitYText, -ChartConfig.FONT_PADDING, -mChartConfig.unitYHeight - ChartConfig.FONT_PADDING, paint);
        }

        if (mChartConfig.showStandLine)
        {
            //画正常值基准线
            paint.setColor(mChartConfig.standLineLowColor);
            DashPathEffect dashPathEffect = new DashPathEffect(new float[]{12, 6, 12, 6}, 0);
            paint.setPathEffect(dashPathEffect);
            float lowY = mChartConfig.chartHeight * (1 - ((mValueEntity.normalLow - mValueEntity.min) / (mValueEntity.max - mValueEntity.min)));
            Path path1 = new Path();
            path1.moveTo(0, lowY);
            path1.lineTo(mChartConfig.chartWidth, lowY);
            canvas.drawPath(path1, paint);

            paint.setColor(mChartConfig.standLineHighColor);
            float heightY = mChartConfig.chartHeight * (1 - ((mValueEntity.normalHigh - mValueEntity.min) / (mValueEntity.max - mValueEntity.min)));
            Path path2 = new Path();
            path2.moveTo(0, heightY);
            path2.lineTo(mChartConfig.chartWidth, heightY);
            canvas.drawPath(path2, paint);
        }
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

    private void createDrawingPoint()
    {
        pointListMap = new TreeMap<>();
        for (Integer integer : drawingListMap.keySet())
        {
            List<ChartItem> chartItems = drawingListMap.get(integer);
            if (chartItems == null || chartItems.isEmpty())
            {
                continue;
            }
            for (int i = 0; i < chartItems.size(); i++)
            {
                ChartItem currentDrawingItem = chartItems.get(i);
                if (currentDrawingItem instanceof EmptyChartItem)
                {
                    continue;
                }
                Point point = new Point();
                point.source = currentDrawingItem;
                point.index = currentDrawingItem.getIndex();
                point.type = currentDrawingItem.getType();

                int unitX = (int) mChartConfig.unitXDistance;
                float deltaValue = mValueEntity.max - mValueEntity.min;
                float startX = -mChartConfig.xDistance % unitX;

                float valueY = mChartConfig.chartHeight * (currentDrawingItem.getValueY() - mValueEntity.min) / deltaValue;

                float x = startX + (currentDrawingItem.getIndex() - mChartConfig.startIndex) * unitX;
                float y = -valueY;

                point.x = x;
                point.y = y;

                if (pointListMap.containsKey(currentDrawingItem.getType()))
                {
                    pointListMap.get(currentDrawingItem.getType()).add(point);
                }
                else
                {
                    List<Point> pointList = new ArrayList<>();
                    pointList.add(point);
                    pointListMap.put(currentDrawingItem.getType(), pointList);
                }
            }
        }

    }

    @SuppressLint("WrongConstant")
    private void drawLine(Canvas canvas)
    {
        canvas.saveLayer(0, -ChartConfig.DEFAULT_PADDING - mChartConfig.chartHeight - mChartConfig.fontHeightX,
                mChartConfig.chartWidth, mChartConfig.fontHeightX + ChartConfig.DEFAULT_PADDING, null, Canvas.ALL_SAVE_FLAG);

        //for (Integer integer : drawingListMap.keySet())
        //{
           /* List<ChartItem> currentDrawingItems = drawingListMap.get(integer);
            //画X轴单位及点
            if (currentDrawingItems == null || currentDrawingItems.isEmpty())
            {
                continue;
            }*/

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setAntiAlias(true);

            if (pointListMap.isEmpty())
            {
                return;
            }
            for (Integer i : pointListMap.keySet())
            {
                mPath.reset();
                measurePath(pointListMap.get(i));

                float distance = mPathMeasure.getLength();
                if (mPathMeasure.getSegment(0, distance, mPath, true))
                {
                    setPaintColorByType(i);
                    canvas.drawPath(mPath, paint);
                }
            }

        //}
        canvas.restore();
        paint.setColor(Color.WHITE);
    }

    @SuppressLint("WrongConstant")
    private void drawPointNext(List<Point> currentDrawingItems, Canvas canvas)
    {
        paint.setStyle(Paint.Style.FILL);
        float scale;

        for (int i = 0; currentDrawingItems.size() > i; i++)
        {
            Point current = currentDrawingItems.get(i);
            if (mChartConfig.drawNormalLinePoint || current.getType() == ChartItem.LINE_SOURCE)
            {
                scale = scalePoint(current.index) ? 1.5f : 1f;
                float x = xShowCount == 1 ? mChartConfig.chartWidth / 2 : current.x;
                drawPointDetail(canvas, x, current.y, scale, current.type, current.index);
            }
        }
        paint.setColor(Color.BLACK);
    }

    private void drawPointDetail(Canvas canvas, float x, float y, float scale, int dataType, int index)
    {
        setPaintColorByType(dataType);
        canvas.drawCircle(x, y, 12 * scale, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, 6 * scale, paint);
    }

    /*坐标的圆点是否要放大*/
    private boolean scalePoint(int index)
    {
        if (mChartConfig.verticalIndex != index
                || !(mChartConfig.sourceEndIndex < 0 || index <= mChartConfig.sourceEndIndex)
                || isEmptySource(index)
                || !mChartConfig.supportVerticalLine)
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
        return mChartConfig.verticalIndex == index && mChartConfig.supportVerticalLine && scalePoint(index)
                && (mChartConfig.sourceEndIndex < 0 || index <= mChartConfig.sourceEndIndex) && !isEmptySource(index);
    }

    /*此点上是否不存在数据，只有标准线*/
    private boolean isEmptySource(int index)
    {
        List<ChartItem> chartItemList = getChartItemsByIndex(index);
        if (chartItemList == null || chartItemList.isEmpty())
        {
            return true;
        }

        for (ChartItem item : chartItemList)
        {
            if (!(item instanceof EmptyChartItem))
            {
                return false;
            }
        }
        return true;
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

    private void drawDetailText(Canvas canvas, List<Point> points)
    {
        if (mChartConfig.detailTextDrawable != null)
        {
            mChartConfig.detailTextDrawable.setData(points, (int) mChartConfig.chartWidth, (int) mChartConfig.chartHeight);
            mChartConfig.detailTextDrawable.draw(canvas);
        }
    }

    private void drawDate(Canvas canvas, int unitX, float startX)
    {
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(COLOR_TEXT);
        if (!TextUtils.isEmpty(mChartConfig.unitXText))
        {
            canvas.drawText(mChartConfig.unitXText, mChartConfig.chartWidth + +mChartConfig.fontWidthX / 2 + ChartConfig.FONT_PADDING, mChartConfig.fontHeightX, paint);
        }
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.saveLayer(-mChartConfig.fontWidthX / 2, -ChartConfig.DEFAULT_PADDING - mChartConfig.chartHeight - mChartConfig.fontHeightX,
                mChartConfig.chartWidth + mChartConfig.fontWidthX / 2,
                mChartConfig.fontHeightX + ChartConfig.DEFAULT_PADDING, null, Canvas.ALL_SAVE_FLAG);

        //如果只有一条，画在中间
        if (xShowCount == 1)
        {
            ChartItem current = getChartItemsByIndex(0).get(0);
            if (current.getValueX() == null)
            {
                return;
            }
            //画日期
            if (mChartConfig.xUnitType == UnitType.TYPE_NUM)
            {
                canvas.drawText(current.getValueX(), startX + unitX, mChartConfig.fontHeightX, paint);
            }
            else
            {
                canvas.drawText(ChartUtils.getDateDay(current.getValueX()), startX + unitX, mChartConfig.fontHeightX/2, paint);
                canvas.drawText(ChartUtils.getDateHour(current.getValueX()), startX + unitX, mChartConfig.fontHeightX, paint);
            }
            return;
        }

        //否则从最左侧开始画
        for (int i = mChartConfig.startIndex; i <= mChartConfig.endIndex; i++)
        {
            if (getChartItemsByIndex(i).size() == 0)
            {
                return;
            }

            if (!drawXUnit(i))
            {
                continue;
            }

            //ChartItem current = getChartItemsByIndex(i).get(0);
            String xDate = getXDate(getChartItemsByIndex(i));
            //画日期
            if (mChartConfig.xUnitType == UnitType.TYPE_NUM)
            {
                canvas.drawText(xDate, startX + (i - mChartConfig.startIndex) * unitX, mChartConfig.fontHeightX, paint);
            }
            else
            {
                canvas.drawText(ChartUtils.getDateDay(xDate), startX + (i - mChartConfig.startIndex) * unitX, mChartConfig.fontHeightX / 2, paint);
                canvas.drawText(ChartUtils.getDateHour(xDate), startX + (i - mChartConfig.startIndex) * unitX, mChartConfig.fontHeightX, paint);
            }
        }
        canvas.restore();

    }

    private boolean drawXUnit(int index)
    {
        if (index == 0 || index == getMaxIndex())
        {
            return true;
        }
        return index % mChartConfig.perCountX == 0;
    }

    private String getXDate( List<ChartItem> chartItemList)
    {
        if (chartItemList == null || chartItemList.isEmpty())
        {
            return "";
        }
        for (ChartItem item : chartItemList)
        {
            if (!TextUtils.isEmpty(item.getValueX()))
            {
                return item.getValueX();
            }
        }
        return "";
    }

    private List<ChartItem> getChartItemsByIndex(int index)
    {
        List<ChartItem> chartItemList = new ArrayList<>();
        for (Integer integer : mChartItemListMap.keySet())
        {
            for (ChartItem item : mChartItemListMap.get(integer))
            {
                if (item.getIndex() == index)
                {
                    chartItemList.add(item);
                }
            }
        }
        return chartItemList;
    }

    private void measurePath(List<Point> items)
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
        for (int valueIndex = 0; valueIndex < lineSize; ++valueIndex)
        {
            Point point = items.get(valueIndex);

            if (Float.isNaN(currentPointX))
            {
                currentPointX = point.x;
                currentPointY = point.y;
            }
            if (Float.isNaN(previousPointX))
            {
                //是否是第一个点
                if (valueIndex > 0)
                {
                    Point prePoint = items.get(valueIndex - 1);
                    previousPointX = prePoint.x;
                    previousPointY = prePoint.y;
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
                    Point prePrePoint = items.get(valueIndex - 2);
                    prePreviousPointX = prePrePoint.x;
                    prePreviousPointY = prePrePoint.y;
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
                Point nextPoint = items.get(valueIndex + 1);
                nextPointX = nextPoint.x;
                nextPointY = nextPoint.y;
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

    private float getRealMaxByType(float value)
    {
        return (float) Math.ceil(value/ mChartConfig.scaleRate) * mChartConfig.scaleRate;
        /*//心率取10的倍数
        if (type == CHART_TYPE_HEART)
        {
            return (float) (Math.ceil(value / 10) * 10);
        }
        *//*其他取整*//*
        else
        {
            return (float) Math.ceil(value);
        }*/
    }

    private float getRealMinByType(float value)
    {
        return (float) Math.floor(value / mChartConfig.scaleRate) * mChartConfig.scaleRate;
      /*  //心率取10的倍数
        if (type == CHART_TYPE_HEART)
        {
            return (float) (Math.floor(value / 10) * 10);
        }
        *//*其他取整*//*
        else
        {
            return (float) Math.floor(value);
        }*/
    }


    public void setPaintColorByType(int type)
    {
        if (mChartConfig.colorMap.containsKey(type))
        {
            paint.setColor(mChartConfig.colorMap.get(type));
        }
        else
        {
            paint.setColor(mChartConfig.defaultLineColor);
        }
    }

    public static class ValueEntity
    {
        float max;
        float min;
        float normalHigh;
        float normalLow;

        public ValueEntity()
        {
        }

        public ValueEntity(float max, float min, float normalHigh, float normalLow)
        {
            this.max = max;
            this.min = min;
            this.normalHigh = normalHigh;
            this.normalLow = normalLow;
        }
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
        public static final int DEFAULT_PADDING = 20;
        /*文字间距*/
        public static final int FONT_PADDING = 20;

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
        int perCountX = 1;
        /*X轴上的单位类型*/
        private UnitType xUnitType = UnitType.TYPE_DATE;
        /*支持竖线提示*/
        private boolean supportVerticalLine = false;
        /*是否显示标准线*/
        private boolean showStandLine = true;
        /*普通的数据点（ChartItem.type != ChartItem.LINE_SOURCE）是否要画圈圈*/
        private boolean drawNormalLinePoint = true;
        private boolean showFullScreen = true;
        private boolean standardLineCanPoint = false;
        /*标准线*/
        private ValueEntity standValueEntity = new ValueEntity();
        /*y轴取最大值最小值时的缩放比例(取10，100，100)，默认取整，即不缩放*/
        private float scaleRate = 1;
        /*Y轴上显示的数值的格式*/
        private String unitYFormat = ChartItem.UNIT_Y_FORMAT_FLOAT_1;
        /*上面那条标准线颜色*/
        private int standLineLowColor = Color.rgb(78, 193, 242);
        /*下面那条标准线颜色*/
        private int standLineHighColor = Color.rgb(78, 193, 242);

        /*滑动模式*/
        private MoveType mMoveType = MoveType.TYPE_LINE;

        private int defaultLineColor = Color.rgb(78, 193, 242);

        private Map<Integer, Integer> colorMap = new HashMap<>();

        /*用于控制竖线可滑动的范围*/
        private int sourceEndIndex = -1;

        private ChartDetailDrawable detailTextDrawable;



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
        private int verticalIndex = -1;
        /*竖线在正在的数据中的位置*/
        //private int drawingVerticalIndex;

        private int startIndex = 0;
        private int endIndex;

        /*---------------------曲线数据------------------------*/
        private Map<Integer, List<ChartItem>> sourceChartItemListMap = new HashMap<>();


        void init(ValueEntity valueEntity, ChartView chartView)
        {
            totalHeight = chartView.getHeight();
            totalWidth = chartView.getWidth();

            Paint paint = new Paint();
            paint.setFakeBoldText(false);
            paint.setStrokeWidth(0);
            paint.setTextSize(DEFAULT_FONT_SIZE);
            float fontHeight = ChartUtils.calculateFontHeight(paint);
            fontWidthY = Math.max(ChartUtils.calculateFontWidth(paint, valueEntity.max + ""), ChartUtils.calculateFontWidth(paint, unitYText));
            if (xUnitType == UnitType.TYPE_DATE)
            {
                fontWidthX = ChartUtils.calculateFontWidth(paint, "00:00:00");
                fontHeightX = 2 * fontHeight;
            }
            else
            {
                fontWidthX = ChartUtils.calculateFontWidth(paint, "99");
                fontHeightX = fontHeight;
            }
            if (!TextUtils.isEmpty(unitYText))
            {
                unitYHeight = fontHeight;
            }

            if (!TextUtils.isEmpty(unitXText))
            {
                unitXWidth = ChartUtils.calculateFontWidth(paint, unitXText + FONT_PADDING);
            }

            startX = fontWidthY + DEFAULT_PADDING + FONT_PADDING;
            startY = DEFAULT_PADDING + 2 * unitYHeight + FONT_PADDING;
            chartWidth = totalWidth - fontWidthY - 2 * DEFAULT_PADDING - unitXWidth - fontWidthX / 2;
            chartHeight = totalHeight - 2 * unitYHeight - 2 * DEFAULT_PADDING - fontHeightX - 2 * FONT_PADDING;

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
            if (!isInit && chartView.mChartItemListMap != null && !chartView.mChartItemListMap.isEmpty())
            {
                isInit = true;
                if (chartView.xShowCount >= countX)
                {
                    xDistance = (float) (chartWidth * (chartView.getMaxSize() - chartView.xShowCount) / (chartView.xShowCount - 1.0));
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

        /*设置X轴上每多少个点画一个刻度（用于数据量过多导致x轴刻度重叠时，设置此参数，从而隔几个刻度画一次）*/
        public ChartConfigBuilder setPerCountX(int count)
        {
            mChartConfig.perCountX = count;
            return this;
        }


        /*设置X轴的刻度数量（实际展示的数量）*/
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

        public ChartConfigBuilder addLine(List<ChartItem> chartItems, int type)
        {
            mChartConfig.sourceChartItemListMap.put(type, chartItems);
            return this;
        }

        public ChartConfigBuilder addMultTypeLine(List<ChartItem> chartItems)
        {
            mChartConfig.sourceChartItemListMap.put(LINE_TYPE_MULT, chartItems);
            return this;
        }

        public ChartConfigBuilder setColor(int type, int color)
        {
            mChartConfig.colorMap.put(type, color);
            return this;
        }

        public ChartConfigBuilder setColorAll(int color)
        {
            mChartConfig.defaultLineColor = color;
            return this;
        }

        public ChartConfigBuilder setDetailDrawable(ChartDetailDrawable drawable)
        {
            mChartConfig.detailTextDrawable = drawable;
            return this;
        }

        public ChartConfigBuilder showStandLine(boolean showStandLine)
        {
            mChartConfig.showStandLine = showStandLine;
            return this;
        }

        public ChartConfigBuilder setStandLineValue(ValueEntity standLineValue)
        {
            mChartConfig.standValueEntity = standLineValue;
            return this;
        }

        public ChartConfigBuilder setYScaleRate(float scaleRate)
        {
            mChartConfig.scaleRate = scaleRate;
            return this;
        }

        public ChartConfigBuilder setHighStandLineColor(int color)
        {
            mChartConfig.standLineHighColor = color;
            return this;
        }

        public ChartConfigBuilder setLowStandLineColor(int color)
        {
            mChartConfig.standLineLowColor = color;
            return this;
        }

        public ChartConfigBuilder setUnitYFormat(String format)
        {
            mChartConfig.unitYFormat = format;
            return this;
        }

        public ChartConfigBuilder drawNormalLinePoint(boolean drawNormalLinePoint)
        {
            mChartConfig.drawNormalLinePoint = drawNormalLinePoint;
            return this;
        }

        /*在数据少于设定的数量时是居中显示还是整个宽度显示*/
        public ChartConfigBuilder showFullScreen(boolean showFullScreen)
        {
            mChartConfig.showFullScreen = showFullScreen;
            return this;
        }

        /*曲线标准线是否可以定位到（孕产妇、婴幼儿等的标准线是一组曲线，此时是否支持定位到此标准线的每个点）*/
        public ChartConfigBuilder standardLineCanPoint(boolean standardLineCanPoint)
        {
            mChartConfig.standardLineCanPoint = standardLineCanPoint;
            return this;
        }

        public ChartConfig build()
        {
            return mChartConfig;
        }
    }

    public interface OnClickPointListener
    {
        void onClick(List<ChartItem> chartItem);
    }

    public OnClickPointListener onClickPointListener;

    public void setOnClickPointListener(OnClickPointListener onClickPointListener)
    {
        this.onClickPointListener = onClickPointListener;
    }

    public static class Point implements Cloneable
    {
        private float x;
        private float y;
        private int index;
        private int type;
        private ChartItem source;

        public float getX()
        {
            return x;
        }

        public float getY()
        {
            return y;
        }

        public int getType()
        {
            return type;
        }

        public ChartItem getSource()
        {
            return source;
        }


        @Override
        public Object clone()
        {
            try
            {
                return super.clone();
            }
            catch (CloneNotSupportedException e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }

}
