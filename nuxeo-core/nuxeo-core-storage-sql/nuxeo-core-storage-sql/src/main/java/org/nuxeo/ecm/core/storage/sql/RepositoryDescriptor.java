/*
 * (C) Copyright 2007-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Florent Guillaume
 */

package org.nuxeo.ecm.core.storage.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XNodeMap;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * Repository descriptor.
 *
 * @author Florent Guillaume
 */
@XObject(value = "repository")
public class RepositoryDescriptor {

    private static final Log log = LogFactory.getLog(RepositoryDescriptor.class);

    @XObject(value = "index")
    public static class FulltextIndexDescriptor {
        @XNode("@name")
        public String name;

        @XNode("@analyzer")
        public String analyzer;

        @XNode("@catalog")
        public String catalog;

        /** string or blob */
        @XNode("fieldType")
        public String fieldType;

        @XNodeList(value = "field", type = HashSet.class, componentType = String.class)
        public Set<String> fields;

        @XNodeList(value = "excludeField", type = HashSet.class, componentType = String.class)
        public Set<String> excludeFields;
    }

    @XObject(value = "field")
    public static class FieldDescriptor {
        @XNode("@type")
        public String type;

        @XNode
        public String field;
    }

    @XNode("@name")
    public String name;

    @XNode("clustering@enabled")
    public boolean clusteringEnabled;

    @XNode("clustering@delay")
    public long clusteringDelay;

    @XNodeList(value = "schema/field", type = ArrayList.class, componentType = FieldDescriptor.class)
    public List<FieldDescriptor> schemaFields = Collections.emptyList();

    @XNode("indexing/fulltext@analyzer")
    public String fulltextAnalyzer;

    @XNode("indexing/fulltext@catalog")
    public String fulltextCatalog;

    @XNodeList(value = "indexing/queryMaker@class", type = ArrayList.class, componentType = Class.class)
    public List<Class<?>> queryMakerClasses;

    @XNodeList(value = "indexing/fulltext/index", type = ArrayList.class, componentType = FulltextIndexDescriptor.class)
    public List<FulltextIndexDescriptor> fulltextIndexes;

    /** Merges only non-JCA properties. */
    public void mergeFrom(RepositoryDescriptor other) {
        clusteringEnabled = other.clusteringEnabled;
        clusteringDelay = other.clusteringDelay;
        schemaFields = other.schemaFields;
        fulltextAnalyzer = other.fulltextAnalyzer;
        fulltextCatalog = other.fulltextCatalog;
        queryMakerClasses = other.queryMakerClasses;
        fulltextIndexes = other.fulltextIndexes;
    }

    @XNode("xa-datasource")
    public String xaDataSourceName;

    @XNodeMap(value = "property", key = "@name", type = HashMap.class, componentType = String.class)
    public Map<String, String> properties;

    /** The possible id generation policies. */
    public enum IdGenPolicy {

        /**
         * Let the Nuxeo application generate a random UUID.
         */
        APP_UUID("application-uuid"),

        /**
         * Let the database generate its own integer using sequences or
         * identity.
         */
        DB_IDENTITY("database-identity");

        private final String value;

        IdGenPolicy(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static IdGenPolicy fromString(String value) {
            for (IdGenPolicy e : values()) {
                if (e.value.equals(value)) {
                    return e;
                }
            }
            throw new IllegalArgumentException(value);
        }
    }

    /**
     * Which id generation policy to use.
     * <p>
     * The default is {@link IdGenPolicy#APP_UUID}.
     */
    @XNode("id-generation")
    public void setIdGeneration(String value) {
        try {
            idGenPolicy = IdGenPolicy.fromString(value);
        } catch (IllegalArgumentException e) {
            log.error("Illegal id generation policy: " + value
                    + ", using default: " + idGenPolicy.getValue());
        }
    }

    public IdGenPolicy idGenPolicy = IdGenPolicy.APP_UUID;

    /**
     * Is the "main" table (containing type information and from which ids are
     * generated) separate from the "hierarchy" table.
     * <p>
     * Having it separate is only needed if a node can be in several places of
     * the hierarchy at the same time (shared nodes) -- this is not implemented
     * anyway for now.
     * <p>
     * Having it <em>not</em> separate improves performance.
     */
    public boolean separateMainTable = false;

}