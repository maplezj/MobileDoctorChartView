package com.sbys.zhaojian.mobiledoctorchartview.chatview;

import android.support.annotation.NonNull;

/**
 * Created by zhaojian on 2018/4/26.
 */

public class ChartItem implements Comparable<ChartItem>
{
    /*源数据，在有多条先的情况下以那条线为准*/
    public static final int LINE_SOURCE = 0;

    public static final int LINE_1 = 1;
    /*X轴不重复的曲线1*/
    public static final int LINE_2 = 2;
    /*X轴不重复的曲线2*/
    public static final int LINE_3 = 3;
    /*X轴不重复的曲线3*/
    public static final int LINE_4 = 4;
    private float value;
    private String date;
    private int type;
    private int index;

    public ChartItem(float value, String date)
    {
        this(value, date, LINE_1);
    }



    public ChartItem(float value, String date, int type)
    {
        this.value = value;
        this.date = date;
        this.type = type;
    }

    public float getValue()
    {
        return value;
    }

    public String getDate()
    {
        return date;
    }

    public void setType(int type)
    {
        this.type = type;
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
