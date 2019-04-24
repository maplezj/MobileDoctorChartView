package com.sbys.loggerlib;

import android.util.Log;

/**
 * Created by zhaojian on 2018/1/18.
 * 描述：参照{@link PrettyFormatStrategy},更改日志输出格式(用法参照开源项目Logger：https://github.com/orhanobut/logger)
 * 此策略格式为：TAG:ThreadName{ThreadID} fileName.method(fileName.java:methodLine) logContent
 * 例：SBYS-test: main{1} App.initM(App.java:163) test
 */

public class SimpleFormatStrategy implements FormatStrategy
{
    /**
     * The minimum stack trace index, starts at this class after two native calls.
     */
    private static final int MIN_STACK_OFFSET = 5;
    private String tag;

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    @Override
    public void log(int priority, String onceOnlyTag, String message)
    {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        String thread = Thread.currentThread().getName() + "{" + Thread.currentThread().getId() + "} ";
        StringBuilder stringBuilder = new StringBuilder();
        int methodIndex = getStackOffset(trace);
        if (methodIndex < 0)
        {
            return;
        }
        stringBuilder.append(thread)
                .append(trace[methodIndex].getFileName().replace(".java", ""))
                .append(".")
                .append(trace[methodIndex].getMethodName())
                .append("(")
                .append(trace[methodIndex].getFileName())
                .append(":")
                .append(trace[methodIndex].getLineNumber())
                .append(") ");
        Log.println(priority, tag, stringBuilder.toString() + message);
    }

    private int getStackOffset(StackTraceElement[] trace) {
        for (int i = MIN_STACK_OFFSET; i < trace.length; i++) {
            StackTraceElement e = trace[i];
            String name = e.getClassName();
            if (!name.equals(LoggerPrinter.class.getName()) && !name.equals(Logger.class.getName())) {
                return i;
            }
        }
        return -1;
    }
}
