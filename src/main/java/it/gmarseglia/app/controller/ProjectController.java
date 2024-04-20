package it.gmarseglia.app.controller;

import com.google.gson.Gson;
import it.gmarseglia.app.boundary.ProjectJSONGetter;
import it.gmarseglia.app.model.Project;

public class ProjectController {

    private final String projName;
    private final ProjectJSONGetter projectJSONGetter;

    public ProjectController(String projName) {
        this.projName = projName;
        this.projectJSONGetter = new ProjectJSONGetter(this.projName);
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
