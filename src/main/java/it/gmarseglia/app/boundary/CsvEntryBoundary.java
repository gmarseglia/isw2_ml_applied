package it.gmarseglia.app.boundary;

import it.gmarseglia.app.controller.MyFileUtils;
import it.gmarseglia.app.entity.Entry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.file.StandardOpenOption.*;

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
            Files.writeString(outFile,
                    "version,name,isBuggy" + System.lineSeparator(),
                    CREATE, APPEND);

            for (Entry entry : entries) {
                Files.writeString(outFile,
                        entry.toCsvLine() + System.lineSeparator(),
                        APPEND);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
