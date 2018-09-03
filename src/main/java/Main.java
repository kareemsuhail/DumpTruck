import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import repository.GcLogReader;
import service.Parser;

public class Main {

  public static void main(String[] args) {
    MeterRegistry meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    Parser parser = new Parser(meterRegistry);
    GcLogReader stream = new GcLogReader(
        "gc.log.0.current", "/home/ksp/log", 1, parser
    );
    stream.run();
  }
}
