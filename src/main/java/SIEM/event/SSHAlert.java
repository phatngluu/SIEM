package SIEM.event;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SSHAlert {
    private String alertMessage;

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public SSHAlert(String senderIpAddr, int port, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ssZZZZ");
        String dateStr = sdf.format(date);
        this.alertMessage = "SSHAlert - " + dateStr + " - Failed on port " + port + " from " + senderIpAddr;
    }
}
