package com.careem.opensource;

import com.careem.opensource.GcData.Name;
import com.google.common.collect.EvictingQueue;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Reporter implements Runnable {

  private final ScheduledExecutorService scheduler;
  private final String logFileDirectory;
  private final String logFileName;
  private final Parser parser;
  private int lastKnownLine;
  private Cache cache;

  /**
   * @param logFileDirectory the path of your GC log file directory
   * @param logFileName the name of GC log file
   * @param parser Parser instance
   */
  public Reporter(
      String logFileDirectory, String logFileName, Parser parser,int cacheSize
  ) {

    this.logFileDirectory = logFileDirectory;
    this.logFileName = logFileName;
    this.parser = parser;
    scheduler = Executors.newScheduledThreadPool(1);
    this.cache = new Cache(cacheSize);
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
        if (gcData.getName() != Name.EMPTY) {
          cache.addItem(gcData);
        }
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * @return EvictingQueue contains cache data
   */
  public EvictingQueue getDate() {
    return this.cache.getData();
  }

  /**
   * @param endpoint string represents Data endpoint URL
   * @return HTML page
   */
  public String getDashboard(String endpoint) {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream is = classloader.getResourceAsStream("templates/gc_dashboard.html");
    Scanner s = new Scanner(is).useDelimiter("\\A");
    String result = s.hasNext() ? s.next() : "";
    return result.replace("endpoint", endpoint);
  }

  /**
   * starts a parsing engine
   */
  public void start() {
    scheduler.scheduleAtFixedRate(this, 60000, 60000, TimeUnit.MILLISECONDS);
    new Thread(this).start();
  }
}
