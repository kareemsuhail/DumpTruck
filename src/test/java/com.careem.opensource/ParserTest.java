package com.careem.opensource;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.careem.opensource.GcData.Name;
import org.junit.Test;

public class ParserTest {

  private final Parser PARSER;
  private final String CHUNK_CONTAINS_EVACUATION_TIME;
  private final String CHUNK_DOES_NOT_CONTAIN_PAUSE;
  private final String CHUNK_CONTAINS_EVACUATION_PAUSE;
  private final String EMPTY_CHUNK;
  private final String CHUNK_CONTAINS_CONCURRENT_MARK;
  private final String CHUNK_CONTAINS_MIXED_GC;
  private final String CHUNK_CONTAINS_YOUNG_GC;
  private final String CHUNK_CONTAINS_PREDICTED_BASE_TIME;
  private final String CHUNK_CONTAINS_PREDICTED_PAUSE_TIME;
  private final String CHUNK_CONTAINS_MAX_PAUSE_TIME;

  public ParserTest() {
    PARSER = new Parser();
    CHUNK_CONTAINS_EVACUATION_TIME = ", 0.0262454 secs]";
    CHUNK_DOES_NOT_CONTAIN_PAUSE = "[ObjectSynchronizer Roots (ms):  0.0  0.0  0.0  0.0\n"
        + "          Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]";
    EMPTY_CHUNK = " \n ";
    CHUNK_CONTAINS_EVACUATION_PAUSE = "2018-08-23T12:23:25.710+0200: 9.636: [GC pause (G1 Evacuation Pause) (young) G1HR #StartGC 8\n";
    CHUNK_CONTAINS_CONCURRENT_MARK = "2018-08-23T12:24:04.060+0200: 47.985: [GC concurrent-mark-end, 0.7346769 secs]";
    CHUNK_CONTAINS_MIXED_GC = "2018-08-23T12:25:22.608+0200: 126.533: [GC pause (G1 Evacuation Pause) (mixed) G1HR #StartGC 253";
    CHUNK_CONTAINS_YOUNG_GC = "2018-08-23T12:25:20.941+0200: 124.867: [GC pause (G1 Evacuation Pause) (young) G1HR #StartGC 248";
    CHUNK_CONTAINS_PREDICTED_BASE_TIME = " 17.943: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 2096, predicted base time: 16.93 ms, remaining time: 33.07 ms, target pause time: 50.00 ms]";
    CHUNK_CONTAINS_PREDICTED_PAUSE_TIME = "223.951: [G1Ergonomics (CSet Construction) finish choosing CSet, eden: 94 regions, survivors: 8 regions, old: 0 regions, predicted pause time: 48.52 ms, target pause time: 50.00 ms]";
    CHUNK_CONTAINS_MAX_PAUSE_TIME = "-XX:InitiatingHeapOccupancyPercent=0 -XX:MaxGCPauseMillis=50 -XX:MaxHeapSize=4294967296 -XX:NumberOfGCLogFiles=5 -XX:+PreserveFramePointer -XX:+PrintAdaptiveSizePolicy -XX:+PrintFlagsFinal -XX:+PrintGC -XX";
  }

  @Test
  public void parseNonPausetimeTag() {
    GcData gcData = PARSER.parse(CHUNK_DOES_NOT_CONTAIN_PAUSE);
    assertThat(gcData.getName(), not(Name.PAUSE_TIME));
  }

  @Test
  public void parseMoreThanOneLine() {
    boolean shouldReadMoreLine = PARSER.shouldReadMoreLine(CHUNK_DOES_NOT_CONTAIN_PAUSE);
    assertTrue(shouldReadMoreLine);
  }

  @Test
  public void parseEmptyChunk() {
    GcData gcData = PARSER.parse(EMPTY_CHUNK);
    assertThat(gcData.getName(), is(Name.EMPTY));
  }

  @Test
  public void countEvacuationPause() {
    PARSER.parse(CHUNK_CONTAINS_EVACUATION_PAUSE);

  }

  @Test
  public void parseCouncurrentMark() {
    GcData gcData = PARSER.parse(CHUNK_CONTAINS_CONCURRENT_MARK);
    assertThat(gcData.getName(), is(Name.CONCURRENT_MARK));
    assertEquals(0.7346769, gcData.getValue(), 0.001);

  }

  @Test
  public void parseMixedGC() {
    PARSER.parse(CHUNK_CONTAINS_MIXED_GC);
    assertEquals(Parser.getCurrentGC(), Name.MIXED_GC);
  }

  @Test
  public void parseMixedGCTime() {
    GcData gcData = PARSER.parse(CHUNK_CONTAINS_EVACUATION_TIME);
    assertEquals(0.0262454, gcData.getValue(), 0.001);
    assertEquals(gcData.getName(), Name.MIXED_GC);
  }

  @Test
  public void parseYoungGC() {
    PARSER.parse(CHUNK_CONTAINS_YOUNG_GC);
    assertEquals(Parser.getCurrentGC(), Name.YOUNG_GC);
  }

  @Test
  public void parseYoungGCTime() {
    GcData gcData = PARSER.parse(CHUNK_CONTAINS_EVACUATION_TIME);
    assertEquals(0.0262454, gcData.getValue(), 0.001);
    assertEquals(gcData.getName(), Name.YOUNG_GC);
  }

  @Test
  public void parsePredictedBaseTime() {
    GcData gcData = PARSER.parse(CHUNK_CONTAINS_PREDICTED_BASE_TIME);
    assertEquals(gcData.getName(), Name.PREDICTED_BASE_TIME);
    assertEquals(16.93, gcData.getValue(), 0.01);
  }

  @Test
  public void parsePredictedPasueTime() {
    GcData gcData = PARSER.parse(CHUNK_CONTAINS_PREDICTED_PAUSE_TIME);
    assertEquals(gcData.getName(), Name.PREDICTED_PAUSE_TIME);
    assertEquals(48.52, gcData.getValue(), 0.01);
  }

  @Test
  public void parseMaxPauseTime() {
    GcData gcData = PARSER.parse(CHUNK_CONTAINS_MAX_PAUSE_TIME);
    assertEquals(gcData.getName(), Name.MAX_PAUSE_TIME);
    assertEquals(50, gcData.getValue(), 0.01);
  }
}
