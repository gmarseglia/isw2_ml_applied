package it.gmarseglia.weka.util;

import it.gmarseglia.app.controller.MyFileUtils;
import it.gmarseglia.app.controller.MyLogger;
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
        final String ARFF_EXT = ".arff";

        List<String> projects = Arrays.asList("BOOKKEEPER");

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

                    if (file.toString().contains(ARFF_EXT)) continue;

                    Path outFile = projOutDir.resolve(
                            file.getFileName().toString().replace(".csv", ARFF_EXT)
                    );

                    // load CSV
                    CSVLoader loader = new CSVLoader();
                    loader.setSource(file.toFile());
                    Instances data = loader.getDataSet();

                    // save ARFF
                    ArffSaver saver = new ArffSaver();
                    saver.setInstances(data);
                    saver.setFile(outFile.toFile());
                    saver.writeBatch();
                }
            }

            Path finalDatasetPath = projOutDir.resolve("final_dataset.arff");
            List<String> finalDatasetLines = Files.readAllLines(finalDatasetPath);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(projOutDir)) {
                for (Path file : stream) {
                    if (!file.toString().contains(".arff") || file.toString().contains("final_dataset") || Files.readAllLines(file).size() < 4) {
                        continue;
                    }

                    List<String> targetLines = Files.readAllLines(file);

                    targetLines.set(2, finalDatasetLines.get(2));
                    targetLines.set(3, finalDatasetLines.get(3));

                    Files.write(file, targetLines);
                }
            } catch (IOException | DirectoryIteratorException ex) {
                MyLogger.getInstance(CsvToArffConverter.class).log(ex.toString());
            }

        }
    }
}
