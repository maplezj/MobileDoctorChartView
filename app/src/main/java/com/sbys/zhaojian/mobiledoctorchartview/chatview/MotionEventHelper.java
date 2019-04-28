package com.sbys.zhaojian.mobiledoctorchartview.chatview;

/**
 * @author zhaojian
 * @time 2019/4/28 9:22
 * @describe
 */
public class MotionEventHelper
{
    public static float downX;
    public static float preX;
    public static float downY;
    public static boolean scrolling;

    public static boolean isclick(float upX, float upY)
    {
        return Math.abs(upX - downX) < 8 && Math.abs(upY - downY) < 8;
    }
}
