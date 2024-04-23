package it.gmarseglia.app.controller;

import com.google.gson.Gson;
import it.gmarseglia.app.boundary.ProjectJSONGetter;
import it.gmarseglia.app.entity.Project;

import java.util.HashMap;
import java.util.Map;

public class ProjectController {

    private static final Map<String, ProjectController> instances = new HashMap<>();

    private final String projName;
    private final ProjectJSONGetter projectJSONGetter;

    private ProjectController(String projName) {
        this.projName = projName;
        this.projectJSONGetter = new ProjectJSONGetter(this.projName);
    }

    public static ProjectController getInstance(String projName){
        ProjectController.instances.computeIfAbsent(projName, string -> new ProjectController(projName));
        return ProjectController.instances.get(projName);
    }


    /**
     * Uses {@code Gson} to map Json to {@link Project}
     *
     * @return {@code Project} from Json of Jira REST API.
     */
    public Project getProject() {
        Gson gson = new Gson();
        String textJson = projectJSONGetter.getJSONFromResources();

        return gson.fromJson(textJson, Project.class);
    }


}
