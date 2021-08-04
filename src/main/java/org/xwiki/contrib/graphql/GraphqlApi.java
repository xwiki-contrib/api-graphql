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

import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.query.QueryManager;
import org.xwiki.security.authorization.AuthorizationException;
import org.xwiki.security.authorization.Right;

import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.api.Object;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

/**
 * The entry-point for the operations provided by the XWiki GraphQL API.
 * <p>
 * The lifecycle of this class is handled by GraphQL (with reflections) and not XWiki's Component Manager.
 *
 * @version $Id$
 * @since 0.1
 */
@GraphQLApi
public class GraphqlApi
{
    private static final String USER_LACKS_RIGHTS_TEMPLATE = "Current user [%s] lacks [%s] right on [%s]";

    /**
     * @param documentReference the reference of the document to get
     * @return the document
     * @throws AuthorizationException if the current user is missing the needed rights
     * @throws Exception in case of problems
     */
    @Query
    @Description("Get a document by reference")
    public Document getDocument(@Name("documentReference") String documentReference)
        throws AuthorizationException, Exception
    {
        // Not nice, but we are forced to use static access if want to reach XWiki components.
        Document document = getDocumentCheckingRights(documentReference, Right.VIEW);

        if (document.isNew()) {
            return null;
        }

        return document;
    }

    private Document getDocumentCheckingRights(String documentReference, Right right)
        throws Exception, AuthorizationException
    {
        DocumentAccessBridge documentAccessBridge = Utils.getComponent(DocumentAccessBridge.class);
        Document document =
            new Document((XWikiDocument) documentAccessBridge.getDocument(documentReference), Utils.getContext());

        if (!document.hasAccessLevel(right.getName())) {
            throw new AuthorizationException(String.format(USER_LACKS_RIGHTS_TEMPLATE,
                documentAccessBridge.getCurrentUserReference(), right.getName(), documentReference));
        }
        return document;
    }

    /**
     * @param language the language of the query
     * @param statement the statement of the query
     * @param wikiId the id of the wiki where to execute the query
     * @param offset start offset
     * @param limit the number of results
     * @return the list of documents selected by the given query
     * @throws Exception in case of problems
     */
    @Query
    @Description("Get documents by query")
    public List<Document> getDocuments(@DefaultValue(org.xwiki.query.Query.XWQL) String language,
        @DefaultValue("") String statement, @DefaultValue("xwiki") String wikiId, @DefaultValue("0") int offset,
        @DefaultValue("10") int limit) throws Exception
    {
        List<Document> result = new ArrayList<>();

        QueryManager queryManager = Utils.getComponent(QueryManager.class);
        List<String> fullNames =
            queryManager.createQuery(statement, language).setWiki(wikiId).setOffset(offset).setLimit(limit).execute();
        for (String documentFullName : fullNames) {

            Document document = null;
            try {
                document = getDocumentCheckingRights(documentFullName, Right.VIEW);
            } catch (AuthorizationException e) {
                continue;
            }

            result.add(document);
        }

        return result;
    }

    /**
     * @param documentReference the reference of the document to create or update
     * @param title the new title of the document
     * @param content the new content
     * @param comment the comment that will describe this change
     * @param isMinor if the change is a minor one
     * @return the created or updated document, after it was saved
     * @throws AuthorizationException if the current user is missing the needed rights
     * @throws Exception in case of problems
     */
    @Mutation
    @Description("Create or update a document by reference")
    public Document createOrUpdateDocument(@NonNull String documentReference, String title, String content,
        String comment, @DefaultValue("false") boolean isMinor) throws AuthorizationException, Exception
    {
        Document document = getDocumentCheckingRights(documentReference, Right.EDIT);

        document.setTitle(title);
        document.setContent(content);
        document.setComment(comment);
        document.setMinorEdit(isMinor);

        document.save(comment);

        return document;
    }

    /**
     * @param documentReference the reference of the document to delete
     * @return the deleted document, after it was deleted from storage
     * @throws Exception in case of problems
     */
    @Mutation
    @Description("Delete a document by reference")
    public Document deleteDocument(String documentReference) throws Exception
    {
        Document document = getDocumentCheckingRights(documentReference, Right.DELETE);

        if (document.isNew()) {
            return null;
        }

        document.delete();

        return document;
    }

    /**
     * @param document the document for which to retrieve objects
     * @return the list of objects of the document
     */
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
