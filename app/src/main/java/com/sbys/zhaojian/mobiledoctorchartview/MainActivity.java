package com.sbys.zhaojian.mobiledoctorchartview;

import android.app.Activity;
import android.os.Bundle;

import com.sbys.zhaojian.mobiledoctorchartview.chatview.ChartItem;
import com.sbys.zhaojian.mobiledoctorchartview.chatview.ChartView;

import java.util.ArrayList;

public class MainActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ChartView chartView = findViewById(R.id.chatView);
        ArrayList<ChartItem> chartItems = new ArrayList<>();
        chartItems.add(new ChartItem(100, "11/11",ChartItem.TYPE_ONE));
        chartItems.add(new ChartItem(80, "11/12"));
        chartItems.add(new ChartItem(50, "11/13",ChartItem.TYPE_ONE));
        chartItems.add(new ChartItem(60, "11/14"));
        chartItems.add(new ChartItem(80, "11/15",ChartItem.TYPE_ONE));
        chartItems.add(new ChartItem(50, "11/16"));
        chartItems.add(new ChartItem(110, "11/17",ChartItem.TYPE_ONE));
        chartItems.add(new ChartItem(40, "11/18",ChartItem.TYPE_ONE));
        chartItems.add(new ChartItem(120, "11/19"));
        chartItems.add(new ChartItem(70, "11/20",ChartItem.TYPE_ONE));
        chartView.setData(chartItems, ChartView.CHART_TYPE_HEART);
    }
}
