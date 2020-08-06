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

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.JsonObject;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.graphql.Graphql;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.execution.ExecutionService;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;

/**
 * @version $Id$
 */
@Component
@Singleton
public class DefaultGraphql implements Graphql
{

    @Inject
    private Logger logger;

    @Override
    public GraphQLSchema getSchema()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JsonObject execute(JsonObject jsonInput) throws IOException
    {
        Config config = new Config()
        {
        };

        // Classes in this artifact
        Indexer indexer = new Indexer();
        InputStream stream =
            getClass().getClassLoader().getResourceAsStream("org/xwiki/contrib/graphql/GraphqlApi.class");
        indexer.index(stream);
        Index index = indexer.complete();

        Schema schema = SchemaBuilder.build(index); // Get the smallrye schema

        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(schema, config);
        // if (config.isMetricsEnabled()) {
        // MetricRegistry vendorRegistry = MetricsService.load().getMetricRegistry(MetricRegistry.Type.VENDOR);
        // Bootstrap.registerMetrics(schema, vendorRegistry);
        // }
        ExecutionService executionService = new ExecutionService(config, graphQLSchema);

        return executionService.execute(jsonInput);
    }
}
