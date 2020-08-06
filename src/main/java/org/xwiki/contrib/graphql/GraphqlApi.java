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

import javax.inject.Singleton;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;

import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

/**
 * The entry-point for the operations provided by the XWiki GraphQL API.
 *
 * @version $Id$
 */
@GraphQLApi
@Component(roles = GraphqlApi.class)
@Singleton
public class GraphqlApi
{
    /**
     * @param documentReference the reference
     * @return the document
     */
    @Query
    @Description("Get a document by reference")
    public Document getDocument(@Name("documentReference") String documentReference) throws Exception
    {
        // Ugly hack but inject doesn't seem to work.
        DocumentAccessBridge documentAccessBridge = Utils.getComponent(DocumentAccessBridge.class);
        return new Document((XWikiDocument) documentAccessBridge.getDocument(documentReference), Utils.getContext());
    }
}
