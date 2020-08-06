/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.graphql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReaderFactory;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xwiki.component.annotation.Component;
import org.xwiki.container.Container;
import org.xwiki.container.servlet.ServletRequest;
import org.xwiki.container.servlet.ServletResponse;
import org.xwiki.resource.AbstractResourceReferenceHandler;
import org.xwiki.resource.ResourceReference;
import org.xwiki.resource.ResourceReferenceHandlerChain;
import org.xwiki.resource.ResourceReferenceHandlerException;
import org.xwiki.resource.ResourceType;

import javax.json.JsonObject;
import javax.json.JsonReader;

import io.smallrye.graphql.execution.ExecutionService;

/**
 * XWiki GraphQL endpoint.
 * Most of this code inspired by smallrye-graphql-servlet implementation.
 *
 * @version $Id$
 * @since 1.0
 */
@Singleton
@Component
@Named("graphql")
public class GraphqlApiResourceReferenceHandler extends AbstractResourceReferenceHandler<ResourceType>
{
    private static final ResourceType TYPE = new ResourceType("graphql");
    private static final String APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8";

    private static final String QUERY = "query";
    private static final String VARIABLES = "variables";
    private static final JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(null);
    private static final JsonWriterFactory jsonWriterFactory = Json.createWriterFactory(null);

    @Inject
    private Container container;

    @Inject
    ExecutionService executionService;

    @Override
    public List<ResourceType> getSupportedResourceReferences()
    {
        return Collections.singletonList(TYPE);
    }

    @Override
    public void handle(ResourceReference reference, ResourceReferenceHandlerChain chain)
        throws ResourceReferenceHandlerException
    {
        HttpServletRequest request = ((ServletRequest) this.container.getRequest()).getHttpServletRequest();
        HttpServletResponse response = ((ServletResponse) this.container.getResponse()).getHttpServletResponse();

        try {
            if (isGet(request)) {
                handleGet(request, response);
            } else {
                handlePost(request, response);
            }
        } catch (Exception e) {
            throw new ResourceReferenceHandlerException("Error while handling GraphQL request.", e);
        }
    }

    /**
     * Ensure that the request method is a GET.
     * @param request the request to test
     * @return {@code true} iff the method of the request is GET.
     */
    private boolean isGet(HttpServletRequest request)
    {
        return "get".equalsIgnoreCase(request.getMethod());
    }

    private void handleGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String query = request.getParameter(QUERY);
        String variables = request.getParameter(VARIABLES);

        JsonObjectBuilder input = Json.createObjectBuilder();
        input.add(QUERY, URLDecoder.decode(query, "UTF8"));
        if (variables != null && !variables.isEmpty()) {
            JsonObject jsonObject = toJsonObject(URLDecoder.decode(variables, "UTF8"));
            input.add(VARIABLES, jsonObject);
        }
        handleInput(input.build(), response);
    }

    private void handlePost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        try (BufferedReader reader = request.getReader()) {
            handleInput(reader, response);
        }
    }

    private void handleInput(Reader inputReader, HttpServletResponse response) throws IOException
    {
        try (JsonReader jsonReader = jsonReaderFactory.createReader(inputReader)) {
            JsonObject jsonInput = jsonReader.readObject();
            handleInput(jsonInput, response);
        }
    }

    private void handleInput(JsonObject jsonInput, HttpServletResponse response) throws IOException {
        JsonObject outputJson = executionService.execute(jsonInput);
        if (outputJson != null) {
            ServletOutputStream out = response.getOutputStream();
            response.setContentType(APPLICATION_JSON_UTF8);

            try (JsonWriter jsonWriter = jsonWriterFactory.createWriter(out)) {
                jsonWriter.writeObject(outputJson);
                out.flush();
            }
        }
    }

    private static JsonObject toJsonObject(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonString))) {
            return jsonReader.readObject();
        }
    }
}
