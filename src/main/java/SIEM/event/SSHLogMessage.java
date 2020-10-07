package SIEM.event;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class SSHLogMessage {
    private String message;
    private String epochTimestamp;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEpochTimestamp() {
        return epochTimestamp;
    }

    public void setEpochTimestamp(String epochTimestamp) {
        this.epochTimestamp = epochTimestamp;
    }

    @Override
    public String toString(){
        long epochTimestamp = Long.valueOf(this.epochTimestamp.substring(0, 13));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ssZZZZ");
        Date date = Date.from(Instant.ofEpochMilli(epochTimestamp));
        return "On " + sdf.format(date) + ": " + message; 
    }

    public SSHLogMessage(String message, String epochTimestamp) {
        this.message = message;
        this.epochTimestamp = epochTimestamp;
    }
}
