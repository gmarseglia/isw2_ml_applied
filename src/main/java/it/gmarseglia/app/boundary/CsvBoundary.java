package it.gmarseglia.app.boundary;

import it.gmarseglia.app.controller.MyFileUtils;
import it.gmarseglia.app.entity.Exportable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.file.StandardOpenOption.*;

public class CsvBoundary {

    public final static Path DEFAULT_OUT_DIR = Paths.get(".", "out");

    public static void writeList(List<? extends Exportable> elements, Path outDirPath, String fileName) {
        MyFileUtils.createDirectory(outDirPath);
        Path outFile = outDirPath.resolve(fileName);
        MyFileUtils.deleteFile(outFile);

        try {
            String firstRow = String.join(",", elements.getFirst().getFieldsNames()).concat(System.lineSeparator());
            Files.writeString(outFile, firstRow, CREATE, WRITE);

            for (Exportable element : elements) {
                List<String> entryValues = element.getFieldsValues().stream().map(Object::toString).toList();
                String entryRow = String.join(",", entryValues).concat(System.lineSeparator());

                Files.writeString(outFile,
                        entryRow,
                        APPEND);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
