package it.gmarseglia.app.controller;

import it.gmarseglia.app.model.Entry;
import it.gmarseglia.app.model.Version;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class EntriesController {

    private final List<Entry> allEntries = new ArrayList<>();

    public List<Entry> findAndAppendEntries(Path localPath, Version version) {
        List<Path> javaSrcPathsForVersion = MyFileUtils.getAllJavaSrcFiles(localPath);

        List<Entry> entriesForVersion = new ArrayList<>();

        for (Path p : javaSrcPathsForVersion) {
            entriesForVersion.add(new Entry(p.getFileName(), version));
        }

        this.allEntries.addAll(entriesForVersion);

        return entriesForVersion;
    }

    public List<Entry> getAllEntries () {
        return this.allEntries;
    }
}
