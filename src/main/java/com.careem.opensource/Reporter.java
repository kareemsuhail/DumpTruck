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

  private static final String BASE_LOG_PATH = "/tmp";

  private final MeterRegistry meterRegistry;
  private final String logFileName;
  private final Path logFileDirectoryPath;
  private final Path logFilePath;
  private final Parser parser;

  public Reporter(MeterRegistry meterRegistry, String logFileName, Parser parser) {
    this.meterRegistry = meterRegistry;
    this.logFileName = logFileName;
    this.logFileDirectoryPath = FileSystems.getDefault().getPath(BASE_LOG_PATH);
    this.logFilePath = FileSystems.getDefault().getPath(BASE_LOG_PATH + "/" + logFileName);
    this.parser = parser;
  }

  @Override
  public void run() {
    log.debug("Running Garbage Collector Reporter");
    try (BufferedReader bufferedReader = Files.newBufferedReader(logFilePath)) {
      try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
        logFileDirectoryPath.register(
            watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW
        );
        WatchKey watchKey;
        while ((watchKey = watchService.take()) != null) {
          for (WatchEvent<?> event : watchKey.pollEvents()) {
            Path changed = (Path) event.context();
            log.debug(
                "[{}]: File content has been affected by event {}",
                changed.toString(), event.kind().name()
            );
            if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
              if (changed.endsWith(logFileName)) {
                GcData gcData = parser.parse(bufferedReader);

                //TODO: make this ENUM switch
                if (gcData.getName().equals("pause_time")) {
                  Timer.builder(gcData.getName())
                      .tags("cause", gcData.getTag())
                      .register(meterRegistry)
                      .record(new Double(gcData.getValue()).longValue(), TimeUnit.MILLISECONDS);
                }
              }
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
