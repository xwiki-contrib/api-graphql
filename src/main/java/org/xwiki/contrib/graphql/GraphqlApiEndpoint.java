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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.graphql.model.Document;

import com.xpn.xwiki.XWikiContext;

/**
 * The entry-point for the GraphQL API.
 *
 * @version $Id$
 */
@GraphQLApi
@Component(roles = GraphqlApiEndpoint.class)
@Singleton
public class GraphqlApiEndpoint implements Initializable
{
    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private Logger logger;

    /**
     * @param documentReference the reference
     * @return the document
     */
    @Query
    @Description("Get a document by reference")
    public Document getDocument(@Name("documentReference") String documentReference)
    {
        // XWikiContext context = contextProvider.get();
        // return personDB.getPerson(personId);
        return new Document(documentReference);
    }

    @Override
    public void initialize() throws InitializationException
    {
        logger.info("{} initialized", this.getClass());
    }
}
