package SIEM.event;

import java.util.Date;

public class SSHFailedLogMessage {
    private String senderIpAddr;
    private int port;
    private Date date;

    public String getSenderIpAddr() {
        return senderIpAddr;
    }

    public void setSenderIpAddr(String senderIpAddr) {
        this.senderIpAddr = senderIpAddr;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public SSHFailedLogMessage(String senderIpAddr, int port, Date date) {
        this.senderIpAddr = senderIpAddr;
        this.port = port;
        this.date = date;
    }
}
