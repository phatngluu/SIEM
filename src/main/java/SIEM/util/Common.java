package SIEM.util;

import com.espertech.esper.common.client.configuration.Configuration;

public class Common {
    private static Configuration configuration;
    public static Configuration getConfiguration(){
        if (configuration == null) {
            return new Configuration();
        }
        return configuration;
    }
}
