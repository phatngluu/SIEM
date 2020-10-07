As raw event type “SSHLogMessage” is defined. 
Detected failed logins should be events of type “SSHFailedLogMessage”. 
If a defined threshold of failed logins is detected, an event of type “SSHAlert” should be raised.

Now feed the log messages into the CEP engine as raw events of event type “SSHLogMessage” and create events using appropriate EPL statements to implement the Event Hierarchy shown above.

Keep in mind, that an event of type “SSHAlert” should only appear if a certain amount of consecutive failed logins (which you can define by yourself or can be defined by the user) took place.


    

    @Override
    public String toString(){
        long epochTimestamp = Long.valueOf(this.epochTimestamp.substring(0, 13));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ssZZZZ");
        Date date = Date.from(Instant.ofEpochMilli(epochTimestamp));
        return "On " + sdf.format(date) + ": " + message; 
    }

    public SSHFailedLogMessage(String message, String epochTimestamp) {
        this.message = message;
        this.epochTimestamp = epochTimestamp;
    }