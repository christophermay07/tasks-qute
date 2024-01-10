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

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class TaskListBeanIT {

    public static final String WEBAPP_SRC = "src/main/webapp";

    @Inject
    private TaskDao taskDaoStub;

    @Inject
    private TaskList taskList;

    @Test
    public void dao_method_getAll_should_be_called_only_once_on() {
        taskList.getAll();
        taskList.getAll();
        taskList.getAll();
        assertEquals(1, ((TaskDaoStub) taskDaoStub).getGetAllCallsCount());
    }

    @Test
    public void dao_method_getAll_should_be_called_after_invalidation() {
        taskList.getAll();
        taskList.getAll();
        assertEquals(1, ((TaskDaoStub) taskDaoStub).getGetAllCallsCount());
        taskList.invalidate();
        assertEquals(1, ((TaskDaoStub) taskDaoStub).getGetAllCallsCount());
        taskList.getAll();
        taskList.getAll();
        assertEquals(2, ((TaskDaoStub) taskDaoStub).getGetAllCallsCount());
    }
}
