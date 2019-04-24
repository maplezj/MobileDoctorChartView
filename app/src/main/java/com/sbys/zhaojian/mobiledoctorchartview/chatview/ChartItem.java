package com.sbys.zhaojian.mobiledoctorchartview.chatview;

import android.support.annotation.NonNull;

/**
 * Created by zhaojian on 2018/4/26.
 */

public class ChartItem implements Comparable<ChartItem>
{
    public static final int TYPE_NORMAL = 0;
    /*X轴不重复的曲线1*/
    public static final int TYPE_SINGLE_ONE = 1;
    /*X轴不重复的曲线2*/
    public static final int TYPE_SINGLE_TWO = 2;
    /*X轴不重复的曲线3*/
    public static final int TYPE_SINGLE_THREE = 3;
    private float value;
    private String date;
    private int type;
    private int index;
    private int jcly;//监测来源
    private int pos;//点的角标

    public ChartItem(float value, String date)
    {
        this(value, date, TYPE_NORMAL);
    }



    public ChartItem(float value, String date, int type)
    {
        this.value = value;
        this.date = date;
        this.type = type;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public float getValue()
    {
        return value;
    }

    public int getJcly() {
        return jcly;
    }

    public void setJcly(int jcly) {
        this.jcly = jcly;
    }

    public String getDate()
    {
        return date;
    }

    public int getType()
    {
        return type;
    }

    public int getIndex()
    {
        return index;
    }



    public void setIndex(int index)
    {
        this.index = index;
    }

    @Override
    public int compareTo(@NonNull ChartItem o)
    {
        if (value > o.value)
        {
            return 1;
        }
        else if (value < o.value)
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }
}
