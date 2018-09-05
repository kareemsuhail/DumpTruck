package com.careem.opensource;

import com.careem.opensource.GcData.Name;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

  // generic matchers
  private static final Pattern DECIMAL_NUMBER_PATTERN = Pattern.compile("([0-9]*[.])?[0-9]+");

  // gc data matchers
  private static final String TOTAL_PAUSE_TIME_REGEX = ", \\d+\\.\\d+ secs]";

  public GcData parse(String chunk) {
    GcData.GcDataBuilder gcDataBuilder = GcData.builder();
    if (chunk.matches(TOTAL_PAUSE_TIME_REGEX)) {
      gcDataBuilder.name(Name.PAUSE_TIME);
      gcDataBuilder.tag("total");
      gcDataBuilder.value(parseDecimalNumber(chunk));
    }
    return gcDataBuilder.build();
  }

  public boolean shouldReadMoreLine(String line) {
    //TODO: there are records that span multiple lines. We need to parse and read until the data makes sense
    return false;
  }

  private double parseDecimalNumber(String line) {
    Matcher matcher = DECIMAL_NUMBER_PATTERN.matcher(line);
    if (matcher.find()) {
      return Double.valueOf(matcher.group(0));
    } else {
      return 0.0;
    }
  }
}
