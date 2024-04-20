package it.gmarseglia.app.boundary;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Objects;

public class ProjectJSONGetter {

    private final String projName;
    private final String urlBase = "https://issues.apache.org/jira/rest/api/2/project/";

    public ProjectJSONGetter(String projName) {
        this.projName = projName;
    }

    public String getUrl(){
        return urlBase + this.projName;
    }

    public String getJSONFromResources () {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        String textJson;

        InputStream isJson = null;

        try {

            isJson = classloader.getResourceAsStream(this.projName + ".json");

            if (isJson == null) {
                try {
                    isJson = new URL(this.getUrl()).openStream();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            textJson = new String(isJson.readAllBytes(), Charset.defaultCharset());

            isJson.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                Objects.requireNonNull(isJson).close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /*
        try (InputStream isJson = classloader.getResourceAsStream(this.projName + ".json")) {
            assert isJson != null;
            try {
                textJson = new String(isJson.readAllBytes(), Charset.defaultCharset());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
         */

        return textJson;
    }

}
