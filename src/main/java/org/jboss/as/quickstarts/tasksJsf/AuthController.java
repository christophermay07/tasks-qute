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
import java.util.ArrayList;
import java.util.List;

import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Response.Status;

/**
 * Provides authentication operations with current user store: {@link Authentication}.
 *
 * @author Lukas Fryc
 *
 */
@Path("/")
@RequestScoped
public class AuthController {

    @Inject
    Template index;

    @Inject
    private Authentication authentication;

    @Inject
    private UserDao userDao;

    // NOTE: Temporary workaround for loss of ConversationScoped; lazy init
    @Inject
    Instance<CurrentTaskStore> taskStore;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response renderLoginPage() {
        if (isLogged()) {
            // Redirect to /tasks
            URI tasksUri = UriBuilder.fromPath("/tasks").build();
            return Response
                    .seeOther(tasksUri)
                    .build();
        }

        TemplateInstance template = index
                .data("currentUser", authentication.getCurrentUser());

        return Response
                .ok(template.render())
                .build();
    }

    /**
     * <p>
     * Authenticates current user with 'username' against user data store
     * </p>
     *
     * @param username the username of the user to authenticate
     */
    @POST
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public Response authenticate(String username) {
        if (isLogged()) {
            throw new IllegalStateException("User is logged and tries to authenticate again");
        }

        List<String> messages = new ArrayList<>();

        User user = userDao.getForUsername(username);
        if (user == null) {
            // TODO: Need a more complete refactor to replicate the old FacesMessage functionality
            // NOTE: Removing "success" message since that would never be seen in any code flow
            try {
                user = createUser(username);
            } catch (Exception e) {
                Log.error(e);

                messages.add("Failed to create user '" + username + "'");

                TemplateInstance template = index
                        .data("username", authentication.getCurrentUser())
                        .data("messages", messages);

                // TODO: Failure response
                return Response
                        .status(Status.UNAUTHORIZED)
                        .entity(template.render())
                        .build();
            }
        }

        authentication.setCurrentUser(user);

        // TODO: Is there a better way to redirect on POST, given the current paradigm?
        // Redirect to /tasks
        return Response
                .seeOther(URI.create("/tasks"))
                .build();
    }

    /**
     * Logs current user out and clears associated cached session data
     * (workaround for loss of ConversationScoped)
     */
    @Path("/logout")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response logout() {
        authentication.setCurrentUser(null);
        taskStore.get().unset();

        return Response
                .seeOther(URI.create("/"))
                .build();
    }

    /**
     * Returns true if user is logged in
     *
     * @return true if user is logged in; false otherwise
     */
    public boolean isLogged() {
        return authentication.getCurrentUser() != null;
    }

    private User createUser(String username) {
        User user = new User(username);
        userDao.createUser(user);
        return user;
    }
}
