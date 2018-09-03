package repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import service.Parser;

public class GcLogReader implements Runnable {
  private final String logFileName;
  private final Path logFileDirectoryPath;
  private final Path logFilePath;
  private final int delay;
  private final Parser parser;

  public GcLogReader(String logFileName, String logFileDirectory, int delay, Parser parser) {
    this.logFileName = logFileName;
    this.logFileDirectoryPath = FileSystems.getDefault().getPath(logFileDirectory);
    this.logFilePath = FileSystems.getDefault().getPath(logFileDirectory + "/" + logFileName);
    this.delay = delay;
    this.parser = parser;
  }

  @Override
  public void run() {
    System.out.println("Running File Reader");
    try (BufferedReader bufferedReader = Files.newBufferedReader(logFilePath)) {
      try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
        logFileDirectoryPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        while (true) {
          WatchKey watchKey = watchService.take();
          for (WatchEvent<?> event : watchKey.pollEvents()) {
            Path changed = (Path) event.context();
            if (changed.endsWith(logFileName)) {
              String line = bufferedReader.readLine();
              System.out.println(line);
              parser.analyzePauseTime(line);
            }
          }

          if (!watchKey.reset()) {
            System.out.println("error occured");
          }

          Thread.sleep(delay);
        }
      } catch (IOException | InterruptedException ex) {
        ex.printStackTrace();
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
