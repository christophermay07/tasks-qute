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

import java.io.Serializable;

import jakarta.enterprise.context.SessionScoped;

/**
 * Store for current authenticated user
 *
 * NOTE: Since ConversationScoped is not an option, need another way to store
 * this information across multiple requests. Ideally, this would come from an
 * external source (e.g., JWT). For the purposes of the demo app, this will be
 * made SessionScoped, and the data will be nulled out where the conversation
 * previously ended.
 *
 * Use of SessionScoped requires the use of the quarkus-undertow extension.
 *
 * @author Lukas Fryc
 *
 */
@SuppressWarnings("serial")
// NOTE: ConversationScoped not available, so short-term fix: move to SessionScoped, and manually invalidate later.
@SessionScoped
public class Authentication implements Serializable {

    private User currentUser;

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        currentUser = user;
    }
}
