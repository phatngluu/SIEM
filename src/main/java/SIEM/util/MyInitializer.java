package SIEM.util;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

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
import SIEM.core.*;

public class MyInitializer {
    public static int countFailed = 0;
    private final static int MAX_FAILED = 3;

    private EPCompiled epCompiled;
    private EPCompiled epCompiled1;
    private EPCompiled epCompiled2;
    // Get config
    Configuration configuration;
    CompilerArguments compilerArguments;

    public EPRuntime init() {
        // configuration.getCommon().addEventType(SSHLogMessage.class);
        // configuration.getCommon().addEventType(SSHFailedLogMessage.class);
        // configuration.getCommon().addEventType(SSHAlert.class);

        // configuration.getCommon().addEventType(SSHLogMessage.class);
        // configuration.getCommon().addEventType(SSHFailedLogMessage.class);
        // configuration.getCommon().addEventType(SSHAlert.class);
        // initCompiler();
        initCompiler2();
        configuration = Common.getConfiguration();
        // epCompiled = compile("@name('ssh-log-message') select message, epochTimestamp from SSHLogMessage", SSHLogMessage.class);
        // epCompiled1 = compile("@name('ssh-failed-log-message') select senderIpAddr, port, date from SSHFailedLogMessage", SSHFailedLogMessage.class);
        // epCompiled2 = compile("@name('ssh-alert') select alertMessage from SSHAlert", SSHAlert.class);
        if (epCompiled != null && epCompiled1 != null && epCompiled2 != null) {
            return initRuntime();
        }
        return null;
    }

    private EPCompiled compile(String epl, Class eventType) {
        System.out.println("Compiling...");
        configuration.getCommon().addEventType(eventType);
        compilerArguments = new CompilerArguments(configuration);

        EPCompiler compiler = EPCompilerProvider.getCompiler();
        try {
            EPCompiled epCompiled = compiler.compile(epl, compilerArguments);
            //epCompiledMap.put(eventType, epCompiled);
            return epCompiled;
        } catch (EPCompileException ex) {
            // handle exception here
            throw new RuntimeException(ex);
        }
    }


    private void initCompiler2() {
        CoreCompiler coreCompiler = new CoreCompiler();
        coreCompiler.compile("@name('ssh-log-message') select message, epochTimestamp from SSHLogMessage", SSHLogMessage.class);
        coreCompiler.compile("@name('ssh-failed-log-message') select senderIpAddr, port, date from SSHFailedLogMessage", SSHFailedLogMessage.class);
        coreCompiler.compile("@name('ssh-alert') select alertMessage from SSHAlert", SSHAlert.class);

        epCompiled = coreCompiler.getEpCompiled(SSHLogMessage.class);
        epCompiled1 = coreCompiler.getEpCompiled(SSHFailedLogMessage.class);
        epCompiled2 = coreCompiler.getEpCompiled(SSHAlert.class);
    }

    private void initCompiler() {
        // Get compiler args
        // CompilerArguments compilerArguments = new CompilerArguments(configuration);

        configuration.getCommon().addEventType(SSHLogMessage.class);
        CompilerArguments compilerArguments = new CompilerArguments(configuration);
        
        
        // Compile SSHLogMessage
        EPCompiler compiler = EPCompilerProvider.getCompiler();
        try {
            // @name annotation and assigns a name my-statement to the statement.
            epCompiled = compiler.compile("@name('ssh-log-message') select message, epochTimestamp from SSHLogMessage",
            compilerArguments);
            
        } catch (EPCompileException ex) {
            // handle exception here
            throw new RuntimeException(ex);
        }
        
        // Compile SSHFailedLogMessage
        
        configuration.getCommon().addEventType(SSHFailedLogMessage.class);
        compilerArguments = new CompilerArguments(configuration);
        EPCompiler compiler1 = EPCompilerProvider.getCompiler();
        try {
            // @name annotation and assigns a name my-statement to the statement.
            epCompiled1 = compiler1.compile(
                "@name('ssh-failed-log-message') select senderIpAddr, port, date from SSHFailedLogMessage",
                compilerArguments);
            } catch (EPCompileException ex) {
                // handle exception here
                throw new RuntimeException(ex);
            }
            
            // Compile SSHFailedLogMessage
            
            configuration.getCommon().addEventType(SSHAlert.class);
            compilerArguments = new CompilerArguments(configuration);
            
            EPCompiler compiler2 = EPCompilerProvider.getCompiler();
            try {
                // @name annotation and assigns a name my-statement to the statement.
                epCompiled2 = compiler2.compile("@name('ssh-alert') select alertMessage from SSHAlert", compilerArguments);
            } catch (EPCompileException ex) {
                // handle exception here
                throw new RuntimeException(ex);
            }
        }
        
    private EPRuntime initRuntime() {
            
        // Set up runtime
        EPRuntime runtime = EPRuntimeProvider.getDefaultRuntime(configuration);
        EPDeployment deployment;
        EPDeployment deployment1;
        EPDeployment deployment2;
        try {
            deployment = runtime.getDeploymentService().deploy(epCompiled);
            deployment1 = runtime.getDeploymentService().deploy(epCompiled1);
            deployment2 = runtime.getDeploymentService().deploy(epCompiled2);
        } catch (EPDeployException ex) {
            // handle exception here
            throw new RuntimeException(ex);
        }

        // Set up statement SSHLogMessage
        EPStatement statement = runtime.getDeploymentService().getStatement(deployment.getDeploymentId(),
                "ssh-log-message");
        statement.addListener((newData, oldData, stmt, rt) -> {
            String message = (String) newData[0].get("message");
            String epochTimestampStr = (String) newData[0].get("epochTimestamp");
            // Print SSHFailedLogMessage
            // System.out.println("SSHLogMessage | Epoch: " + message + " - Message: " + message);

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
        EPStatement statement1 = runtime.getDeploymentService().getStatement(deployment1.getDeploymentId(),
                "ssh-failed-log-message");
        statement1.addListener((newData, oldData, stmt, rt) -> {
            String senderIpAddr = (String) newData[0].get("senderIpAddr");
            int port = (int) newData[0].get("port");
            Date date = (Date) newData[0].get("date");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ssZZZZ");
            String dateStr = sdf.format(date);
            // Print SSHFailedLogMessage
            System.out.println("SSHFailedLogMessage | " + dateStr + " from " + senderIpAddr + " on port " + port);

            // Send event SSHAlert
            if (countFailed > MAX_FAILED )
            rt.getEventService().sendEventBean((new SSHAlert(senderIpAddr, port, date)),
                        "SSHAlert");
        });

        // Set up statement SSHAlert
        EPStatement statement2 = runtime.getDeploymentService().getStatement(deployment2.getDeploymentId(),
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
