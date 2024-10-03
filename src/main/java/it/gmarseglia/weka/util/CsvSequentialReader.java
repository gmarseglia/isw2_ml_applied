package it.gmarseglia.weka.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvSequentialReader {

    public static List<String> getFirstColumnEntries(Path csvFile) throws IOException {
        try (Stream<String> lines = Files.lines(csvFile).skip(1)) {
            return lines.map(line -> line.split(",")[0])
                    .collect(Collectors.toList());
        }
    }
}
