package it.gmarseglia.app.controller;

import com.google.gson.Gson;
import it.gmarseglia.app.boundary.ProjectJSONGetter;
import it.gmarseglia.app.model.Project;

public class ProjectController {

    private final String projName;
    private ProjectJSONGetter tjg;

    public ProjectController(String projName) {
        this.projName = projName;
        this.tjg = new ProjectJSONGetter(this.projName);
    }

    public String getUrl(){
        return tjg.getUrl();
    }

    public Project getProject(){
        Gson gson = new Gson();
        String textJson = tjg.getJSONFromResources();

        return gson.fromJson(textJson, Project.class);
    }


}
