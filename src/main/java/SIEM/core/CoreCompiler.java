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
    private HashMap<String, EPCompiled> epCompiledHashMap;

    private Configuration configuration;

    public CoreCompiler() {
        epCompiledMap = new HashMap<Class, EPCompiled>();
        epCompiledHashMap = new HashMap<String, EPCompiled>();
        configuration = Common.getConfiguration();
    }

    public EPCompiled compile(String epl, Class eventType) {
        
        // Set new config and update to Common
        configuration = Common.getConfiguration();
        configuration.getCommon().addEventType(eventType);
        CompilerArguments compilerArguments = new CompilerArguments(configuration);

        // Compile
        EPCompiler compiler = EPCompilerProvider.getCompiler();
        try {
            EPCompiled epCompiled = compiler.compile(epl, compilerArguments);
            epCompiledMap.put(eventType, epCompiled);
            return epCompiled;
        } catch (EPCompileException ex) {
            System.out.println("Cannot compile \"" + epl + "\" of event type " + eventType.getName());
            ex.printStackTrace();
            return null;
        }
    }

    public EPCompiled compileByName(String epl, String eventTypeName) {
        
        // Set new config and update to Common
        configuration = Common.getConfiguration();
        CompilerArguments compilerArguments = new CompilerArguments(configuration);

        // Compile
        EPCompiler compiler = EPCompilerProvider.getCompiler();
        try {
            EPCompiled epCompiled = compiler.compile(epl, compilerArguments);
            epCompiledHashMap.put(eventTypeName, epCompiled);
            return epCompiled;
        } catch (EPCompileException ex) {
            System.out.println("Cannot compile \"" + epl + "\" of event type " + eventTypeName);
            ex.printStackTrace();
            return null;
        }
    }

    public EPCompiled getEpCompiled(Class eventType) {
        return epCompiledMap.get(eventType);
    }

    public EPCompiled getEpCompiledByName(String eventTypeName){
        return epCompiledHashMap.get(eventTypeName);
    }

    public HashMap<Class, EPCompiled> getEpCompiledMap(){
        return epCompiledMap;
    }
}
