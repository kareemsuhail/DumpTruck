package com.careem.opensource;

import io.micrometer.core.instrument.Metrics;

public class Main {

  public static void main(String[] args) {
    new Reporter(Metrics.globalRegistry, "/tmp", "gc.log", new Parser()).start();
  }
}
