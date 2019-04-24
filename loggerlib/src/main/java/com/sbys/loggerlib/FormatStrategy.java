package com.sbys.loggerlib;

public interface FormatStrategy {

  void log(int priority, String tag, String message);
}
