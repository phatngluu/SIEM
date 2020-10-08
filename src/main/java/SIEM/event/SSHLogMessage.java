package SIEM.event;

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

    public SSHLogMessage(String message, String epochTimestamp) {
        this.message = message;
        this.epochTimestamp = epochTimestamp;
    }
}
