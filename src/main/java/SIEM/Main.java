package SIEM;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import com.espertech.esper.runtime.client.EPRuntime;
import org.json.JSONException;
import org.json.JSONObject;

import SIEM.core.CoreCompiler;
import SIEM.core.CoreRuntime;
import SIEM.event.SSHAlert;
import SIEM.event.SSHFailedLogMessage;

public class Main {
    public static void main(String[] args) {
        // Setting up compiler
        CoreCompiler coreCompiler = new CoreCompiler();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("@JsonSchema(dynamic=true) @public @buseventtype create json schema SSHLogMessage(__REALTIME_TIMESTAMP string, MESSAGE string);\n");
        stringBuilder.append("@name('ssh-log-message') select __REALTIME_TIMESTAMP as epochTimestamp, MESSAGE as message from SSHLogMessage where MESSAGE LIKE 'Failed password for%';");
        coreCompiler.compileByName(stringBuilder.toString(), "SSHLogMessage");
        coreCompiler.compile("@name('ssh-failed-log-message') select senderIpAddr, port, date from SSHFailedLogMessage;", SSHFailedLogMessage.class);
        coreCompiler.compile("@name('ssh-alert') select alertMessage from SSHAlert;", SSHAlert.class);

        // Setting up run time
        CoreRuntime coreRuntime = new CoreRuntime(coreCompiler);
        EPRuntime runtime = coreRuntime.getEPRuntime();

        // Latest log epochtime
        long latestLogEpochtime = -1;

        while (true) {
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", "journalctl -u ssh.service -o json");
            Process process;
            try {
                process = builder.start();
                InputStream is = process.getInputStream();
                Scanner scanner = new Scanner(is);
                while (scanner.hasNextLine()) {
                    try {
                        String json = scanner.nextLine();
                        if (!json.isBlank()) {
                            JSONObject logObj = new JSONObject(json);
                            String epochtime = logObj.getString("__REALTIME_TIMESTAMP");
                            long logEpochtime = Long.valueOf(epochtime);
                            if (latestLogEpochtime < logEpochtime) {
                                latestLogEpochtime = logEpochtime;
                                String logMsg = logObj.getString("MESSAGE");
                                // Send event
                                runtime.getEventService().sendEventJson(json, "SSHLogMessage");
                            }
                        }
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
}
