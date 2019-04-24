package com.sbys.zhaojian.mobiledoctorchartview.chatview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.sbys.loggerlib.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static  com.sbys.zhaojian.mobiledoctorchartview.chatview.ChartView.ChartEntity.X_COUNT;
import static   com.sbys.zhaojian.mobiledoctorchartview.chatview.ChartView.ChartEntity.Y_COUNT;

/**
 * Created by zhaojian on 2018/4/26.
 */

public class ChartView extends View {
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
    private ChartEntity mChartEntity = new ChartEntity();
    private GestureDetector mGestureDetector;
    private int type;
    private int xShowCount = 0;
    private Context context;
    List<ChartItem> showingItems;

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    private void initView() {
        mGestureDetector = new GestureDetector(mGestureListener);
    }

    public void setEmpty(int type) {
        setMultiData(new ArrayList<List<ChartItem>>(), type);
    }

    public void setData(List<ChartItem> chartItemList, int type) {
        if (chartItemList == null) {
            Logger.d("data is null");
        }
        List<List<ChartItem>> chartItemListList = new ArrayList<>();
        chartItemListList.add(chartItemList);
        setMultiData(chartItemListList, type);

    }

    public void setMultiData(List<List<ChartItem>> chartItemListList) {
        setMultiData(chartItemListList, CHART_TYPE_PRESSURE);
    }

    private void setMultiData(List<List<ChartItem>> chartItemListList, int type) {
        if (checkValidate(chartItemListList, type)) {
            Logger.d("list size:" + chartItemListList.size() + "|type:" + type);
            this.type = type;
            initData(chartItemListList);
        }
    }

    private void initData(List<List<ChartItem>> chartItemListList) {
        currentDrawingItemsList.clear();
        mChartItemListList.clear();
        mChartEntity.isInit = false;
        xShowCount = 0;

        mChartItemListList.addAll(chartItemListList);
        resetValueEntity();
        for (List<ChartItem> chartItems : chartItemListList) {
            if (chartItems.size() <= X_COUNT) {
                currentDrawingItemsList.add(chartItems);
                if (xShowCount < chartItems.size()) {
                    xShowCount = chartItems.size();
                }
            } else {
                currentDrawingItemsList.add(chartItems.subList(chartItems.size() - X_COUNT, chartItems.size()));
                xShowCount = X_COUNT;
            }
        }
        updateValueEntity();
        invalidate();
    }

    /**
     * 更新最大值最小值
     */
    private void updateValueEntity() {
        List<ChartItem> allDrawingItems = new ArrayList<>();
        for (List<ChartItem> chartItems : currentDrawingItemsList) {
            allDrawingItems.addAll(chartItems);
        }
        if (!allDrawingItems.isEmpty()) {
            ChartItem realMax = Collections.max(allDrawingItems);
            ChartItem realMin = Collections.min(allDrawingItems);
            if (realMax.getValue() > mValueEntity.max) {
                mValueEntity.max = getRealMaxByType(realMax.getValue(), type);
            }
            if (realMin.getValue() < mValueEntity.min) {
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
    private boolean checkValidate(List<List<ChartItem>> chartItemListList, int type) {
        if (chartItemListList == null) {
            Logger.d("data is null");
            return false;
        }
        if (type == CHART_TYPE_PRESSURE) {
            if (chartItemListList.isEmpty()) {
                return true;
            }
            if (chartItemListList.size() != 2) {
                Logger.d("checkValidate: 血压数据必须为2条" + "|size:" + chartItemListList.size());
                return false;
            } else if (chartItemListList.get(0).size() != chartItemListList.get(1).size()) {
                Logger.d("checkValidate: 血压大的两条数据长度必须一致");
                return false;
            }
            return true;
        }
        return true;
    }

    private void updateData() {
        resetValueEntity();
        int outCount = (int) Math.ceil((mChartEntity.xDistance + 0.001) / mChartEntity.unitX);
        for (int i = 0; i < mChartItemListList.size(); i++) {
            List<ChartItem> chartItems = mChartItemListList.get(i);
            if (chartItems.size() > X_COUNT + outCount) {
                currentDrawingItemsList.remove(i);
                currentDrawingItemsList.add(i, chartItems.subList(outCount - 1, outCount + X_COUNT));
            } else {
                currentDrawingItemsList.remove(i);
                currentDrawingItemsList.add(i, chartItems.subList(outCount - 1, chartItems.size()));
            }
        }

        updateValueEntity();
    }

    private void resetValueEntity() {
        switch (type) {
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
    protected void onDraw(Canvas canvas) {
        mChartEntity.init();
        drawWrapper(canvas, paint);
        drawUnit(canvas, paint);
        drawLine(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    /**
     * 画单位
     */
    private void drawUnit(Canvas canvas, Paint paint) {
        paint.setFakeBoldText(false);
        paint.setTextSize(CommonUtil.dp2px(getContext(), 7));
        paint.setStrokeWidth(0);
        canvas.translate(-mChartEntity.fontHeightY, 0);
        //画Y轴单位
        float startY = mValueEntity.min;
        float unitY = (mValueEntity.max - mValueEntity.min) / (Y_COUNT - 1);
        for (int i = 0; i < Y_COUNT; i++) {
            String stringY = String.format("%.1f", startY + i * unitY);
            canvas.drawText(stringY, 0, (float) (mChartEntity.chartHeight * (1 - i / (Y_COUNT - 1.0))), paint);
        }
        //画正常值基准线
        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{16, 8, 16, 8}, 0);
        paint.setPathEffect(dashPathEffect);
        float lowY = mChartEntity.chartHeight * (1 - ((mValueEntity.normalLow - mValueEntity.min) / (mValueEntity.max - mValueEntity.min)));
//        canvas.drawLine(mChartEntity.fontHeightY, lowY, mChartEntity.chartWidth + mChartEntity.fontHeightY, lowY, paint);
        Path path1 = new Path();
        path1.moveTo(mChartEntity.fontHeightY, lowY);
        path1.lineTo(mChartEntity.chartWidth + mChartEntity.fontHeightY, lowY);
        canvas.drawPath(path1, paint);
        float heightY = mChartEntity.chartHeight * (1 - ((mValueEntity.normalHigh - mValueEntity.min) / (mValueEntity.max - mValueEntity.min)));
//        canvas.drawLine(mChartEntity.fontHeightY, heightY, mChartEntity.chartWidth + mChartEntity.fontHeightY, heightY, paint);
        Path path2 = new Path();
        path2.moveTo(mChartEntity.fontHeightY, heightY);
        path2.lineTo(mChartEntity.chartWidth + mChartEntity.fontHeightY, heightY);
        canvas.drawPath(path2, paint);
    }

    /**
     * 画外壳
     */
    private void drawWrapper(Canvas canvas, Paint paint) {
        paint.reset();
        paint.setFakeBoldText(false);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.rgb(153, 153, 153));
        paint.setStrokeWidth(2);
        canvas.translate(mChartEntity.startX, mChartEntity.startY);
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(0, mChartEntity.chartHeight);
        path.lineTo(mChartEntity.chartWidth, mChartEntity.chartHeight);
        canvas.drawPath(path, paint);
    }

    float deltaValue;

    @SuppressLint("WrongConstant")
    private void drawLine(Canvas canvas) {
        paint.setPathEffect(null);
        if (mChartItemListList == null || mChartItemListList.isEmpty()) {
            return;
        }

        canvas.translate(mChartEntity.fontHeightY, mChartEntity.chartHeight + mChartEntity.fontHeightX);
        paint.setStyle(Paint.Style.FILL);

        int unitX = (int) mChartEntity.unitX;
        deltaValue = mValueEntity.max - mValueEntity.min;
        float startX = -mChartEntity.xDistance % unitX;
        canvas.saveLayer(-calculateFontWith(currentDrawingItemsList.get(0).get(0).getDate()) / 2, -mChartEntity.padding - mChartEntity.chartHeight - mChartEntity.fontHeightX,
                mChartEntity.chartWidth + calculateFontWith(currentDrawingItemsList.get(0).get(currentDrawingItemsList.get(0).size() - 1).getDate()) / 2,
                0, null, Canvas.ALL_SAVE_FLAG);
        drawDate(canvas, unitX, startX);

        for (List<ChartItem> currentDrawingItems : currentDrawingItemsList) {
            //画X轴单位及点
            if (currentDrawingItems == null || currentDrawingItems.isEmpty()) {
                return;
            }

            //画曲线
            canvas.translate(0, -mChartEntity.fontHeightX);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            List<List<ChartItem>> itemsList = createLists(currentDrawingItems);
            if (itemsList == null || itemsList.isEmpty()) {
                return;
            }
            for (List<ChartItem> subChartItems : itemsList) {
                mPath.reset();
                measurePath(subChartItems);
                float distance = mPathMeasure.getLength();
                if (mPathMeasure.getSegment(0, distance, mPath, true)) {
                    setPaintColorByType(subChartItems.get(0).getType());
                    canvas.drawPath(mPath, paint);
                }
            }

            paint.setStyle(Paint.Style.FILL);
            canvas.translate(0, mChartEntity.fontHeightX);
            drawPoint(currentDrawingItems, canvas, unitX, startX, deltaValue);
        }
        paint.setColor(Color.WHITE);

    }

    @SuppressLint("WrongConstant")
    private void drawPoint(List<ChartItem> currentDrawingItems, Canvas canvas, int unitX, float startX, float deltaValue)
    {
        showingItems = currentDrawingItems;
        //画点
        canvas.saveLayer(0, -mChartEntity.padding - mChartEntity.chartHeight - mChartEntity.fontHeightX,
                mChartEntity.chartWidth,
                6, null, Canvas.ALL_SAVE_FLAG);

        //如果只有一条，画在中间
        if (xShowCount == 1)
        {
            ChartItem current = currentDrawingItems.get(0);
            float valueY = mChartEntity.chartHeight * (current.getValue() - mValueEntity.min) / deltaValue;
            setPaintColorByType(current.getType());
            canvas.drawCircle(startX + unitX, -valueY - mChartEntity.fontHeightX, 12, paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(startX + unitX, -valueY - mChartEntity.fontHeightX, 6, paint);
            return;
        }

        //否则从最左侧开始画
        for (int i = 0; currentDrawingItems.size() > i; i++)
        {
            ChartItem current = currentDrawingItems.get(i);
            float valueY = mChartEntity.chartHeight * (current.getValue() - mValueEntity.min) / deltaValue;
            setPaintColorByType(current.getType());
            canvas.drawCircle(startX + i * unitX, -valueY - mChartEntity.fontHeightX, 12, paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(startX + i * unitX, -valueY - mChartEntity.fontHeightX, 6, paint);
        }
        canvas.restore();
        paint.setColor(Color.BLACK);
    }

    private void drawDate(Canvas canvas, int unitX, float startX) {
        List<ChartItem> currentDrawingItems = currentDrawingItemsList.get(0);

        //如果只有一条，画在中间
        if (xShowCount == 1) {
            ChartItem current = currentDrawingItems.get(0);
            //画日期
            canvas.drawText(DateUtil.getDateDay(current.getDate()), startX + unitX - calculateFontWith(current.getDate()) / 2, 0, paint);
            canvas.drawText(DateUtil.getDateHour(current.getDate()), startX + unitX - calculateFontWith(current.getDate()) / 2, calculateFontHeight(current.getDate()), paint);
            return;
        }

        //否则从最左侧开始画
        for (int i = 0; currentDrawingItems.size() > i; i++) {
            ChartItem current = currentDrawingItems.get(i);
            //画日期
            canvas.drawText(current.getDate(), startX + i * unitX - calculateFontWith(current.getDate()) / 2, 0, paint);
        }
    }

    private List<List<ChartItem>> createLists(List<ChartItem> currentDrawingItems) {
        if (currentDrawingItems == null || currentDrawingItems.isEmpty()) {
            return null;
        }
        List<List<ChartItem>> lists = new ArrayList<>();
        List<ChartItem> normalList = new ArrayList<>();
        List<ChartItem> typeOneList = new ArrayList<>();
        List<ChartItem> typeTwoList = new ArrayList<>();
        List<ChartItem> typeThreeList = new ArrayList<>();
        for (int i = 0; i < currentDrawingItems.size(); i++) {
            ChartItem currentDrawingItem = currentDrawingItems.get(i);
            currentDrawingItem.setIndex(i);
            switch (currentDrawingItem.getType()) {
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
        if (!normalList.isEmpty()) {
            lists.add(normalList);
        }
        if (!typeOneList.isEmpty()) {
            lists.add(typeOneList);
        }
        if (!typeTwoList.isEmpty()) {
            lists.add(typeTwoList);
        }
        if(!typeThreeList.isEmpty()){
            lists.add(typeThreeList);
        }
        return lists;
    }

    private void measurePath(List<ChartItem> items) {
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
        int unitX = (int) mChartEntity.unitX;
        float deltaValue = mValueEntity.max - mValueEntity.min;
        float startX = -mChartEntity.xDistance % unitX;
        for (int valueIndex = 0; valueIndex < lineSize; ++valueIndex) {
            ChartItem currentItem = items.get(valueIndex);

            if (Float.isNaN(currentPointX)) {
                float valueY = mChartEntity.chartHeight * (currentItem.getValue() - mValueEntity.min) / deltaValue;

                currentPointX = startX + currentItem.getIndex() * unitX;
                currentPointY = -valueY;
            }
            if (Float.isNaN(previousPointX)) {
                //是否是第一个点
                if (valueIndex > 0) {
                    ChartItem preItem = items.get(valueIndex - 1);
                    previousPointX = startX + (currentItem.getIndex() - preItem.getIndex()) * unitX;
                    previousPointY = -mChartEntity.chartHeight * (preItem.getValue() - mValueEntity.min) / deltaValue;
                } else {
                    //是的话就用当前点表示上一个点
                    previousPointX = currentPointX;
                    previousPointY = currentPointY;
                }
            }

            if (Float.isNaN(prePreviousPointX)) {
                //是否是前两个点
                if (valueIndex > 1) {
                    ChartItem prePreItem = items.get(valueIndex - 2);
                    //Point point = mPointList.get(valueIndex - 2);
                    prePreviousPointX = startX + (currentItem.getIndex() - prePreItem.getIndex()) * unitX;
                    prePreviousPointY = -mChartEntity.chartHeight * (prePreItem.getValue() - mValueEntity.min) / deltaValue;
                } else {
                    //是的话就用当前点表示上上个点
                    prePreviousPointX = previousPointX;
                    prePreviousPointY = previousPointY;
                }
            }

            // 判断是不是最后一个点了
            if (valueIndex < lineSize - 1) {
                ChartItem lastItem = items.get(valueIndex + 1);
                float valueY = mChartEntity.chartHeight * (lastItem.getValue() - mValueEntity.min) / deltaValue;

                //Point point = mPointList.get(valueIndex + 1);
                nextPointX = startX + (lastItem.getIndex()) * unitX;
                nextPointY = -valueY;
            } else {
                //是的话就用当前点表示下一个点
                nextPointX = currentPointX;
                nextPointY = currentPointY;
            }

            if (valueIndex == 0) {
                // 将Path移动到开始点
                mPath.moveTo(currentPointX, currentPointY);
                mAssistPath.moveTo(currentPointX, currentPointY);
            } else {
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

    private float calculateFontWith(String font) {
        Rect bounds = new Rect();
        paint.getTextBounds(font, 0, font.length(), bounds);
        return bounds.width();
    }

    private float calculateFontHeight(String font) {
        Rect bounds = new Rect();
        paint.getTextBounds(font, 0, font.length(), bounds);
        return bounds.height();
    }

    private float getRealMaxByType(float value, int type) {
        //心率取10的倍数
        if (type == CHART_TYPE_HEART) {
            return (float) (Math.ceil(value / 10) * 10);
        }
        /*其他取整*/
        else {
            return (float) Math.ceil(value);
        }
    }

    private float getRealMinByType(float value, int type) {
        //心率取10的倍数
        if (type == CHART_TYPE_HEART) {
            return (float) (Math.floor(value / 10) * 10);
        }
        /*其他取整*/
        else {
            return (float) Math.floor(value);
        }
    }

    private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.OnGestureListener() {
        int index;

        @Override
        public boolean onDown(MotionEvent e) {
            if (onClickPointListener == null)
            {
                return false;
            }
            int range = 50;
            float eventX = e.getX() - mChartEntity.startX;
            float eventY = e.getY() - mChartEntity.startY - mChartEntity.chartHeight - mChartEntity.fontHeightX;
            float unitX = mChartEntity.unitX;
            float startX = -mChartEntity.xDistance % unitX;
            if (showingItems != null) {
                for (int i = 0; showingItems.size() > i; i++) {
                    index = showingItems.get(i).getPos();
                    ChartItem current = showingItems.get(i);
                    float valueY = -(mChartEntity.chartHeight * (current.getValue() - mValueEntity.min) / deltaValue) - mChartEntity.fontHeightX;
                    float valueX = startX + i * unitX;
                    if (eventX >= valueX - range && eventX <= valueX + range &&
                            eventY >= valueY - range && eventY <= valueY + range) {//每个节点周围8dp都是可点击区域
                        onClickPointListener.onClick(showingItems.get(i).getPos(), showingItems.get(i).getJcly());
                        return true;
                    }
                }
            }
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        /**
         * Touch了滑动时触发
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Logger.d("onScroll-----------------> distanceX:" + distanceX + "|distanceY" + distanceY);
            //如果向右滑动，并且已经滑动到最后一个，则不让滑动
            if (distanceX > 0 && isEnd()) {
                Logger.d("onScroll-----------------> is end");
                return true;
            }
            //如果向左滑动，并且已经滑动到第一个，则不让滑动
            if (distanceX < 0 && isStart()) {
                Logger.d("onScroll------------------> is start");
                return true;
            }
            mChartEntity.xDistance = mChartEntity.xDistance + distanceX;
            if (mChartEntity.xDistance < 0) {
                mChartEntity.xDistance = 0;
            }
            updateData();
            postInvalidate();
            return true;
        }

        private boolean isEnd() {
            if (mChartItemListList == null || mChartItemListList.isEmpty()) {
                return true;
            }
            List<ChartItem> chartItems = mChartItemListList.get(0);
            List<ChartItem> currentDrawingItems = currentDrawingItemsList.get(0);

            return chartItems.size() <= X_COUNT
                    || currentDrawingItems.get(currentDrawingItems.size() - 1).equals(chartItems.get(chartItems.size() - 1)) && mChartEntity.xDistance >= mChartEntity.chartWidth * (chartItems.size() - X_COUNT) / (X_COUNT - 1.0);
        }

        private boolean isStart() {
            if (mChartItemListList == null || mChartItemListList.isEmpty()) {
                return true;
            }
            List<ChartItem> chartItems = mChartItemListList.get(0);
            List<ChartItem> currentDrawingItems = currentDrawingItemsList.get(0);
            return currentDrawingItems.get(0).equals(chartItems.get(0)) && mChartEntity.xDistance <= 0;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }
    };

    public void setPaintColorByType(int type) {
        switch (type) {
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

    private static class DefaultValueEntity {
        float max;
        float min;
        float normalHigh;
        float normalLow;
    }

    class ChartEntity {
        /*x轴刻度数量*/
        public static final int X_COUNT = 7;
        /*y轴刻度数量*/
        public static final int Y_COUNT = 6;
        /*是否已经初始化（只初始化一次）*/
        boolean isInit = false;

        /*总宽度*/
        float totalWidth;
        /*总高度*/
        float totalHeight;
        /*边距*/
        float padding;
        /*Y坐标文字高度*/
        float fontHeightY;
        /*X坐标文字高度*/
        float fontHeightX;
        /*x起始位置*/
        float startX;
        /*Y起始位置*/
        float startY;
        /*图表宽度*/
        float chartWidth;
        /*图表高度*/
        float chartHeight;
        /*x轴距*/
        float unitX;
        /*y轴距*/
        float unitY;
        /*X轴滑动的距离*/
        private float xDistance = 0;

        void init() {
            totalHeight = getHeight();
            totalWidth = getWidth();
            padding = totalWidth / 30;
            fontHeightY = totalWidth / 15;
            fontHeightX = totalWidth / 15;
            startX = fontHeightY + padding;
            startY = padding;
            chartWidth = totalWidth - (2 * padding + fontHeightY);
            chartHeight = totalHeight - 2 * padding - fontHeightX;

            //如果只有一条数据，则分成两段，值放中间
            if (xShowCount <= 1) {
                unitX = (int) chartWidth / 2;
                chartWidth = unitX * 2;
            }
            //否则安正常流程处理
            else {
                //X的每个刻度取整
                unitX = (int) chartWidth / (xShowCount - 1);
                //X刻度取整后，图表宽度需根据刻度重新计算，取整
                chartWidth = unitX * (xShowCount - 1);
            }

            unitY = chartHeight / (Y_COUNT - 1);
            if (!isInit && mChartItemListList != null && !mChartItemListList.isEmpty()) {
                isInit = true;
                if (xShowCount >= X_COUNT) {
                    mChartEntity.xDistance = (float) (mChartEntity.chartWidth * (mChartItemListList.get(0).size() - xShowCount) / (xShowCount - 1.0));
                } else {
                    mChartEntity.xDistance = 0;
                }
            }
        }
    }

    public interface OnClickPointListener {
        void onClick(int index, int jcly);
    }

    public OnClickPointListener onClickPointListener;

    public void setOnClickPointListener(OnClickPointListener onClickPointListener) {
        this.onClickPointListener = onClickPointListener;
    }

}
