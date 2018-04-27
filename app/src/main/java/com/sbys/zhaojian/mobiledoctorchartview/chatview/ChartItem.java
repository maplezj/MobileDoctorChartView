package com.sbys.zhaojian.mobiledoctorchartview.chatview;

import android.support.annotation.NonNull;

/**
 * Created by zhaojian on 2018/4/26.
 */

public class ChartItem implements Comparable<ChartItem>
{
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_ONE = 1;
    public static final int TYPE_TWO = 2;
    private float value;
    private String date;
    private int type;

    public ChartItem(float value, String date)
    {
        this.value = value;
        this.date = date;
    }

    public float getValue()
    {
        return value;
    }

    public String getDate()
    {
        return date;
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
