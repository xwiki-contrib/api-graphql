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

import java.io.IOException;

import javax.json.JsonObject;

import org.xwiki.component.annotation.Role;

/**
 * Component in charge of initializing and executing GraphQL queries
 *
 * @version $Id$
 * @since 1.0
 */
@Role
public interface Graphql
{
    /**
     * @return the schema describing the GraphQL API
     */
    String printSchema() throws IOException;

    /**
     * @param jsonInput the json query
     * @return the json output
     * @throws IOException if case of problems
     */
    JsonObject execute(JsonObject jsonInput) throws IOException;
}
