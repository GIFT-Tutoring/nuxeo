/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     bstefanescu
 *
 * $Id$
 */

package org.nuxeo.ecm.platform.rendering.fm.adapters;

import java.util.Iterator;

import org.nuxeo.ecm.core.api.model.Property;

import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public class PropertyIteratorTemplate extends PropertyWrapper implements TemplateModelIterator, AdapterTemplateModel {

    protected final Iterator<Property> iterator;

    public PropertyIteratorTemplate(DocumentObjectWrapper wrapper, Iterator<Property> iterator) {
        super(wrapper);
        this.iterator = iterator;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdaptedObject(Class hint) {
        return iterator;
    }

    @Override
    public boolean hasNext() throws TemplateModelException {
        return iterator.hasNext();
    }

    @Override
    public TemplateModel next() throws TemplateModelException {
        return wrap(iterator.next());
    }

}
