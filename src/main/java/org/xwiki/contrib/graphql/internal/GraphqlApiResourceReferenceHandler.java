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
package org.xwiki.contrib.graphql.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.container.Container;
import org.xwiki.container.servlet.ServletRequest;
import org.xwiki.container.servlet.ServletResponse;
import org.xwiki.contrib.graphql.Graphql;
import org.xwiki.resource.AbstractResourceReferenceHandler;
import org.xwiki.resource.ResourceReference;
import org.xwiki.resource.ResourceReferenceHandlerChain;
import org.xwiki.resource.ResourceReferenceHandlerException;
import org.xwiki.resource.ResourceType;
import org.xwiki.resource.annotations.Authenticate;

/**
 * XWiki GraphQL endpoint that supports XWiki (basicauth or cookie) authentication. Most of this code inspired by
 * smallrye-graphql-servlet implementation.
 *
 * @version $Id$
 * @since 0.1
 */
@Singleton
@Component
@Named("graphql")
@Authenticate
public class GraphqlApiResourceReferenceHandler extends AbstractResourceReferenceHandler<ResourceType>
{
    private static final String APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8";

    private static final String QUERY = "query";

    private static final String VARIABLES = "variables";

    private static final JsonReaderFactory JSON_READER_FACTORY = Json.createReaderFactory(null);

    private static final JsonWriterFactory JSON_WRITER_FACTORY = Json.createWriterFactory(null);

    @Inject
    private Container container;

    @Inject
    private Graphql graphql;

    @Override
    public List<ResourceType> getSupportedResourceReferences()
    {
        return Collections.singletonList(GraphqlApiResourceReference.TYPE);
    }

    @Override
    public void handle(ResourceReference reference, ResourceReferenceHandlerChain chain)
        throws ResourceReferenceHandlerException
    {
        GraphqlApiResourceReference graphqlApiResourceReference = (GraphqlApiResourceReference) reference;
        HttpServletRequest request = ((ServletRequest) this.container.getRequest()).getHttpServletRequest();
        HttpServletResponse response = ((ServletResponse) this.container.getResponse()).getHttpServletResponse();

        try {
            if (graphqlApiResourceReference.isDisplaySchema()) {
                getSchema(request, response);
            } else {
                if (isGet(request)) {
                    handleGet(request, response);
                } else {
                    handlePost(request, response);
                }
            }
        } catch (Exception e) {
            throw new ResourceReferenceHandlerException("Error while handling GraphQL request.", e);
        }
    }

    protected void getSchema(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.setContentType("text/plain");
        try (PrintWriter out = response.getWriter()) {
            out.print(graphql.printSchema());
            out.flush();
        }
    }

    /**
     * Ensure that the request method is a GET.
     *
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

        if (StringUtils.isBlank(query)) {
            throw new IllegalArgumentException("Missing mandatory 'query' parameter");
        }

        JsonObjectBuilder input = Json.createObjectBuilder();
        input.add(QUERY, URLDecoder.decode(query, StandardCharsets.UTF_8.name()));
        if (StringUtils.isNotBlank(variables)) {
            JsonObject jsonObject = toJsonObject(URLDecoder.decode(variables, StandardCharsets.UTF_8.name()));
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
        try (JsonReader jsonReader = JSON_READER_FACTORY.createReader(inputReader)) {
            JsonObject jsonInput = jsonReader.readObject();
            handleInput(jsonInput, response);
        }
    }

    private void handleInput(JsonObject jsonInput, HttpServletResponse response) throws IOException
    {
        JsonObject outputJson = graphql.execute(jsonInput);
        if (outputJson != null) {
            ServletOutputStream out = response.getOutputStream();
            response.setContentType(APPLICATION_JSON_UTF8);

            try (JsonWriter jsonWriter = JSON_WRITER_FACTORY.createWriter(out)) {
                jsonWriter.writeObject(outputJson);
                out.flush();
            }
        }
    }

    private static JsonObject toJsonObject(String jsonString)
    {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonString))) {
            return jsonReader.readObject();
        }
    }
}
