import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GCStream {

    private File logFile;
    private int delay;
    private boolean random;
    private long lastKnownLocation;

    public GCStream(String logFilePath, int delay, boolean random) {
        this.logFile = new File(logFilePath);
        this.delay = delay;
        this.random = random;

    }

    public static void main(String[] args) {
        //log file path is hardcoded but should be changed
        // TODO change path from hardcoded to system arguments
        GCStream stream = new GCStream("C:\\Users\\engka\\OneDrive\\Documents\\NetBeansProjects\\GC_project\\src\\gc_project\\gc.log.1.current", 5000, false);
        stream.run();
    }

    public void run() {
        Parser parser = new Parser();
        parser.setup();
        while(true){

            try {
                Thread.sleep(delay);
                if (random) {
                    long fileLength = logFile.length();
                    RandomAccessFile randomAccessLogFile = new RandomAccessFile(logFile,"r");
                    if(fileLength > lastKnownLocation){


                    }else if(fileLength < lastKnownLocation){
                        lastKnownLocation = 0l;

                    }
                    randomAccessLogFile.seek(lastKnownLocation);
                    String tempLine = null;
                    while ((tempLine = randomAccessLogFile.readLine()) != null) {
                        parser.analyzePauseTime(tempLine);
                        lastKnownLocation++;
                    }

                } else {
                    Scanner fileReader = new Scanner(logFile);
                    while (fileReader.hasNext()) {
                        String tempLine = fileReader.nextLine();
                        parser.analyzePauseTime(tempLine);

                    }
                }

            } catch (FileNotFoundException ex) {
                System.out.println("logs file is not found");
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }   catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
        }


    }
}

class Parser {

    Timer timer ;

    public static String pauseTimePatternString = ", \\d+\\.\\d+ secs]";
    public static String floatNumberPatternString = "([0-9]*[.])?[0-9]+";
    public static Pattern floatNumberPattern = Pattern.compile(floatNumberPatternString);
    PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    public void analyzePauseTime(String line) {
        boolean hasPause = line.matches(pauseTimePatternString);
        if (hasPause) {
            try {
                Matcher secsMatcher = floatNumberPattern.matcher(line);
                secsMatcher.find();
                System.out.println(secsMatcher.group(0));
                timer.record(122, TimeUnit.MILLISECONDS);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    public  void setup(){

        try {
            //TODO change prometheus config
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/prometheus", httpExchange -> {
                String response = prometheusRegistry.scrape();
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });

            new Thread(server::start).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        timer = Timer
                .builder("pause.time")
                .description("GC pause time timer") // optional
                .tags("region", "test") // optional
                .register(prometheusRegistry);


    }
}
