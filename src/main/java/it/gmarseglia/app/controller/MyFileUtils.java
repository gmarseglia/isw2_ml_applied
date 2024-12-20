package it.gmarseglia.app.controller;

import it.gmarseglia.app.exceptions.CustomRuntimeException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class MyFileUtils {

    private MyFileUtils(){}

    private static final MyLogger logger = MyLogger.getInstance(MyFileUtils.class);

    public static void deleteDirectory(Path path) {
        try {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            logger.logFine(String.format("%s does not exists.", path));
        }
    }

    public static boolean isJavaSrcFile(Path p){
        String srcString = "src" + File.separator + "main";
        return p.getFileName().toString().endsWith(".java") && p.toString().contains(srcString);
    }

    public static List<Path> getAllJavaSrcFiles(Path localPath) {
        List<Path> result;

        try (Stream<Path> pathStream = Files.find(localPath,
                Integer.MAX_VALUE,
                (p, basicFileAttributes) -> isJavaSrcFile(p))
        ) {
            result = pathStream.toList();
        } catch (IOException e) {
            throw new CustomRuntimeException(e);
        }

        return result;
    }

    public static void createDirectory(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new CustomRuntimeException(e);
        }
    }

    public static void deleteFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new CustomRuntimeException(e);
        }
    }

    public static Path createFile(Path path) {
        try {
            Files.createFile(path);
        } catch (IOException e) {
            throw new CustomRuntimeException(e);
        }
        return path;
    }

}
