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
  private final String logFileDirectory;
  private final String logFileName;
  private final Parser parser;

  public Reporter(
      MeterRegistry meterRegistry, String logFileDirectory, String logFileName, Parser parser
  ) {
    this.meterRegistry = meterRegistry;
    this.logFileDirectory = logFileDirectory;
    this.logFileName = logFileName;
    this.parser = parser;
  }

  @Override
  public void run() {
    log.info("Running Garbage Collector Reporter");
    Path logFilePath = FileSystems.getDefault().getPath(logFileDirectory + "/" + logFileName);
    try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
      Path logFileDirectoryPath = FileSystems.getDefault().getPath(logFileDirectory);
      logFileDirectoryPath.register(
          watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW
      );

      try (BufferedReader bufferedReader = Files.newBufferedReader(logFilePath)) {
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

                // read lines until the chunk data makes sense
                StringBuilder chunkBuilder = new StringBuilder();
                String line;
                do {
                  line = bufferedReader.readLine();
                  log.debug("{}", line);
                  chunkBuilder.append(line);
                } while (parser.shouldReadMoreLine(line));
                String chunk = chunkBuilder.toString();

                // parse and report
                GcData gcData = parser.parse(chunk);
                log.debug("{}", gcData);
                switch (gcData.getName()) {
                  case PAUSE_TIME:
                    Timer.builder(gcData.getName().name())
                        .tags("cause", gcData.getTag())
                        .register(meterRegistry)
                        .record(new Double(gcData.getValue() * 1000).longValue(), TimeUnit.MILLISECONDS);
                    break;
                  default:
                    break;
                }
              }
            }
          }

          if (!watchKey.reset()) {
            log.error("Error occurred while resetting watchKey");
          }
        }
      } catch (IOException ex) {

        ex.printStackTrace();
      }
    } catch (IOException | InterruptedException ex) {
      ex.printStackTrace();
    }
  }

  public void start() {
    new Thread(this).start();
  }
}
