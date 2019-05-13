package com.sbys.zhaojian.mobiledoctorchartview.chatview;

import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;

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
                return getDateString(getDate(dateString, "M/d HH:mm"), "M-d");
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
                return getDateString(getDate(dateString, "M/d HH:mm:ss"), "HH:mm:ss");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}
