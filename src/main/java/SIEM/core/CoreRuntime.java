package SIEM.core;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPRuntimeProvider;
import com.espertech.esper.runtime.client.EPStatement;

import SIEM.event.SSHAlert;
import SIEM.event.SSHFailedLogMessage;
import SIEM.event.SSHLogMessage;
import SIEM.util.Common;
import SIEM.core.*;

public class CoreRuntime {
    private final CoreCompiler coreCompiler;
    private final Configuration configuration;
    private EPRuntime runtime;
    private final int MAX_FAILED = 10;
    private int countFailed;

    public CoreRuntime(CoreCompiler coreCompiler) {
        this.coreCompiler = coreCompiler;
        configuration = Common.getConfiguration();
        runtime = EPRuntimeProvider.getDefaultRuntime(configuration);
        countFailed = 0;
    }

    public EPRuntime getEPRuntime() {
        EPRuntime runtime = EPRuntimeProvider.getDefaultRuntime(configuration);

        // Set up for deployment
        EPDeployment deploySSHLogMessageDeploy;
        EPDeployment deploySSHFailedLogMessage;
        EPDeployment deploySSHAlert;

        try {
            deploySSHLogMessageDeploy = runtime.getDeploymentService()
                    .deploy(coreCompiler.getEpCompiled(SSHLogMessage.class));
            deploySSHFailedLogMessage = runtime.getDeploymentService()
                    .deploy(coreCompiler.getEpCompiled(SSHFailedLogMessage.class));
            deploySSHAlert = runtime.getDeploymentService().deploy(coreCompiler.getEpCompiled(SSHAlert.class));
        } catch (EPDeployException ex) {
            throw new RuntimeException(ex);
        }

        // Set up statement SSHLogMessage
        EPStatement statement = runtime.getDeploymentService().getStatement(deploySSHLogMessageDeploy.getDeploymentId(),
                "ssh-log-message");
        statement.addListener((newData, oldData, stmt, rt) -> {
            String message = (String) newData[0].get("message");
            String epochTimestampStr = (String) newData[0].get("epochTimestamp");
            // Print SSHFailedLogMessage
            // System.out.println("SSHLogMessage | Epoch: " + message + " - Message: " +
            // message);

            // Select failed message and send event
            if (message.matches("Failed password for (.*) from (.*).(.*).(.*).(.*) port (.*) ssh2")) {
                String[] slices = message.split(" ");
                String senderIpAddr = slices[5];
                int port = Integer.valueOf(slices[7]);
                long epochTimestamp = Long.valueOf(epochTimestampStr.substring(0, 13));
                Date date = Date.from(Instant.ofEpochMilli(epochTimestamp));

                // Send event SSHFailedLogMessage
                countFailed++;
                rt.getEventService().sendEventBean((new SSHFailedLogMessage(senderIpAddr, port, date)),
                        "SSHFailedLogMessage");
            }
        });

        // Set up statement SSHFailedLogMessage
        EPStatement statement1 = runtime.getDeploymentService()
                .getStatement(deploySSHFailedLogMessage.getDeploymentId(), "ssh-failed-log-message");
        statement1.addListener((newData, oldData, stmt, rt) -> {
            String senderIpAddr = (String) newData[0].get("senderIpAddr");
            int port = (int) newData[0].get("port");
            Date date = (Date) newData[0].get("date");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ssZZZZ");
            String dateStr = sdf.format(date);
            // Print SSHFailedLogMessage
            System.out.println("SSHFailedLogMessage | " + dateStr + " from " + senderIpAddr + " on port " + port);

            // Send event SSHAlert
            if (countFailed > MAX_FAILED)
                rt.getEventService().sendEventBean((new SSHAlert(senderIpAddr, port, date)), "SSHAlert");
        });

        // Set up statement SSHAlert
        EPStatement statement2 = runtime.getDeploymentService().getStatement(deploySSHAlert.getDeploymentId(),
                "ssh-alert");
        statement2.addListener((newData, oldData, stmt, rt) -> {
            // Print alert message
            System.out.println("------------------------------------");
            System.out.println("SSH Alert - Failed more than " + MAX_FAILED + " times");
            System.out.println("------------------------------------");
        });

        return runtime;
    }
}
