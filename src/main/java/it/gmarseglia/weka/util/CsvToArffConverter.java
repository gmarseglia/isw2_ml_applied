package it.gmarseglia.weka.util;

import it.gmarseglia.app.controller.MyFileUtils;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class CsvToArffConverter {

    public static void main(String[] args) throws IOException {

        List<String> projects = Arrays.asList("BOOKKEEPER", "OPENJPA");

        // Create WEKA output directory
        Path wekaDir = Path.of("out", "WEKA");
        MyFileUtils.createDirectory(wekaDir);

        for (String projName : projects) {
            // projOutDir = "out/WEKA/XXX"
            Path projOutDir = wekaDir.resolve(projName);
            MyFileUtils.createDirectory(projOutDir);

            // projInDir = "out/XXX/datasets"
            Path projInDir = Path.of("out", projName, "datasets");

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(projInDir)) {
                for (Path file : stream) {

                    if (file.toString().contains(".arff")) continue;

                    Path outFile = projOutDir.resolve(
                            file.getFileName().toString().replace(".csv", ".arff")
                    );

                    System.out.println(file.getFileName());
                    System.out.println(outFile.getFileName());


                    // load CSV
                    CSVLoader loader = new CSVLoader();
                    loader.setSource(file.toFile());
                    Instances data = loader.getDataSet();

                    // save ARFF
                    ArffSaver saver = new ArffSaver();
                    saver.setInstances(data);
                    saver.setFile(outFile.toFile());
                    saver.writeBatch();
                    // .arff file will be created in the output location

                }
            } catch (IOException | DirectoryIteratorException ex) {
                System.err.println("Error reading directory: " + ex.getMessage());
            }
        }


    }
}
