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
import java.util.List;

public class CsvToArffConverter {

    private CsvToArffConverter() { }

    private static void writeLines(Path file, List<String> finalDatasetLines) throws IOException {
        if (!file.toString().contains(".arff") || file.toString().contains("final_dataset") || Files.readAllLines(file).size() < 4) {
            return;
        }

        List<String> targetLines = Files.readAllLines(file);

        targetLines.set(2, finalDatasetLines.get(2));
        targetLines.set(3, finalDatasetLines.get(3));
        if(targetLines.size() >= 18) {
            targetLines.set(18, "@attribute Buggy {true,false}");
        }

        Files.write(file, targetLines);
    }

    public static void convertProjects(List<String> projects) throws IOException {

        // Create WEKA output directory
        MyFileUtils.createDirectory(Configs.WEKA_DIR);

        for (String projName : projects) {
            // projOutDir = "out/WEKA/XXX"
            Path projOutDir = Configs.getProjWekaOutDir(projName);
            MyFileUtils.createDirectory(projOutDir);

            // projInDir = "out/XXX/datasets"
            Path projInDir = Configs.getProjRawDatasetDir(projName);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(projInDir)) {
                for (Path file : stream) {

                    if (file.toString().contains(Configs.ARFF_EXT)) continue;

                    Path outFile = projOutDir.resolve(
                            file.getFileName().toString().replace(".csv", Configs.ARFF_EXT)
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

            // Align the header of all datasets
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(projOutDir)) {
                for (Path file : stream) {
                    writeLines(file, finalDatasetLines);
                }
            } catch (IOException | DirectoryIteratorException ex) {
                MyLogger.getInstance(CsvToArffConverter.class).log(ex.toString());
            }

        }
    }
}
