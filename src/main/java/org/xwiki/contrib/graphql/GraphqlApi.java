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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.api.Object;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

/**
 * The entry-point for the operations provided by the XWiki GraphQL API.
 *
 * @version $Id$
 * @since 1.0
 */
@GraphQLApi
@Component(roles = GraphqlApi.class)
@Singleton
public class GraphqlApi
{
    @Query
    @Description("Get a document by reference")
    public Document getDocument(@Name("documentReference") String documentReference) throws Exception
    {
        // Ugly hack but @Inject doesn't seem to work to access XWiki components.
        DocumentAccessBridge documentAccessBridge = Utils.getComponent(DocumentAccessBridge.class);
        Document document =
            new Document((XWikiDocument) documentAccessBridge.getDocument(documentReference), Utils.getContext());

        if (document.isNew()) {
            return null;
        }

        if (!document.hasAccessLevel("view")) {
            throw new Exception(String.format("Current user [%s] lacks [%s] right on [%s]",
                documentAccessBridge.getCurrentUserReference(), "view", documentReference));
        }

        return document;
    }

    @Query
    @Description("Get documents by query")
    public List<Document> getDocuments(@DefaultValue(org.xwiki.query.Query.XWQL) String language,
        @DefaultValue("") String statement, @DefaultValue("xwiki") String wikiId, @DefaultValue("0") int offset,
        @DefaultValue("10") int limit) throws Exception
    {
        DocumentAccessBridge documentAccessBridge = Utils.getComponent(DocumentAccessBridge.class);

        List<Document> result = new ArrayList<>();

        QueryManager queryManager = Utils.getComponent(QueryManager.class);
        List<String> fullNames =
            queryManager.createQuery(statement, language).setWiki(wikiId).setOffset(offset).setLimit(limit).execute();
        for (String documentFullName : fullNames) {

            Document document =
                new Document((XWikiDocument) documentAccessBridge.getDocument(documentFullName), Utils.getContext());

            if (!document.hasAccessLevel("view")) {
                continue;
            }

            result.add(document);
        }

        return result;
    }

    @Mutation
    @Description("Create or update a document by reference")
    public Document createOrUpdateDocument(@NonNull String documentReference, String title, String content,
        String comment, @DefaultValue("false") boolean isMinor) throws Exception
    {
        DocumentAccessBridge documentAccessBridge = Utils.getComponent(DocumentAccessBridge.class);
        Document document =
            new Document((XWikiDocument) documentAccessBridge.getDocument(documentReference), Utils.getContext());

        if (!document.hasAccessLevel("edit")) {
            throw new Exception(String.format("Current user [%s] lacks [%s] right on [%s]",
                documentAccessBridge.getCurrentUserReference(), "edit", documentReference));
        }

        document.setTitle(title);
        document.setContent(content);
        document.setComment(comment);
        document.setMinorEdit(isMinor);

        document.save(comment);

        return document;
    }

    @Mutation
    @Description("Delete a document by reference")
    public Document deleteDocument(String documentReference) throws Exception
    {
        DocumentAccessBridge documentAccessBridge = Utils.getComponent(DocumentAccessBridge.class);
        Document document =
            new Document((XWikiDocument) documentAccessBridge.getDocument(documentReference), Utils.getContext());

        if (document.isNew()) {
            return null;
        }

        document.delete();

        return document;
    }

    @Named("objects")
    public List<Object> getObjects(@Source Document document)
    {
        List<Object> result = new ArrayList<>();
        for (Vector<Object> objects : document.getxWikiObjects().values()) {
            result.addAll(objects);
        }

        result.sort((Object o1, Object o2) -> o1.getxWikiClass().getDocumentReference()
            .compareTo(o2.getxWikiClass().getDocumentReference()));

        return result;
    }
}
