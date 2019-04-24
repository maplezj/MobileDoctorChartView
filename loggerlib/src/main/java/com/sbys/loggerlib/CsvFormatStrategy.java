package com.sbys.loggerlib;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * CSV formatted file logging for Android.
 * Writes to CSV the following data:
 * epoch timestamp, ISO8601 timestamp (human-readable), log level, tag, log message.
 * 描述：本地存储策略类，本地存储逻辑需优化
 */
public class CsvFormatStrategy implements FormatStrategy {

  private static final String NEW_LINE = System.getProperty("line.separator");
  private static final String NEW_LINE_REPLACEMENT = " <br> ";
  private static final String SEPARATOR = ",";
  private static final int MIN_STACK_OFFSET = 5;


  private final Date date;
  private final SimpleDateFormat dateFormat;
  private final LogStrategy logStrategy;
  private final String tag;

  private CsvFormatStrategy(Builder builder) {
    date = builder.date;
    dateFormat = builder.dateFormat;
    logStrategy = builder.logStrategy;
    tag = builder.tag;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Override public void log(int priority, String onceOnlyTag, String message) {
    String tag = formatTag(onceOnlyTag);

    date.setTime(System.currentTimeMillis());

    StringBuilder builder = new StringBuilder();

    // machine-readable date/time
    //builder.append(Long.toString(date.getTime()));

    // human-readable date/time
    //builder.append(SEPARATOR);
    builder.append(dateFormat.format(date));

    // level
    //builder.append(SEPARATOR);
    builder.append("[");
    builder.append(tag);
    builder.append("] ");
    builder.append("[");
    builder.append(Utils.logLevel(priority));
    builder.append("]");

    StackTraceElement[] trace = Thread.currentThread().getStackTrace();
    String thread = Thread.currentThread().getName() + "{" + Thread.currentThread().getId() + "} ";
    int methodIndex = getStackOffset(trace);
    if (methodIndex < 0)
    {
      return;
    }
    builder.append(thread)
            .append(trace[methodIndex].getFileName().replace(".java", ""))
            .append(".")
            .append(trace[methodIndex].getMethodName())
            .append("(")
            .append(trace[methodIndex].getFileName())
            .append(":")
            .append(trace[methodIndex].getLineNumber())
            .append(") ");

    // tag
    //builder.append(SEPARATOR);
    //builder.append(tag);

    // message
    if (message.contains(NEW_LINE)) {
      // a new line would break the CSV format, so we replace it here
      message = message.replaceAll(NEW_LINE, NEW_LINE_REPLACEMENT);
    }
    //builder.append(SEPARATOR);
    builder.append(message);

    // new line
    builder.append(NEW_LINE);

    logStrategy.log(priority, tag, builder.toString());
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

  private String formatTag(String tag) {
    if (!Utils.isEmpty(tag) && !Utils.equals(this.tag, tag)) {
      return this.tag + "-" + tag;
    }
    return this.tag;
  }

  public static final class Builder {
    private static final int MAX_BYTES = 5000 * 1024; // 5000K averages to a 40000 lines per file

    Date date;
    SimpleDateFormat dateFormat;
    LogStrategy logStrategy;
    String tag = "PRETTY_LOGGER";
    String path;

    private Builder() {
    }

    public Builder date(Date val) {
      date = val;
      return this;
    }

    public Builder dateFormat(SimpleDateFormat val) {
      dateFormat = val;
      return this;
    }

    public Builder logStrategy(LogStrategy val) {
      logStrategy = val;
      return this;
    }

    public Builder tag(String tag) {
      this.tag = tag;
      return this;
    }

    public Builder path(String path)
    {
      this.path = path;
      return this;
    }

    public CsvFormatStrategy build() {
      if (date == null) {
        date = new Date();
      }
      if (dateFormat == null) {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS", Locale.UK);
      }
      if (logStrategy == null) {
        String diskPath = TextUtils.isEmpty(path) ? Environment.getExternalStorageDirectory().getAbsolutePath() : path;
        String folder = diskPath + File.separatorChar + "logger";

        HandlerThread ht = new HandlerThread("SBYSFileLogger." + folder);
        ht.start();
        Handler handler = new DiskLogStrategy.WriteHandler(ht.getLooper(), folder, MAX_BYTES);
        logStrategy = new DiskLogStrategy(handler);
      }
      return new CsvFormatStrategy(this);
    }
  }
}
