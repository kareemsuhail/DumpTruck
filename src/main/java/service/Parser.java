package service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

  private static final String PAUSE_TIME_PATTERN_STRING = ", \\d+\\.\\d+ secs]";
  private static final String FLOAT_NUMBER_PATTERN_STRING = "([0-9]*[.])?[0-9]+";
  private static final Pattern FLOAT_NUMBER_PATTERN = Pattern.compile(FLOAT_NUMBER_PATTERN_STRING);

  private final Timer timer;

  public Parser(MeterRegistry meterRegistry) {
    timer = Timer.builder("time_total")
        .tags("metrics", "pauseTime")
        .register(meterRegistry);
  }

  public void analyzePauseTime(String line) {
    if (line.matches(PAUSE_TIME_PATTERN_STRING)) {
      try {
        Matcher secsMatcher = FLOAT_NUMBER_PATTERN.matcher(line);
        if (secsMatcher.find()) {
          long pauseTime = new Double(Double.valueOf(secsMatcher.group(0)) * 1000).longValue();
          timer.record(pauseTime, TimeUnit.MILLISECONDS);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
