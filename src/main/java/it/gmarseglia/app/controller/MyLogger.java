package it.gmarseglia.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class MyLogger {

    private static final String FORMAT = "%-11s %s";
    private static final Map<String, MyLogger> instances = new HashMap<>();
    private static Boolean staticVerbose = false;
    private static Boolean staticVerboseFine = false;
    private static Boolean staticVerboseFinest = false;
    private final Logger internalLogger;
    private Boolean verbose;
    private Boolean verboseFine;
    private Boolean verboseFinest;

    private MyLogger(String className) {
        String cappedClassName = String.format("%-24s", className);
        this.internalLogger = LoggerFactory.getLogger(cappedClassName);
    }

    public static MyLogger getInstance(String className) {
        MyLogger.instances.computeIfAbsent(className, MyLogger::new);
        return MyLogger.instances.get(className);
    }

    public static MyLogger getInstance(Class<?> actualClass) {
        return MyLogger.getInstance(actualClass.getSimpleName());
    }

    public static void setStaticVerboseFinest(Boolean staticVerboseFinest) {
        MyLogger.staticVerboseFinest = staticVerboseFinest;
    }

    public static void setStaticVerbose(Boolean staticVerbose) {
        MyLogger.staticVerbose = staticVerbose;
    }

    public static void setStaticVerboseFine(Boolean staticVerboseFine) {
        MyLogger.staticVerboseFine = staticVerboseFine;
    }

    public void logNoPrefix(String msg) {
        if ((verbose == null && staticVerbose) || Boolean.TRUE.equals(verbose)) {
            internalLogger.info(msg);
        }
    }

    public void log(String msg) {
        String logMsg = String.format(FORMAT, "logs:", msg);
        this.logNoPrefix(logMsg);
    }

    public void logFineNoPrefix(String msg) {
        if ((verboseFine == null && staticVerboseFine) || Boolean.TRUE.equals(verboseFine)) {
            internalLogger.info(msg);
        }
    }

    public void logFine(String msg) {
        String logMsg = String.format(FORMAT, "logs fine:", msg);
        this.logFineNoPrefix(logMsg);
    }

    public void logFinestNoPrefix(String msg) {
        if ((verboseFinest == null && staticVerboseFinest) || Boolean.TRUE.equals(verboseFinest)) {
            internalLogger.info(msg);
        }
    }

    public void logFinest(String msg) {
        String logMsg = String.format(FORMAT, "logs finest:", msg);
        this.logFinestNoPrefix(logMsg);
    }

    public void logObject(Object object) {
        this.log(object.toString());
    }

    public void logObjectNoPrefix(Object object) {
        this.logNoPrefix(object.toString());
    }

    public void setVerbose(Boolean verbose) {
        this.verbose = verbose;
    }

    public void setVerboseFine(Boolean verboseFine) {
        this.verboseFine = verboseFine;
    }

    public void setVerboseFinest(Boolean verboseFinest) {
        this.verboseFinest = verboseFinest;
    }

    public boolean getAnyVerboseFinest() {
        return (verboseFinest == null && staticVerboseFinest) || Boolean.TRUE.equals(verboseFinest);
    }

    public boolean getAnyVerboseFine() {
        return (verboseFine == null && staticVerboseFine) || Boolean.TRUE.equals(verboseFine);

    }
}
