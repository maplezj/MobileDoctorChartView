package com.sbys.zhaojian.mobiledoctorchartview.chatview;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author zhaojian
 * @time 2019/5/13 14:16
 * @describe
 */
public class ChartUtils
{
    public static float calculateFontHeight(Paint paint)
    {
        Paint.FontMetrics fm = paint.getFontMetrics();

        return  (float)Math.ceil(fm.descent - fm.ascent);
    }

    public static float calculateFontWidth(Paint paint, String font)
    {
        Rect bounds = new Rect();
        paint.getTextBounds(font, 0, font.length(), bounds);
        return bounds.width();
    }


    public static Date getDate(String dateString, String format) {
        if (!TextUtils.isEmpty(dateString)) {
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
                return simpleDateFormat.parse(dateString);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return new Date();
    }

    public static String getDateString(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }

    public static String getDateDay(String dateString)
    {
        if (!TextUtils.isEmpty(dateString)) {
            try {
                return getDateString(getDate(dateString, "yyyy-MM-dd HH:mm"), "MM-dd");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static String getDateHour(String dateString)
    {
        if (!TextUtils.isEmpty(dateString)) {
            try {
                return getDateString(getDate(dateString, "yyyy-MM-dd HH:mm:ss"), "HH:mm:ss");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static float dpToPx(Context context, float value) {
        return applyDimension(context, TypedValue.COMPLEX_UNIT_DIP, value);
    }

    /**
     * @param context
     * @param unit TypedValue 如TypedValue.COMPLEX_UNIT_DIP
     * @param value
     * @return
     */
    public static float applyDimension(Context context, int unit, float value) {
        return applyDimension(unit, value, context.getResources().getDisplayMetrics());
    }

    private static float applyDimension(int unit, float value, DisplayMetrics metrics){
        switch (unit) {
            case TypedValue.COMPLEX_UNIT_PX:
                return value;
            case TypedValue.COMPLEX_UNIT_DIP:
                return value * metrics.density;
            case TypedValue.COMPLEX_UNIT_SP:
                return value * metrics.scaledDensity;
            case TypedValue.COMPLEX_UNIT_PT:
                return value * metrics.xdpi * (1.0f/72);
            case TypedValue.COMPLEX_UNIT_IN:
                return value * metrics.xdpi;
            case TypedValue.COMPLEX_UNIT_MM:
                return value * metrics.xdpi * (1.0f/25.4f);
        }
        return 0;
    }
}
