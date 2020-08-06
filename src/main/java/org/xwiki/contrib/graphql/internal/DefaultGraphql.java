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

import javax.inject.Singleton;
import javax.json.JsonObject;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
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
 */
@Component
@Singleton
public class DefaultGraphql implements Graphql
{
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
        Index index = buildIndex();

        // Read the indexed classes to build the smallrye schema.
        Schema schema = SchemaBuilder.build(index);

        // Build the actual GraphQLSchema.
        return Bootstrap.bootstrap(schema, getConfiguration());
    }

    private Index buildIndex() throws IOException
    {
        Indexer indexer = new Indexer();

        // FIXME: Come up with a way to automatically discover from the classpath the domain that is used to build the
        // schema. For now, we are adding them by hand.

        InputStream stream =
            getClass().getClassLoader().getResourceAsStream("org/xwiki/contrib/graphql/GraphqlApi.class");
        indexer.index(stream);

        stream = getClass().getClassLoader().getResourceAsStream("com/xpn/xwiki/api/Document.class");
        indexer.index(stream);

        return indexer.complete();
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
