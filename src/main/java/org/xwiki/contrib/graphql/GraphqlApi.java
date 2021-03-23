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
import org.xwiki.security.authorization.Right;

import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.api.Object;
import com.xpn.xwiki.api.Property;
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
     * @throws Exception in case of problems
     */
    @Query
    @Description("Get a document by reference")
    public Document getDocument(@Name("documentReference") String documentReference) throws Exception
    {
        // Not nice, but we are forced to use static access if want to reach XWiki components.
        DocumentAccessBridge documentAccessBridge = Utils.getComponent(DocumentAccessBridge.class);
        Document document =
            new Document((XWikiDocument) documentAccessBridge.getDocument(documentReference), Utils.getContext());

        if (document.isNew()) {
            return null;
        }

        if (!document.hasAccessLevel(Right.VIEW.getName())) {
            throw new Exception(String.format(USER_LACKS_RIGHTS_TEMPLATE,
                documentAccessBridge.getCurrentUserReference(), Right.VIEW.getName(), documentReference));
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

    /**
     * @param documentReference the reference of the document to create or update
     * @param title the new title of the document
     * @param content the new content
     * @param comment the comment that will describe this change
     * @param isMinor if the change is a minor one
     * @return the created or updated document, after it was saved
     * @throws Exception in case of problems
     */
    @Mutation
    @Description("Create or update a document by reference")
    public Document createOrUpdateDocument(@NonNull String documentReference, String title, String content,
        String comment, @DefaultValue("false") boolean isMinor) throws Exception
    {
        DocumentAccessBridge documentAccessBridge = Utils.getComponent(DocumentAccessBridge.class);
        Document document =
            new Document((XWikiDocument) documentAccessBridge.getDocument(documentReference), Utils.getContext());

        if (!document.hasAccessLevel(Right.EDIT.getName())) {
            throw new Exception(String.format(USER_LACKS_RIGHTS_TEMPLATE,
                documentAccessBridge.getCurrentUserReference(), Right.EDIT.getName(), documentReference));
        }

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
        DocumentAccessBridge documentAccessBridge = Utils.getComponent(DocumentAccessBridge.class);
        Document document =
            new Document((XWikiDocument) documentAccessBridge.getDocument(documentReference), Utils.getContext());

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

    /**
     * @param object the object for which to retrieve the properties
     * @return the list of properties of the object
     */
    @Named("properties")
    public List<Property> getProperties(@Source Object object)
    {
        List<Property> result = new ArrayList<>();
        for (java.lang.Object propertyName : object.getPropertyNames()) {
            if (propertyName instanceof String) {
                result.add(object.getProperty((String) propertyName));
            }
        }
        return result;
    }

    /**
     * @param object the object from which the class should be taken
     * @return the corresponding XWiki class
     */
    @Named("class")
    public com.xpn.xwiki.api.Class getXWikiClass(@Source Object object)
    {
        return object.getxWikiClass();
    }
}
