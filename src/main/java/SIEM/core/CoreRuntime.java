package SIEM.core;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPRuntimeProvider;
import com.espertech.esper.runtime.client.EPStatement;

import SIEM.event.SSHAlert;
import SIEM.event.SSHFailedLogMessage;
import SIEM.util.Common;

public class CoreRuntime {
    private final CoreCompiler coreCompiler;
    private final Configuration configuration;
    private int countFailed;

    public CoreRuntime(CoreCompiler coreCompiler) {
        this.coreCompiler = coreCompiler;
        configuration = Common.getConfiguration();
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
                    .deploy(coreCompiler.getEpCompiledByName("SSHLogMessage"));
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

            String[] slices = message.split(" ");
            String senderIpAddr = slices[5];
            int port = Integer.valueOf(slices[7]);
            long epochTimestamp = Long.valueOf(epochTimestampStr.substring(0, 13));
            Date date = Date.from(Instant.ofEpochMilli(epochTimestamp));

            // Send event SSHFailedLogMessage
            countFailed++;
            rt.getEventService().sendEventBean((new SSHFailedLogMessage(senderIpAddr, port, date)),
                    "SSHFailedLogMessage");
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
            if (countFailed > Common.FAILED_MAX)
                rt.getEventService().sendEventBean((new SSHAlert()), "SSHAlert");
        });

        // Set up statement SSHAlert
        EPStatement statement2 = runtime.getDeploymentService().getStatement(deploySSHAlert.getDeploymentId(),
                "ssh-alert");
        statement2.addListener((newData, oldData, stmt, rt) -> {
            // Print alert
            System.out.println((String) newData[0].get("alertMessage"));
        });

        return runtime;
    }
}
