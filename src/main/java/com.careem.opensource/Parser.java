package com.careem.opensource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

  private static final String PAUSE_TIME_PATTERN_STRING = ", \\d+\\.\\d+ secs]";
  private static final String FLOAT_NUMBER_PATTERN_STRING = "([0-9]*[.])?[0-9]+";
  private static final Pattern FLOAT_NUMBER_PATTERN = Pattern.compile(FLOAT_NUMBER_PATTERN_STRING);

  public double analyzePauseTime(String line) {
    if (line.matches(PAUSE_TIME_PATTERN_STRING)) {
        Matcher secsMatcher = FLOAT_NUMBER_PATTERN.matcher(line);
        if (secsMatcher.find()) {
          return Double.valueOf(secsMatcher.group(0)) * 1000;
        } else {
          return 0.0;
        }
    } else {
      return 0.0;
    }
  }
}
