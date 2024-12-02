package it.gmarseglia.app.boundary;

import it.gmarseglia.app.controller.MyFileUtils;
import it.gmarseglia.app.controller.MyLogger;
import it.gmarseglia.app.entity.Exportable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardOpenOption.*;

public class ToFileBoundary {

    private static final MyLogger logger = MyLogger.getInstance(ToFileBoundary.class);
    public static final Path DEFAULT_OUT_DIR = Paths.get(".", "out");
    private static final List<String> alreadyUsedFilenames = new ArrayList<>();

    private ToFileBoundary(){

    }

    public static void writeListProj(List<? extends Exportable> elements, String projName, String fileName) {
        Path outDir = DEFAULT_OUT_DIR.resolve(projName);
        writeList(elements, outDir, fileName);
    }

    public static void writeStringProj(String text, String projName, String fileName) {
        try {
            Path outDir = DEFAULT_OUT_DIR.resolve(projName);
            Path outFile = outDir.resolve(fileName);
            String outText = text.concat(System.lineSeparator());

            if (!alreadyUsedFilenames.contains(outFile.toString())) {
                MyFileUtils.createDirectory(outDir);
                alreadyUsedFilenames.add(outFile.toString());
                Files.writeString(outFile, outText, CREATE, TRUNCATE_EXISTING);
            } else {
                Files.writeString(outFile, outText, APPEND);
            }
        } catch (IOException e) {
            logger.log(String.format("Can't write \"%s\" to: \"%s\"", text, fileName));
        }
    }

    public static void writeList(List<? extends Exportable> elements, String fileName) {
        writeList(elements, DEFAULT_OUT_DIR, fileName);
    }

    public static void writeList(List<? extends Exportable> elements, Path outDirPath, String fileName) {
        try {
            MyFileUtils.createDirectory(outDirPath);
            Path outFile = outDirPath.resolve(fileName);

            String firstRow;

            if (elements.isEmpty()) {
                firstRow = "Got empty list of " + elements.getClass().getSimpleName() + ".";
            } else {
                firstRow = String.join(",", elements.getFirst().getFieldsNames()).concat(System.lineSeparator());
            }
            Files.writeString(outFile, firstRow, CREATE, TRUNCATE_EXISTING);

            for (Exportable element : elements) {
                List<String> entryValues = element.getFieldsValues()
                        .stream()
                        .map(serializable -> (serializable == null ? "null" : serializable.toString()))
                        .toList();
                String entryRow = String.join(",", entryValues).concat(System.lineSeparator());

                Files.writeString(outFile,
                        entryRow,
                        APPEND);
            }
        } catch (IOException e) {
            logger.log(String.format("Can't write \"%s\" to: \"%s\"", elements.getClass().getSimpleName(), fileName));
        }
    }

}
