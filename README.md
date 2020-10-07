As raw event type “SSHLogMessage” is defined. 
Detected failed logins should be events of type “SSHFailedLogMessage”. 
If a defined threshold of failed logins is detected, an event of type “SSHAlert” should be raised.

Now feed the log messages into the CEP engine as raw events of event type “SSHLogMessage” and create events using appropriate EPL statements to implement the Event Hierarchy shown above.

Keep in mind, that an event of type “SSHAlert” should only appear if a certain amount of consecutive failed logins (which you can define by yourself or can be defined by the user) took place.