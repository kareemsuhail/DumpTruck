package com.careem.opensource;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GcData {

  public enum Name {
    PAUSE_TIME("pause_time"),
    CONCURRENT_MARK("concurrent_mark"),
    MIXED_GC("mixed_gc"),
    YOUNG_GC("young_gc"),
    PREDICTED_BASE_TIME("predicted_base_time"),
    PREDICTED_PAUSE_TIME("predicted_pause_time"),
    MAX_PAUSE_TIME("max_pause_time"),
    EMPTY("");

    private final String name;

    Name(final String name) {
      this.name = name;
    }

    public String toString() {
      return name;
    }
  }

  @Builder.Default
  Name name = Name.EMPTY;

  @Builder.Default
  String tag = "";

  @Builder.Default
  double value = 0.0;

  boolean shouldReadMoreLine;
}
