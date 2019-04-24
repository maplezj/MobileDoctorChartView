package com.sbys.zhaojian.mobiledoctorchartview.chatview;

import android.content.Context;

import java.util.List;

/**
 * @author zhaojian
 * @time 2018/6/27 16:57
 * @describe
 */
public class CommonUtil
{
    public static int dp2px(Context context, float dpValue)
    {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(Context context, float pxValue)
    {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue)
    {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static boolean isListEmpty(List list) {
        return list == null || list.size() == 0;
    }
}
