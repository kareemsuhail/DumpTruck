import com.sun.xml.internal.bind.v2.TODO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;
import java.util.logging.Logger;
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
                        Parser.analyizePauseTime(tempLine);
                        lastKnownLocation++;
                    }

                } else {
                    Scanner fileReader = new Scanner(logFile);
                    while (fileReader.hasNext()) {
                        String tempLine = fileReader.nextLine();
                        Parser.analyizePauseTime(tempLine);

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

    public static String pauseTimePatternString = ", \\d+\\.\\d+ secs]";
    public static String floatNumberPatternString = "([0-9]*[.])?[0-9]+";
    public static Pattern floatNumberPattern = Pattern.compile(floatNumberPatternString);

    public static void analyizePauseTime(String line) {
        boolean hasPause = line.matches(pauseTimePatternString);
        if (hasPause) {
            try {
                Matcher secsMatcher = floatNumberPattern.matcher(line);
                secsMatcher.find();
                System.out.println(secsMatcher.group(0));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
