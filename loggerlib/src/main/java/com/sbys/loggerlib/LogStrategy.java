package com.sbys.loggerlib;

public interface LogStrategy {

  void log(int priority, String tag, String message);
}
