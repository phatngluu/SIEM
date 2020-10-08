package SIEM.event;

import SIEM.util.Common;

public class SSHAlert {
    private String alertMessage;

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public SSHAlert() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("------------------------------------\n");
        stringBuilder.append("SSH Alert - Failed more than " + Common.FAILED_MAX + " times\n");
        stringBuilder.append("------------------------------------");
        this.alertMessage = stringBuilder.toString();
    }
}
