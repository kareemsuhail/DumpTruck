package com.careem.opensource;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Reporter implements Runnable {

  private final ScheduledExecutorService scheduler;
  private final MeterRegistry meterRegistry;
  private final String logFileDirectory;
  private final String logFileName;
  private final Parser parser;
  private int lastKnownLine;

  public Reporter(
      MeterRegistry meterRegistry, String logFileDirectory, String logFileName, Parser parser
  ) {
    this.meterRegistry = meterRegistry;
    this.logFileDirectory = logFileDirectory;
    this.logFileName = logFileName;
    this.parser = parser;
    scheduler = Executors.newScheduledThreadPool(1);
  }

  @Override
  public void run() {
    log.info("Running Garbage Collector Reporter");
    Path logFilePath = FileSystems.getDefault().getPath(logFileDirectory + "/" + logFileName);
    try (Stream<String> linesStream = Files.lines(logFilePath)) {
      Stream<String> numberOfLinesStream = Files.lines(logFilePath);
      long count = numberOfLinesStream.count();
      numberOfLinesStream.close();
      if (count == lastKnownLine) {
        log.info("end of file");
        return;
      }
      if (count < lastKnownLine) {
        log.info("re-read file");
        lastKnownLine = 0;
      }
      linesStream.skip(lastKnownLine).forEach(line -> {
        lastKnownLine += 1;
        GcData gcData = parser.parse(line);
        log.debug("{}", line);
        log.debug("{}", gcData);
        switch (gcData.getName()) {
          case YOUNG_GC:
            Timer.builder(gcData.getName().name())
                .tags("cause", gcData.getTag())
                .register(meterRegistry)
                .record(new Double(gcData.getValue() * 1000).longValue(),
                    TimeUnit.MILLISECONDS);
            break;
          case MIXED_GC:
            Timer.builder(gcData.getName().name())
                .tags("cause", gcData.getTag())
                .register(meterRegistry)
                .record(new Double(gcData.getValue() * 1000).longValue(),
                    TimeUnit.MILLISECONDS);
            break;
          case PREDICTED_BASE_TIME:
            Timer.builder(gcData.getName().name())
                .tags("cause", gcData.getTag())
                .register(meterRegistry)
                .record(new Double(gcData.getValue()).longValue(),
                    TimeUnit.MILLISECONDS);
            break;
          case PREDICTED_PAUSE_TIME:
            Timer.builder(gcData.getName().name())
                .tags("cause", gcData.getTag())
                .register(meterRegistry)
                .record(new Double(gcData.getValue()).longValue(),
                    TimeUnit.MILLISECONDS);
            break;
          case CONCURRENT_MARK:
            Timer.builder(gcData.getName().name())
                .tags("cause", gcData.getTag())
                .register(meterRegistry)
                .record(new Double(gcData.getValue() * 1000).longValue(),
                    TimeUnit.MILLISECONDS);
            break;
          default:
            break;
        }
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void start() {
    scheduler.scheduleAtFixedRate(this, 5000, 5000, TimeUnit.MILLISECONDS);
    new Thread(this).start();
  }
}
