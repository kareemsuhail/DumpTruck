package com.careem.opensource;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GcData {
  String name;
  String tag;
  double value;
}
