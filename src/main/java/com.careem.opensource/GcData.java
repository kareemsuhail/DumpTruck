package com.careem.opensource;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GcData {
  @Builder.Default
  String name = "";

  @Builder.Default
  String tag = "";

  @Builder.Default
  double value = 0.0;
}
