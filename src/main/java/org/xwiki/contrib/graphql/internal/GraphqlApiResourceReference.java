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

import org.xwiki.resource.AbstractResourceReference;
import org.xwiki.resource.ResourceType;

/**
 * Resource reference for the GraphQL endpoint.
 *
 * @version $Id$
 */
public class GraphqlApiResourceReference extends AbstractResourceReference
{
    /**
     * Represents an GraphQL Resource Type.
     */
    public static final ResourceType TYPE = new ResourceType("graphql");

    private boolean displaySchema;

    public GraphqlApiResourceReference()
    {
        setType(TYPE);
    }

    /**
     * @return true if this is a schema request and not an actual query
     */
    public boolean isDisplaySchema()
    {
        return displaySchema;
    }

    public GraphqlApiResourceReference setDisplaySchema(boolean displaySchema)
    {
        this.displaySchema = displaySchema;
        return this;
    }
}
