package it.gmarseglia.app.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

public class ConfigsController {

    private static final MyLogger logger = MyLogger.getInstance(ConfigsController.class);
    private static final Path PROPERTIES_PATH = Paths.get(".", "configs").resolve("metrics.properties");
    private static Properties properties = null;

    private ConfigsController() {

    }

    private static void initialize() throws IOException {
        if (properties == null) {
            properties = new Properties();
            try (FileInputStream fileInputStream = new FileInputStream(PROPERTIES_PATH.toString())) {
                properties.load(fileInputStream);
            }
        }
    }

    public static boolean getPropertyUseStepMetrics() {
        String key = "useStepMetrics";
        String defaultValue = "false";
        String result;
        try {
            initialize();
            result = properties.getProperty(key, defaultValue);
            logger.logFinest(String.format("Property \"%s\" found, using value: %s", key, result));
        } catch (IOException e) {
            logger.logFinest(String.format("Property \"%s\" not found, using default value: %s", key, defaultValue));
            result = defaultValue;
        }
        return Objects.equals(result, "true");
    }

}
