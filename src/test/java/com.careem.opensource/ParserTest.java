package com.careem.opensource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.careem.opensource.GcData.Name;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParserTest {

  private static Parser parser;
  private String chunk;

  @BeforeClass
  public static void setup() {
    parser = new Parser();
  }

  @Before
  public void setupForEach() {
    chunk = ", 0.0262454 secs]";
  }

  @Test
  public void parse() {
    GcData gcData = parser.parse(chunk);
    assertThat(gcData.getName(), is(Name.PAUSE_TIME));
  }
}
