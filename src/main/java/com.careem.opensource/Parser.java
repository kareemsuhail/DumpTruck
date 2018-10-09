package com.careem.opensource;

import com.careem.opensource.GcData.Name;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;

public class Parser {

  // generic matchers
  private static final Pattern DECIMAL_NUMBER_PATTERN = Pattern.compile("([0-9]*[.])?[0-9]+");
  // gc data matchers
  private static final String PAUSE_TIME_REGEX = "^, \\d+\\.\\d+ secs]";
  private static final String CONCURRENT_MARK_TIME_REGEX = ", \\d+\\.\\d+ secs]";
  private static final Pattern CONCURRENT_MARK_TIME_PATTERN = Pattern
      .compile(CONCURRENT_MARK_TIME_REGEX);
  private static final String STRING_DEDUPLICATION_REGEX = "[GC concurrent-string-deduplication, deleted";
  // gc Evacuation pause
  private static final String EVACUATION_PAUSE_REGEX = "(G1 Evacuation Pause)";
  private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat(
      "yyyy-MM-dd HH:mm:ss.SSS");
  private static final Pattern PAUSE_TIME_PATTERN = Pattern.compile(PAUSE_TIME_REGEX);
  private static final String MIXED_GC_REGEX = "(mixed) G1HR #StartGC";
  private static final String YOUNG_GC_REGEX = "(young) G1HR #StartGC";
  private static final String META_DATA_THRESHOLD_REGEX = "(Metadata GC Threshold)";
  private static final String PREDICTED_BASE_TIME_REGEX = "predicted base time: \\d+\\.\\d+";
  private static final String PREDICTED_PAUSE_TIME_REGEX = "predicted pause time: \\d+\\.\\d+";
  private static final String MAX_PAUSE_TIME_REGEX = "-XX:MaxGCPauseMillis=\\d+";
  private static final String GC_DATE_REGEX = "^[^T]+";
  private static final String GC_TIME_REGEX = "(?<=T)[^\\+]+";
  private static final Pattern GC_DATE_PATTERN = Pattern.compile(GC_DATE_REGEX);
  private static final Pattern GC_TIME_PATTERN = Pattern.compile(GC_TIME_REGEX);
  private static final Pattern MAX_PAUSE_TIME_PATTERN = Pattern.compile(MAX_PAUSE_TIME_REGEX);
  private static final Pattern PREDICTED_BASE_TIME_PATTERN = Pattern
      .compile(PREDICTED_BASE_TIME_REGEX);
  private static final Pattern PREDICTED_PAUSE_TIME_PATTERN = Pattern
      .compile(PREDICTED_PAUSE_TIME_REGEX);
  @Getter
  private static Name currentGC = Name.EMPTY;
  @Getter
  private static long currentTime = 0l;
  // concurrent time pattern
  private static final String CONCURRENT_MARK_REGEX = "[GC concurrent-mark-end,";

  public GcData parse(String chunk) {
    GcData.GcDataBuilder gcDataBuilder = GcData.builder();
    if (analyzeMixedAndYoungGCs(chunk)) {
      return gcDataBuilder.build();
    } else if (analyzePauseTime(gcDataBuilder, chunk)) {
      return gcDataBuilder.build();
    } else if (analyzeStringDeduplication(gcDataBuilder, chunk)) {
      return gcDataBuilder.build();
    } else if (analyzePredictedBaseTime(gcDataBuilder, chunk)) {
      return gcDataBuilder.build();
    } else if (analyzePredictedPauseTime(gcDataBuilder, chunk)) {
      return gcDataBuilder.build();
    } else if (analyzeConcurrentMark(gcDataBuilder, chunk)) {
      return gcDataBuilder.build();
      // make sure to keep this case the last case
    } else if (analyzeMaxPauseTime(gcDataBuilder, chunk)) {
      return gcDataBuilder.build();
    }
    return gcDataBuilder.build();
  }

  private boolean analyzePauseTime(GcData.GcDataBuilder gcDataBuilder, String chunk) {
    if (chunk.matches(PAUSE_TIME_REGEX)) {
      gcDataBuilder.name(Parser.currentGC);
      gcDataBuilder.value(parseDecimalNumber(chunk));
      gcDataBuilder.timestamp(Parser.currentTime);
      return true;
    } else {
      return false;
    }
  }

  private double parseDecimalNumber(String line) {
    Matcher matcher = DECIMAL_NUMBER_PATTERN.matcher(line);
    if (matcher.find()) {
      return Double.valueOf(matcher.group(matcher.groupCount() - 1));
    } else {
      return 0.0;
    }
  }

  private String extractConcurrentSecs(String chunk) {
    Matcher matcher = CONCURRENT_MARK_TIME_PATTERN.matcher(chunk);
    matcher.find();
    return matcher.group(0);
  }

  private boolean analyzeConcurrentMark(GcData.GcDataBuilder gcDataBuilder, String chunk) {
    if (chunk.contains(CONCURRENT_MARK_REGEX)) {
      gcDataBuilder.name(Name.CONCURRENT_MARK);
      gcDataBuilder.value(parseDecimalNumber(extractConcurrentSecs(chunk)));
      return true;
    } else {
      return false;
    }
  }

  private boolean analyzeStringDeduplication(GcData.GcDataBuilder gcDataBuilder, String chunk) {
    if (chunk.contains(STRING_DEDUPLICATION_REGEX)) {
      gcDataBuilder.name(Name.STRING_DEDUPLICATION);
      gcDataBuilder.value(parseDecimalNumber(extractConcurrentSecs(chunk)));
      return true;
    } else {
      return false;
    }
  }

  private void analyzeGCDateAndTime(String chunk) {
    Matcher timeMatcher = GC_TIME_PATTERN.matcher(chunk);
    Matcher dateMatcher = GC_DATE_PATTERN.matcher(chunk);
    timeMatcher.find();
    dateMatcher.find();
    String dateTime = dateMatcher.group(0) + " " + timeMatcher.group(0);
    try {
      Date GCDateTime = DATE_TIME_FORMAT.parse(dateTime);
      Parser.currentTime = GCDateTime.getTime();
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  private boolean analyzeMixedAndYoungGCs(String chunk) {
    if (chunk.contains(YOUNG_GC_REGEX)) {
      Parser.currentGC = Name.YOUNG_GC;
      analyzeGCDateAndTime(chunk);
      return true;
    } else if (chunk.contains(MIXED_GC_REGEX)) {
      Parser.currentGC = Name.MIXED_GC;
      analyzeGCDateAndTime(chunk);
      return true;
    }
    return false;
  }

  private boolean analyzePredictedBaseTime(GcData.GcDataBuilder gcDataBuilder, String chunk) {
    Matcher predictedBaseTimeMatcher = PREDICTED_BASE_TIME_PATTERN.matcher(chunk);
    if (predictedBaseTimeMatcher.find()) {
      double secs = parseDecimalNumber(predictedBaseTimeMatcher.group(0));
      gcDataBuilder.name(Name.PREDICTED_BASE_TIME);
      gcDataBuilder.value(secs);
      gcDataBuilder.timestamp(Parser.currentTime);
      return true;
    } else {
      return false;
    }
  }

  private boolean analyzePredictedPauseTime(GcData.GcDataBuilder gcDataBuilder, String chunk) {
    Matcher predictedBaseTimeMatcher = PREDICTED_PAUSE_TIME_PATTERN.matcher(chunk);
    if (predictedBaseTimeMatcher.find()) {
      double value = parseDecimalNumber(predictedBaseTimeMatcher.group(0));
      gcDataBuilder.name(Name.PREDICTED_PAUSE_TIME);
      gcDataBuilder.value(value);
      gcDataBuilder.timestamp(Parser.currentTime);
      return true;
    } else {
      return false;
    }
  }

  private boolean analyzeMaxPauseTime(GcData.GcDataBuilder gcDataBuilder, String chunk) {
    Matcher maxPauseTimeMatcher = MAX_PAUSE_TIME_PATTERN.matcher(chunk);
    if (maxPauseTimeMatcher.find()) {
      double value = parseDecimalNumber(maxPauseTimeMatcher.group(0));
      gcDataBuilder.name(Name.MAX_PAUSE_TIME);
      gcDataBuilder.value(value);
      return true;
    } else {
      return false;
    }
  }
}
