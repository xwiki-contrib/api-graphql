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

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.graphql.Name;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.ApplicationStartedEvent;
import org.xwiki.observation.event.Event;

/**
 * @version $Id$
 */
@Component
@Name("org.xwiki.contrib.graphql.GraphqlApiInitializer")
@Singleton
public class GraphqlApiInitializer extends AbstractEventListener
{
    private static final String NAME = "org.xwiki.contrib.graphql.GraphqlApiInitializer";

    private static final List<Event> EVENTS = Arrays.asList(new ApplicationStartedEvent());

    @Inject
    private Logger logger;


    /**
     * Default Constructor
     */
    public GraphqlApiInitializer()
    {
        super(NAME, EVENTS);
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        logger.info("Application initialized");

        // Scan the classpath and build the GraphQL Schema
        // Build the GraphQL object that is used to run queries

//        Set<URL> urls = getUrlFromClassPath();
//        return createIndexView(urls);




    }

}
