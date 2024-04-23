package it.gmarseglia.app.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MyLogger {

    private static final Map<String, MyLogger> instances = new HashMap<>();
    private static Boolean staticVerbose = false;
    private static Boolean staticVerboseFine = false;
    private final String className;
    private Boolean verbose;
    private Boolean verboseFine;

    private MyLogger(String className) {
        this.className = className;
    }

    public static MyLogger getInstance(String className) {
        MyLogger.instances.computeIfAbsent(className, string -> new MyLogger(className));
        return MyLogger.instances.get(className);
    }

    public static MyLogger getInstance(Class<?> actualClass) {
        String simplePackageName = Arrays.stream(actualClass.getPackageName().split("\\.")).toList().getLast();
        return MyLogger.getInstance(simplePackageName + "." + actualClass.getSimpleName());
    }

    public static void setStaticVerbose(Boolean staticVerbose) {
        MyLogger.staticVerbose = staticVerbose;
    }

    public static void setStaticVerboseFine(Boolean staticVerboseFine) {
        MyLogger.staticVerboseFine = staticVerboseFine;
    }

    public void log(Runnable runnable) {
        if ((verbose == null && staticVerbose) || Boolean.TRUE.equals(verbose)) {
            System.out.printf("%s logs:\t", this.className);
            runnable.run();
        }
    }

    public void logFine(Runnable runnable) {
        if ((verboseFine == null && staticVerboseFine) || Boolean.TRUE.equals(verboseFine)) {
            System.out.printf("%s logs:\t", this.className);
            runnable.run();
        }
    }

    public void logObject(Object object) {
        this.log(() -> System.out.println(object));
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setVerboseFine(Boolean verboseFine) {
        this.verboseFine = verboseFine;
    }
}
