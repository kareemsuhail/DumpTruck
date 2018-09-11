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

  private static Parser parser;
  private static String chunk;
  private static String chunkDoesNotContainPause;
  private static String chunkContainEvacuationPause;
  private static String emptyChunk;

  @BeforeClass
  public static void setup() {
    parser = new Parser();
    chunk = ", 0.0262454 secs]";
    chunkDoesNotContainPause = "[ObjectSynchronizer Roots (ms):  0.0  0.0  0.0  0.0\n"
        + "          Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]";
    emptyChunk = " \n ";
    chunkContainEvacuationPause = "2018-08-23T12:23:25.710+0200: 9.636: [GC pause (G1 Evacuation Pause) (young) G1HR #StartGC 8\n";
  }

  @Before
  public void setupForEach() {

  }

  @Test
  public void parse() {
    GcData gcData = parser.parse(chunk);
    assertThat(gcData.getName(), is(Name.PAUSE_TIME));
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
  public void countEvacuationPause(){
    parser.parse(chunkContainEvacuationPause);
    assertEquals(1,parser.getEvacuationPauseCount());
  }


}
