package it.gmarseglia.weka.util;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Configs {
    protected static final List<String> PROJECTS = Arrays.asList("BOOKKEEPER", "OPENJPA");
    public static final String TRAINING_SUFFIX = "_trainingset";
    public static final String TESTING_SUFFIX = "_Testingset";
    public static final Path WEKA_DIR = Path.of("out", "WEKA");
    public static final String ARFF_EXT = ".arff";

    private Configs(){
    }

    public static Path getProjInDir(String projName){
        return Path.of("out", projName, "datasets");
    }
    public static Path getProjOutDir(String projName){
        return Configs.WEKA_DIR.resolve(projName);
    }


    public static List<String> getProjects (){
        return PROJECTS;
    }
}
