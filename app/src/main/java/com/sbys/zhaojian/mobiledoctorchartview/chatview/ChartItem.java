package com.sbys.zhaojian.mobiledoctorchartview.chatview;

import android.support.annotation.NonNull;

/**
 * Created by zhaojian on 2018/4/26.
 */

public class ChartItem implements Comparable<ChartItem>
{
    /*源数据，在有多条先的情况下以那条线为准,如果所画的线中有此种类型的线，
    * 则此线范围外的点点击无效*/
    public static final int LINE_SOURCE = 0;

    public static final int LINE_1 = 1;
    public static final int LINE_2 = 2;
    public static final int LINE_3 = 3;
    public static final int LINE_4 = 4;
    private float valueY;
    private String valueX;
    private int type;
    private int index;

    public ChartItem(float valueY, String valueX)
    {
        this(valueY, valueX, LINE_1);
    }



    public ChartItem(float valueY, String valueX, int type)
    {
        this.valueY = valueY;
        this.valueX = valueX;
        this.type = type;
    }

    public float getValueY()
    {
        return valueY;
    }

    public String getValueX()
    {
        return valueX;
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
        if (valueY > o.valueY)
        {
            return 1;
        }
        else if (valueY < o.valueY)
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }
}
