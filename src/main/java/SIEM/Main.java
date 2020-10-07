package SIEM;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import com.espertech.esper.runtime.client.EPRuntime;
import org.json.JSONException;
import org.json.JSONObject;
import SIEM.event.SSHLogMessage;
import SIEM.util.MyInitializer;

/**
 * Hello world!
 *
 */
public class Main {
    public static void main(String[] args) {
        // EPRuntime runtime2 = new MyInitializer2().init();
        MyInitializer myInitializer = new MyInitializer();
        // myInitializer.setRuntime2(runtime2);
        EPRuntime runtime = myInitializer.init();

        ProcessBuilder builder = new ProcessBuilder("bash", "-c", "journalctl -u ssh.service -o json");
        builder.redirectErrorStream(true);
        Process process;
        try {
            process = builder.start();
            InputStream is = process.getInputStream();
            Scanner scanner = new Scanner(is);
            while (scanner.hasNextLine()) {
                try {
                    JSONObject logObj = new JSONObject(scanner.nextLine());
                    String logMsg = logObj.getString("MESSAGE");
                    String epochtime = logObj.getString("__REALTIME_TIMESTAMP");

                    // Send event
                    runtime.getEventService().sendEventBean(new SSHLogMessage(logMsg, epochtime), "SSHLogMessage");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            scanner.close();
        } catch (IOException e1) {
            System.err.println("Cannot execute bash command.");
            e1.printStackTrace();
        }
    }
}
