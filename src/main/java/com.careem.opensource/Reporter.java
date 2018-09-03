package com.careem.opensource;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Reporter implements Runnable {

  private final MeterRegistry meterRegistry;
  private final String logFileName;
  private final Path logFileDirectoryPath;
  private final Path logFilePath;
  private final Parser parser;

  private final Timer timer;

  public Reporter(
      MeterRegistry meterRegistry, String logFileName, String logFileDirectory,
      Parser parser
  ) {
    this.meterRegistry = meterRegistry;
    this.logFileName = logFileName;
    this.logFileDirectoryPath = FileSystems.getDefault().getPath(logFileDirectory);
    this.logFilePath = FileSystems.getDefault().getPath(logFileDirectory + "/" + logFileName);
    this.parser = parser;

    timer = Timer.builder("time_total")
        .tags("metrics", "pauseTime")
        .register(meterRegistry);
  }

  @Override
  public void run() {
    log.info("Running Garbage Collector Reader");
    try (BufferedReader bufferedReader = Files.newBufferedReader(logFilePath)) {
      try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
        logFileDirectoryPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        while (true) {
          WatchKey watchKey = watchService.take();
          for (WatchEvent<?> event : watchKey.pollEvents()) {
            Path changed = (Path) event.context();
            if (changed.endsWith(logFileName)) {
              String line = bufferedReader.readLine();
              log.info(line);
              double pauseTime = parser.analyzePauseTime(line);
              timer.record(new Double(pauseTime).longValue(), TimeUnit.MILLISECONDS);
            }
          }

          if (!watchKey.reset()) {
            log.error("Error occurred while resetting watchKey");
          }
        }
      } catch (IOException | InterruptedException ex) {
        ex.printStackTrace();
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public void start() {
    new Thread(this).start();
  }
}
