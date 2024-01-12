/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.tasksJsf;

import java.net.URI;
import java.util.Optional;

import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

/**
 * <p>
 * Basic operations for tasks owned by current user - additions, deletions/
 * </p>
 *
 * @author Lukas Fryc
 *
 */
@Path("/tasks")
@RequestScoped
public class TaskController {

    @Inject
    Template tasks;

    @Inject
    private TaskDao taskDao;

    @Inject
    private TaskList taskList;

    /**
     * Injects authentication, which is used to obtain the current user
     */
    @Inject
    private Authentication authentication;

    /**
     * Injects current task store
     */
    @Inject
    private CurrentTaskStore currentTaskStore;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response renderTasksPage() {
        // TODO: Duplicate logic from AuthController (!isLogged())
        // TODO: This unauth redirect call should move to a ReaderInterceptor
        if (authentication.getCurrentUser() == null) {
            Log.debug("Unauthenticated: null user");
            return unauthRedirect();
        }

        TemplateInstance template = tasks
                .data("currentUser", authentication.getCurrentUser())
                .data("currentTask", currentTaskStore.get())
                .data("taskList", taskList.getAll());

        return Response
                .ok(template.render())
                .build();
    }

    /**
     * Set the current task to the context
     *
     * @param task current task to be set to context
     */
    public void setCurrentTask(Task task) {
        currentTaskStore.set(task);
    }

    /**
     * Creates new task and, if no task is selected as current, selects it.
     *
     * @param taskTitle
     */
    @POST
    @Path("/addTask")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public Response createTask(@FormParam("taskTitle") String taskTitle) {
        taskList.invalidate();
        Task task = new Task(taskTitle);
        taskDao.createTask(authentication.getCurrentUser(), task);
        if (currentTaskStore.get() == null) {
            currentTaskStore.set(task);
        }

        return tasksRedirect();
    }

    /**
     * Creates new task and, if no task is selected as current, selects it.
     *
     * @param taskTitle
     */
    @POST
    @Path("/showTaskDetails")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public Response showTaskDetails(@FormParam("taskId") Long taskId) {
        Optional<Task> currentTask = taskList.getAll().stream()
                .filter(task -> task.getId().equals(taskId))
                .findFirst();

        currentTaskStore.set(currentTask.orElse(null));

        return tasksRedirect();
    }

    /**
     * Deletes given task
     *
     * @param task to delete
     */
    @POST
    @Path("/deleteTask")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public Response deleteTask(@FormParam("taskId") Long taskId) {
        Task currentTask = getTaskById(taskId);

        if (currentTask != null) {
            taskList.invalidate();
            if (currentTask.equals(currentTaskStore.get())) {
                currentTaskStore.unset();
            }
            taskDao.deleteTask(currentTask);
        } else {
            Log.warn("Task with id " + taskId + "is null!");
        }

        return tasksRedirect();
    }

    private Task getTaskById(Long id) {
        Optional<Task> currentTask = taskList.getAll().stream()
                .filter(task -> task.getId().equals(id))
                .findFirst();

        return currentTask.orElse(null);
    }

    private Response tasksRedirect() {
        return Response
                .seeOther(URI.create("/tasks"))
                .build();
    }

    private Response unauthRedirect() {
        // Redirect to root context (login page)
        URI tasksUri = UriBuilder
                .fromPath("/")
                .build();

        return Response
                .seeOther(tasksUri)
                .build();
    }
}
