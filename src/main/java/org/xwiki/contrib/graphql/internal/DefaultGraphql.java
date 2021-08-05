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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.JsonObject;

import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.graphql.Graphql;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.execution.ExecutionService;
import io.smallrye.graphql.execution.SchemaPrinter;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;

/**
 * Default implementation for {@link Graphql}.
 *
 * @version $Id$
 * @since 0.1
 */
@Component
@Singleton
public class DefaultGraphql implements Graphql
{
    private static final String JANDEX_IDX = "META-INF/jandex.idx";

    @Inject
    private Logger logger;

    private class XWikiGraphqlConfig implements Config
    {
        // Use defaults.
    }

    private Config getConfiguration()
    {
        // Default configuration.
        return new XWikiGraphqlConfig();
    }

    private GraphQLSchema getSchema() throws IOException
    {
        // Read the classpath and index the classes providing domain and operations.
        IndexView index = buildIndex();

        // Read the indexed classes to build the smallrye schema.
        Schema schema = SchemaBuilder.build(index);

        // Build the actual GraphQLSchema.
        return Bootstrap.bootstrap(schema, getConfiguration());
    }

    private IndexView buildIndex() throws IOException
    {
        List<IndexView> indexes = new ArrayList<>();

        // Add pre-built jandex indexes available on the classpath.
        Enumeration<URL> urls = getClass().getClassLoader().getResources(JANDEX_IDX);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();

            logger.debug("Loading jandex index from [{}]", url);

            try (InputStream indexStream = url.openStream()) {
                IndexReader reader = new IndexReader(indexStream);
                IndexView index = reader.read();
                indexes.add(index);
            } catch (Exception e) {
                logger.warn("Failed to load jandex index from [{}]", url, e);
            }
        }

        // Merge all the indexes into one, exposed as a CompositeIndex.
        CompositeIndex result = CompositeIndex.create(indexes);
        return result;
    }

    @Override
    public String printSchema() throws IOException
    {
        SchemaPrinter schemaPrinter = new SchemaPrinter(getConfiguration());
        return schemaPrinter.print(getSchema());
    }

    @Override
    public JsonObject execute(JsonObject jsonInput) throws IOException
    {
        ExecutionService executionService = new ExecutionService(getConfiguration(), getSchema());

        return executionService.execute(jsonInput);
    }
}
