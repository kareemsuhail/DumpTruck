package com.careem.opensource;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * TODO: this is for testing. Remove later and write tests
 */
public class Main {

  public static void main(String[] args) {
    MeterRegistry meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    Parser parser = new Parser();
    Reporter gcLogReader = new Reporter(
        meterRegistry, "/tmp", "gc.log", parser
    );
    gcLogReader.start();
  }
}
