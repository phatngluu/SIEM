package SIEM.util;

import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;

import SIEM.event.*;

public class Common {
    private static Configuration configuration;
    public static Configuration getConfiguration(){
        if (configuration == null) {
            configuration = new Configuration();
        }

        return configuration;
    }
    public static Configuration setConfiguration(Configuration conf){
        configuration = conf;
        return configuration;
    }

    private static CompilerArguments compilerArguments;
    public static CompilerArguments getCompilerArguments(){
        if (compilerArguments == null) {
            return new CompilerArguments();
        }
        return compilerArguments;
    }
}
