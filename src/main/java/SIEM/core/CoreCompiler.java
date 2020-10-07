package SIEM.core;

import java.util.HashMap;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;

import SIEM.util.*;

public class CoreCompiler {
    private HashMap<Class, EPCompiled> epCompiledMap;
    private Configuration configuration;

    public CoreCompiler() {
        epCompiledMap = new HashMap<Class, EPCompiled>();
        configuration = Common.getConfiguration();
    }

    public void compile(String epl, Class eventType) {
        configuration.getCommon().addEventType(eventType);
        CompilerArguments compilerArguments = new CompilerArguments(configuration);

        EPCompiler compiler = EPCompilerProvider.getCompiler();
        try {
            EPCompiled epCompiled = compiler.compile(epl, compilerArguments);
            epCompiledMap.put(eventType, epCompiled);
        } catch (EPCompileException ex) {
            // handle exception here
            throw new RuntimeException(ex);
        }
    }

    public HashMap<Class, EPCompiled> getEpCompiledMap(){
        return epCompiledMap;
    }
}
