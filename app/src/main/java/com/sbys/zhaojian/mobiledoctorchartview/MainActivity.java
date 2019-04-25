package com.sbys.zhaojian.mobiledoctorchartview;

import android.app.Activity;
import android.os.Bundle;

import com.sbys.zhaojian.mobiledoctorchartview.chatview.ChartItem;
import com.sbys.zhaojian.mobiledoctorchartview.chatview.ChartView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ChartView chartView = findViewById(R.id.chatView);
        chartView.setEmpty(ChartView.CHART_TYPE_HEART);
        drawDouble(chartView);
        //drawDoubleSpecial(chartView);
        //drawSingle(chartView);
    }

    /**
     * 画单条曲线
     * @param chartView
     */
    private void drawSingle(ChartView chartView)
    {
        ArrayList<ChartItem> chartItems = new ArrayList<>();

        chartItems.add(new ChartItem(100, "11/11 14:22:33"));
        chartItems.add(new ChartItem(80, "11/12 14:22"));
        chartItems.add(new ChartItem(50, "11/13 14:22"));
        chartItems.add(new ChartItem(60, "11/14 14:22"));
        chartItems.add(new ChartItem(80, "11/15 14:22"));
        chartItems.add(new ChartItem(50, "11/16 14:22"));
        chartItems.add(new ChartItem(110, "11/17 14:22"));
        chartItems.add(new ChartItem(40, "11/18 14:22"));
        chartItems.add(new ChartItem(120, "11/19 14:22:55"));
        chartItems.add(new ChartItem(70, "11/20 14:22"));
        chartView.setData(chartItems, ChartView.CHART_TYPE_HEART);
    }

    /**
     * 画血糖的两条线
     * @param chartView
     */
    private void drawDoubleSpecial(ChartView chartView)
    {
        ArrayList<ChartItem> chartItems = new ArrayList<>();

        chartItems.add(new ChartItem(5, "11/11",ChartItem.TYPE_SINGLE_ONE));
        chartItems.add(new ChartItem(8, "11/12",ChartItem.TYPE_SINGLE_TWO));
        chartItems.add(new ChartItem(4, "11/13",ChartItem.TYPE_SINGLE_ONE));
        chartItems.add(new ChartItem(2, "11/14",ChartItem.TYPE_SINGLE_TWO));
        chartItems.add(new ChartItem(8, "11/15",ChartItem.TYPE_SINGLE_TWO));
        chartItems.add(new ChartItem(7, "11/16",ChartItem.TYPE_SINGLE_ONE));
        chartItems.add(new ChartItem(3, "11/17",ChartItem.TYPE_SINGLE_TWO));
        chartItems.add(new ChartItem(6, "11/18",ChartItem.TYPE_SINGLE_ONE));
        chartItems.add(new ChartItem(11, "11/19",ChartItem.TYPE_SINGLE_ONE));
        chartItems.add(new ChartItem(1, "11/20",ChartItem.TYPE_SINGLE_TWO));
        chartView.setData(chartItems, ChartView.CHART_TYPE_SUGAR);
    }

    /**
     * 画血压的两条线
     * @param chartView
     */
    private void drawDouble(ChartView chartView)
    {
        ArrayList<ChartItem> chartItems = new ArrayList<>();
        ArrayList<ChartItem> chartItems2 = new ArrayList<>();

        chartItems.add(new ChartItem(100, "11/11"));
        chartItems.add(new ChartItem(80, "11/12"));
        chartItems.add(new ChartItem(50, "11/13"));
        chartItems.add(new ChartItem(60, "11/14"));
        chartItems.add(new ChartItem(80, "11/15"));
        chartItems.add(new ChartItem(50, "11/16"));
        chartItems.add(new ChartItem(110, "11/17"));
        chartItems.add(new ChartItem(40, "11/18"));
        chartItems.add(new ChartItem(120, "11/19"));
        chartItems.add(new ChartItem(70, "11/20"));

        chartItems2.add(new ChartItem(50, "11/11"));
        chartItems2.add(new ChartItem(20, "11/12"));
        chartItems2.add(new ChartItem(10, "11/13"));
        chartItems2.add(new ChartItem(90, "11/14"));
        chartItems2.add(new ChartItem(30, "11/15"));
        chartItems2.add(new ChartItem(100, "11/16"));
        chartItems2.add(new ChartItem(50, "11/17"));
        chartItems2.add(new ChartItem(90, "11/18"));
        chartItems2.add(new ChartItem(140, "11/19"));
        chartItems2.add(new ChartItem(40, "11/20"));

        List<List<ChartItem>> chartItemListList = new ArrayList<>();
        chartItemListList.add(chartItems);
        chartItemListList.add(chartItems2);
        chartView.setMultiData(chartItemListList);
    }
}
