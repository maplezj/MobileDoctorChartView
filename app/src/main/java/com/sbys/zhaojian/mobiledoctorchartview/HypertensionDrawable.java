package com.sbys.zhaojian.mobiledoctorchartview;

import android.content.Context;

import com.github.MDChartView.chatview.ChartDetailDrawable;
import com.github.MDChartView.chatview.ChartItem;
import com.github.MDChartView.chatview.ChartView;

/**
 * @author zhaojian
 * @time 2019/5/5 17:55
 * @describe
 */
public class HypertensionDrawable extends ChartDetailDrawable
{

    public HypertensionDrawable(Context context)
    {
        super(context);
    }

    @Override
    protected void setPointXY()
    {
        if (mChartItemList == null || mChartItemList.isEmpty())
        {
            return;
        }
        for (ChartView.Point point : mChartItemList)
        {
            if (point.getType() == ChartItem.LINE_1)
            {
                pointX = (int) point.getX();
                pointY = (int) point.getY();
            }
        }
    }

    @Override
    protected String createContent()
    {
        String date = "";
        int ssy = 0;
        int szy = 0;
        for (ChartView.Point point : mChartItemList)
        {
            if (point.getType() == ChartItem.LINE_1)
            {
                date = point.getSource().getValueX();
                ssy = (int) point.getSource().getValueY();
            }
            else if (point.getType() == ChartItem.LINE_SOURCE)
            {
                szy = (int) point.getSource().getValueY();
            }
        }
        String format =  "随访日期：%s\n血         糖：%s/%smmHg\n用药信息：头孢、埃斯皮里、感冒药、发烧药";
        return String.format(format, date, ssy, szy);
        //return  "随访日期：2019-02-12\n血         压：110/88mmHg\n用药信息：头孢、埃斯皮里、感冒药、发烧药";
    }
}
