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

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * <p>
 * Basic operations for tasks owned by current user - additions, deletions/
 * </p>
 *
 * @author Lukas Fryc
 *
 */
@RequestScoped
@Named
public class TaskController {

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
    public void createTask(String taskTitle) {
        taskList.invalidate();
        Task task = new Task(taskTitle);
        taskDao.createTask(authentication.getCurrentUser(), task);
        if (currentTaskStore.get() == null) {
            currentTaskStore.set(task);
        }
    }

    /**
     * Deletes given task
     *
     * @param task to delete
     */
    public void deleteTask(Task task) {
        taskList.invalidate();
        if (task.equals(currentTaskStore.get())) {
            currentTaskStore.unset();
        }
        taskDao.deleteTask(task);
    }

    /**
     * Deletes given task
     *
     * @param task to delete
     */
    public void deleteCurrentTask() {
        deleteTask(currentTaskStore.get());
    }
}
