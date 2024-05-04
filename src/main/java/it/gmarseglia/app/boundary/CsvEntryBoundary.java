package it.gmarseglia.app.boundary;

import it.gmarseglia.app.controller.MyFileUtils;
import it.gmarseglia.app.entity.Entry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class CsvEntryBoundary {

    private final String projName;
    private final Path outDirPath = Paths.get(".", "out");

    public CsvEntryBoundary(String projName) {
        this.projName = projName;
    }

    public void writeEntries(List<Entry> entries) {
        MyFileUtils.createDirectory(outDirPath);
        Path outFile = outDirPath.resolve(this.projName + "_dataset.csv");
        MyFileUtils.deleteFile(outFile);

        try {
            String firstRow = String.join(",", Entry.getFieldsNames()).concat(System.lineSeparator());
            Files.writeString(outFile, firstRow, CREATE, APPEND);

            for (Entry entry : entries) {
                List<String> entryValues = entry.getFieldsValues().stream().map(Object::toString).toList();
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
