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
import SIEM.event.SSHLogMessage;

public class Main {
    public static void main(String[] args) {
        // Setting up compiler
        CoreCompiler coreCompiler = new CoreCompiler();
        coreCompiler.compile("@name('ssh-log-message') select message, epochTimestamp from SSHLogMessage", SSHLogMessage.class);
        coreCompiler.compile("@name('ssh-failed-log-message') select senderIpAddr, port, date from SSHFailedLogMessage", SSHFailedLogMessage.class);
        coreCompiler.compile("@name('ssh-alert') select alertMessage from SSHAlert", SSHAlert.class);
        
        // Setting up run time
        CoreRuntime coreRuntime = new CoreRuntime(coreCompiler);
        EPRuntime runtime = coreRuntime.getEPRuntime();

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
