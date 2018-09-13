package com.careem.opensource;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.careem.opensource.GcData.Name;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParserTest {

  private final Parser parser;
  private final String chunkContainsEvacuationTime;
  private final String chunkDoesNotContainPause;
  private final String chunkContainEvacuationPause;
  private final String emptyChunk;
  private final String chunkContainsCouncurrentMark;
  private final String chunkContainsMixedGC;
  private final String chunkContainsYoungGC;

  public ParserTest() {
    parser = new Parser();
    chunkContainsEvacuationTime = ", 0.0262454 secs]";
    chunkDoesNotContainPause = "[ObjectSynchronizer Roots (ms):  0.0  0.0  0.0  0.0\n"
        + "          Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]";
    emptyChunk = " \n ";
    chunkContainEvacuationPause = "2018-08-23T12:23:25.710+0200: 9.636: [GC pause (G1 Evacuation Pause) (young) G1HR #StartGC 8\n";
    chunkContainsCouncurrentMark = "2018-08-23T12:24:04.060+0200: 47.985: [GC concurrent-mark-end, 0.7346769 secs]";
    chunkContainsMixedGC = "2018-08-23T12:25:22.608+0200: 126.533: [GC pause (G1 Evacuation Pause) (mixed) G1HR #StartGC 253";
    chunkContainsYoungGC = "2018-08-23T12:25:20.941+0200: 124.867: [GC pause (G1 Evacuation Pause) (young) G1HR #StartGC 248";
  }

  @Test
  public void parseNonPausetimeTag() {
    GcData gcData = parser.parse(chunkDoesNotContainPause);
    assertThat(gcData.getName(), not(Name.PAUSE_TIME));
  }

  @Test
  public void parseMoreThanOneLine() {
    boolean shouldReadMoreLine = parser.shouldReadMoreLine(chunkDoesNotContainPause);
    assertTrue(shouldReadMoreLine);
  }

  @Test
  public void parseEmptyChunk() {
    GcData gcData = parser.parse(emptyChunk);
    assertThat(gcData.getName(), is(Name.EMPTY));
  }

  @Test
  public void countEvacuationPause() {
    parser.parse(chunkContainEvacuationPause);
    assertEquals(1, parser.getEvacuationPauseCount());
  }

  @Test
  public void parseCouncurrentMark() {
    GcData gcData = parser.parse(chunkContainsCouncurrentMark);
    assertThat(gcData.getName(), is(Name.CONCURRENT_MARK));
    assertEquals(0.7346769, gcData.getValue(), 0.001);
    assertEquals(1, Parser.getConcurrentMarkCount());
  }

  @Test
  public void parseMixedGC() {
    parser.parse(chunkContainsMixedGC);
    assertEquals(Parser.getCurrentGC(), Name.MIXED_GC);
  }

  @Test
  public void parseMixedGCTime() {
    GcData gcData = parser.parse(chunkContainsEvacuationTime);
    assertEquals(0.0262454, gcData.getValue(), 0.001);
    assertEquals(gcData.getName(), Name.MIXED_GC);
  }

  @Test
  public void parseYoungGC() {
    parser.parse(chunkContainsYoungGC);
    assertEquals(Parser.getCurrentGC(), Name.YOUNG_GC);
  }

  @Test
  public void parseYoungGCTime() {
    GcData gcData = parser.parse(chunkContainsEvacuationTime);
    assertEquals(0.0262454, gcData.getValue(), 0.001);
    assertEquals(gcData.getName(), Name.YOUNG_GC);
  }

}
