package com.careem.opensource;

import com.careem.opensource.GcData.Name;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

  // TODO: 9/12/18  names needs to be refactord
  // generic matchers
  private static final Pattern DECIMAL_NUMBER_PATTERN = Pattern.compile("([0-9]*[.])?[0-9]+");
  private static int evacuationPauseCount;
  private static int concurrentMarkCount;
  // gc data matchers
  private static final String TOTAL_PAUSE_TIME_REGEX = ", \\d+\\.\\d+ secs]";


  // gc Evacuation pause
  private static final String EVACUATION_PAUSE_REGEX = "(G1 Evacuation Pause)";
  private static final Pattern EVACUATION_PAUSE_PATTERN = Pattern.compile(TOTAL_PAUSE_TIME_REGEX);

  // concurrent time pattern
  private static final String CONCURRENT_MARK_REGEX = "[GC concurrent-mark-end,";

  public GcData parse(String chunk) {
    // TODO: 9/12/18 refator the order of execution and add a break point
    GcData.GcDataBuilder gcDataBuilder = GcData.builder();
    analyzePauseTime(gcDataBuilder,chunk);
    analyzeConcurrentMark(gcDataBuilder,chunk);
    countEvacuationPause(chunk);
    return gcDataBuilder.build();
  }

  public static int getEvacuationPauseCount() {
    return evacuationPauseCount;
  }

  public static int getConcurrentMarkCount() {
    return concurrentMarkCount;
  }

  private void countEvacuationPause(String chunk) {
    if (chunk.contains(EVACUATION_PAUSE_REGEX)) {
      evacuationPauseCount ++;
    }
  }


  private void analyzePauseTime(GcData.GcDataBuilder gcDataBuilder,String chunk){
    if (chunk.matches(TOTAL_PAUSE_TIME_REGEX)) {
      gcDataBuilder.name(Name.PAUSE_TIME);
      gcDataBuilder.tag("total");
      gcDataBuilder.value(parseDecimalNumber(chunk));
    }
  }


  public boolean shouldReadMoreLine(String line) {
    //TODO: there are records that span multiple lines. We need to parse and read until the data makes sense
    return false;
  }

  private double parseDecimalNumber(String line) {
    Matcher matcher = DECIMAL_NUMBER_PATTERN.matcher(line);
    if (matcher.find()) {
      return Double.valueOf(matcher.group(matcher.groupCount()-1));
    } else {
      return 0.0;
    }
  }
  private String extractSecs(String chunk){
    Matcher matcher = EVACUATION_PAUSE_PATTERN.matcher(chunk);
    if (matcher.find()) {
      return matcher.group(0);
    } else {
      return "1.21 secs]";
    }
  }
  private void analyzeConcurrentMark(GcData.GcDataBuilder gcDataBuilder,String chunk){
    if (chunk.contains(CONCURRENT_MARK_REGEX)) {
      concurrentMarkCount ++;
      gcDataBuilder.name(Name.CONCURRENT_MARK);
      gcDataBuilder.tag("concurrent_mark_time");
      gcDataBuilder.value(parseDecimalNumber(extractSecs(chunk)));
    }
  }

}
