package com.careem.opensource;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

  // generic matchers
  private static final Pattern DECIMAL_NUMBER_PATTERN = Pattern.compile("([0-9]*[.])?[0-9]+");

  // gc data matchers
  private static final String TOTAL_PAUSE_TIME_REGEX = ", \\d+\\.\\d+ secs]";

  public GcData parse(BufferedReader bufferedReader) throws IOException {
    String line = bufferedReader.readLine();
    GcData.GcDataBuilder gcDataBuilder = GcData.builder();
    if (line.matches(TOTAL_PAUSE_TIME_REGEX)) {
      gcDataBuilder.name("pause_time");
      gcDataBuilder.tag("total");
      gcDataBuilder.value(parseDecimalNumber(line));
    }
    return gcDataBuilder.build();
  }

  private double parseDecimalNumber(String line) {
    Matcher matcher = DECIMAL_NUMBER_PATTERN.matcher(line);
    if (matcher.find()) {
      return convertSecondToMillisecond(Double.valueOf(matcher.group(0)));
    } else {
      return 0.0;
    }
  }

  private double convertSecondToMillisecond(double second) {
    return second * 1000;
  }
}
