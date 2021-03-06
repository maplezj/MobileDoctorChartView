package com.sbys.zhaojian.mobiledoctorchartview;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.github.MDChartView.chatview.ChartItem;
import com.github.MDChartView.chatview.ChartView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity
{
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ChartView chartView = findViewById(R.id.chatView);
        //chartView.setEmpty(ChartView.CHART_TYPE_HEART);
        chartView.setConfig(new ChartView.ChartConfigBuilder()
                .setUnitYFormat(ChartItem.UNIT_Y_FORMAT_INT)
                .setCountX(10)
                .setCountY(4)
                //.setPerCountX(11)
                .setUnitY("(cm)")
                .setUnitX("(周)")
                .drawNormalLinePoint(false)
                .setUnitXType(ChartView.UnitType.TYPE_NUM)
                .setMoveType(ChartView.MoveType.TYPE_VERTIAL_LINE)
                .addLine(drawSingle(), ChartItem.LINE_SOURCE)
                .addLine(line1(), ChartItem.LINE_1,Color.parseColor("#56BD72"))
                //.addLine(line2(), ChartItem.LINE_2)
                //.addLine(line3(), ChartItem.LINE_3)
                //.setColor(ChartItem.LINE_1, getResources().getColor(R.color.colorAccent))
                .setColor(ChartItem.LINE_2, Color.rgb(86, 189, 114))
                .addMultTypeLine(drawDoubleSpecial())
                .showVertialLine(true)
                .showFullScreen(false)
                //.setStandLineValue(new ChartView.ValueEntity(150, 50, 139, 89))
                .showStandLine(true)
                .setDetailDrawable(new HypertensionDrawable(this))
                .addStandLine(66f, Color.parseColor("#FF4081"))
                .addStandLine(11f, Color.parseColor("#FF4081"))
                .setMaxValue(99)
                //.setHighStandLineColor(R.color.colorAccent)
                //.setLowStandLineColor(R.color.colorPrimary)
                .standardLineCanPoint(true)
                .drawNet(true)
                .build());

        chartView.setOnClickPointListener(new ChartView.OnClickPointListener()
        {
            @Override
            public void onClick(List<ChartItem> chartItem)
            {
                Log.d(TAG, "onClick:----------------> " + chartItem.get(0).getValueY());
            }
        });

        //drawDouble();
        //drawDoubleSpecial(chartView);
        //drawSingle(chartView);
    }

    /**
     * 画单条曲线
     */
    private List<ChartItem> drawSingle()
    {
        ArrayList<ChartItem> chartItems = new ArrayList<>();

        chartItems.add(new ChartItem(40, "1"));
        chartItems.add(new ChartItem(80, "2"));
        chartItems.add(new ChartItem(50, "3"));
        //chartItems.add(new ChartItem(60, "4"));
        //chartItems.add(new EmptyChartItem("4"));
        //chartItems.add(new EmptyChartItem("5"));
        //chartItems.add(new ChartItem(10, "6"));
        //chartItems.add(new ChartItem(50, "7"));
        //chartItems.add(new ChartItem(110, "7"));
        //chartItems.add(new ChartItem(40, "8"));
        //chartItems.add(new ChartItem(120, "9"));
        //chartItems.add(new ChartItem(70, "10"));

        //chartView.setData(chartItems, ChartView.CHART_TYPE_HEART);
        return chartItems;
    }

    private List<ChartItem> line1()
    {
        ArrayList<ChartItem> chartItems = new ArrayList<>();

        //chartItems.add(new ChartItem(30, "1"));
        chartItems.add(new ChartItem(30,"1"));
        chartItems.add(new ChartItem(35, "2"));
        chartItems.add(new ChartItem(40, "3"));
        chartItems.add(new ChartItem(45, "4"));
        chartItems.add(new ChartItem(50, "5"));
        chartItems.add(new ChartItem(55, "6"));
        chartItems.add(new ChartItem(60, "7"));
        chartItems.add(new ChartItem(65, "8"));
        chartItems.add(new ChartItem(70, "9"));
        chartItems.add(new ChartItem(90, "10"));
        //chartItems.add(new ChartItem(80, "10"));
        //chartItems.add(new EmptyChartItem("11"));

        //chartView.setData(chartItems, ChartView.CHART_TYPE_HEART);
        return chartItems;
    }

    private List<ChartItem> line2()
    {
        ArrayList<ChartItem> chartItems = new ArrayList<>();

        chartItems.add(new ChartItem(40,"1"));
        chartItems.add(new ChartItem(45, "2"));
        chartItems.add(new ChartItem(50, "3"));
        chartItems.add(new ChartItem(55, "4"));
        chartItems.add(new ChartItem(60, "5"));
        chartItems.add(new ChartItem(65, "6"));
        chartItems.add(new ChartItem(70, "7"));
        chartItems.add(new ChartItem(75, "8"));
        chartItems.add(new ChartItem(80, "9"));
        chartItems.add(new ChartItem(100, "10"));
        return chartItems;
    }

    private List<ChartItem> line3()
    {
        ArrayList<ChartItem> chartItems = new ArrayList<>();

        chartItems.add(new ChartItem(50,"1"));
        chartItems.add(new ChartItem(55, "2"));
        chartItems.add(new ChartItem(60, "3"));
        chartItems.add(new ChartItem(65, "4"));
        chartItems.add(new ChartItem(70, "5"));
        chartItems.add(new ChartItem(75, "6"));
        chartItems.add(new ChartItem(80, "7"));
        chartItems.add(new ChartItem(85, "8"));
        chartItems.add(new ChartItem(90, "9"));
        chartItems.add(new ChartItem(110, "10"));
        return chartItems;
    }

    /**
     * 画血糖的两条线
     */
    private List<ChartItem> drawDoubleSpecial()
    {
        ArrayList<ChartItem> chartItems = new ArrayList<>();

        chartItems.add(new ChartItem(5, "1",ChartItem.LINE_2));
        chartItems.add(new ChartItem(8, "2",ChartItem.LINE_3));
        chartItems.add(new ChartItem(4, "3",ChartItem.LINE_2));
        chartItems.add(new ChartItem(2, "4",ChartItem.LINE_3));
        chartItems.add(new ChartItem(8, "5",ChartItem.LINE_3));
        chartItems.add(new ChartItem(7, "6",ChartItem.LINE_2));
        chartItems.add(new ChartItem(11, "7",ChartItem.LINE_4));
        chartItems.add(new ChartItem(3, "8",ChartItem.LINE_3));
        chartItems.add(new ChartItem(6, "9",ChartItem.LINE_2));
        chartItems.add(new ChartItem(11, "10",ChartItem.LINE_2));
        chartItems.add(new ChartItem(1, "11",ChartItem.LINE_3));
        chartItems.add(new ChartItem(9, "12",ChartItem.LINE_4));
        //chartView.add(chartItems, ChartView.CHART_TYPE_SUGAR);
        return chartItems;
    }

    /**
     * 画血压的两条线
     */
    private void drawDouble()
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
        //chartView.setMultiData(chartItemListList);
    }
}
