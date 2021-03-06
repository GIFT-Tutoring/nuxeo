/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nicolas Chapurlat <nchapurlat@nuxeo.com>
 */

package org.nuxeo.ecm.core.io.marshallers.json;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.nuxeo.ecm.core.io.registry.MarshallerRegistry;
import org.nuxeo.ecm.core.io.registry.Reader;
import org.nuxeo.ecm.core.io.registry.context.RenderingContext;
import org.nuxeo.ecm.core.io.registry.reflect.Supports;

/**
 * Base class for Json {@link Reader}.
 * <p>
 * This class provides an easy way to create java object from json and also provides the current context:
 * {@link AbstractJsonReader#ctx}. It provides you a {@link JsonNode} to manage the unmarshalling.
 * </p>
 * <p>
 * The use of this class optimize the JsonFactory usage especially when aggregating unmarshallers.
 * </p>
 *
 * @param <EntityType> The expected Java type.
 * @since 7.2
 */
@Supports(APPLICATION_JSON)
public abstract class AbstractJsonReader<EntityType> implements Reader<EntityType> {

    /**
     * The current {@link RenderingContext}.
     */
    @Inject
    protected RenderingContext ctx;

    /**
     * The marshaller registry. You may use it to use other marshallers.
     */
    @Inject
    protected MarshallerRegistry registry;

    @Override
    public boolean accept(Class<?> clazz, Type genericType, MediaType mediatype) {
        return true;
    }

    @Override
    public EntityType read(Class<?> clazz, Type genericType, MediaType mediaType, InputStream in) throws IOException {
        JsonNode jn = getNode(in, true);
        return read(jn);
    }

    /**
     * Provide a {@link JsonNode}, try to get it from the context.
     *
     * @param in The current {@link InputStream}.
     * @param getCurrentIfAvailable If true, try to get it from the context (if another marshaller already create it and
     *            call this marshaller).
     * @return A valid {@link JsonNode}.
     * @since 7.2
     */
    protected JsonNode getNode(InputStream in, boolean getCurrentIfAvailable) throws IOException, JsonParseException,
            JsonProcessingException {
        if (getCurrentIfAvailable) {
            if (in instanceof InputStreamWithJsonNode) {
                return ((InputStreamWithJsonNode) in).getJsonNode();
            }
        }
        JsonParser jp = JsonFactoryProvider.get().createJsonParser(in);
        JsonNode jn = jp.readValueAsTree();
        return jn;
    }

    /**
     * Implement this method, read the entity data in the provided {@link JsonNode} and return corresponding java
     * object.
     *
     * @param jn A ready to use {@link JsonNode}.
     * @return The unmarshalled entity.
     * @since 7.2
     */
    public abstract EntityType read(JsonNode jn) throws IOException;

    /**
     * Use this method to delegate the unmarshalling of a part or your Json to the {@link MarshallerRegistry}. This will
     * work only if a Json {@link Reader} is registered for the provided clazz and if the node format is the same as the
     * one expected by the marshaller.
     *
     * @param clazz The expected Java class.
     * @param genericType The generic type of the expected object: usefull if it's a generic List for example (use
     *            TypeUtils to create the parametrize type).
     * @param jn The {@link JsonNode} to unmarshall.
     * @return An object implementing the expected clazz.
     * @since 7.2
     */
    @SuppressWarnings("unchecked")
    protected <T> T readEntity(Class<?> clazz, Type genericType, JsonNode jn) throws IOException {
        Type effectiveGenericType = genericType != null ? genericType : clazz;
        Reader<T> reader = (Reader<T>) registry.getReader(ctx, clazz, effectiveGenericType, APPLICATION_JSON_TYPE);
        return reader.read(clazz, effectiveGenericType, APPLICATION_JSON_TYPE, new InputStreamWithJsonNode(jn));
    }

    /**
     * Try to get a string property of the given {@link JsonNode}. Return null if the node is null.
     *
     * @param jn The {@link JsonNode} to parse.
     * @param elName The property name.
     * @return The property text if it exists and it's a text, null otherwise.
     * @since 7.2
     */
    protected String getStringField(JsonNode jn, String elName) {
        JsonNode elNode = jn.get(elName);
        if (elNode != null && !elNode.isNull() && elNode.isTextual()) {
            return elNode.getTextValue();
        } else {
            return null;
        }
    }

}
