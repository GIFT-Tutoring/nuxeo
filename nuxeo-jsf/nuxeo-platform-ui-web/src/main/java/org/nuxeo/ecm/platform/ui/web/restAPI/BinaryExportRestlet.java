/*
 *   File Name: BinaryExportRestlet.java
 *
 *   Classification:  Unclassified
 *
 *   Prime Contract No.: W91CRB11C0092
 *
 *   This work was generated under U.S. Government contract and the
 *   Government has unlimited data rights therein.
 *
 *   Copyrights:      Copyright 2014
 *                    Dignitas Technologies, LLC.
 *                    All rights reserved.
 *
 *   Distribution Statement A: Approved for public release; distribution is unlimited
 *
 *   Organizations:   Dignitas Technologies, LLC.
 *                    3504 Lake Lynda Drive, Suite 170
 *                    Orlando, FL 32817
 *
 */

package org.nuxeo.ecm.platform.ui.web.restAPI;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.DocumentPipe;
import org.nuxeo.ecm.core.io.DocumentReader;
import org.nuxeo.ecm.core.io.DocumentWriter;
import org.nuxeo.ecm.core.io.impl.DocumentPipeImpl;
import org.nuxeo.ecm.core.io.impl.plugins.DocumentTreeReader;
import org.nuxeo.ecm.core.io.impl.plugins.NuxeoLightArchiveWriter;
import org.restlet.data.MediaType;
import org.restlet.resource.Representation;

/**
 * Created this extension of the ExportRestlet class in order to use a different
 * version of the NuxeoArchiveWriter instance so that it will write a Binary export
 * of a tree's (folder) content (attached files) in a zip.  The zip will essentially
 * resemble the folder structure seen in Windows Explorer.
 * 
 * @author mhoffman
 *
 */
public class BinaryExportRestlet extends ExportRestlet implements Serializable {

    private static final long serialVersionUID = 7831287875548588711L;

    @Override
    protected Representation makeRepresentation(MediaType mediaType, DocumentModel root, final boolean exportAsTree,
            final boolean exportAsZip, final boolean isUnrestricted) {

        return new ExportRepresentation(mediaType, root, isUnrestricted) {

            @Override
            protected DocumentPipe makePipe() {
            	return new DocumentPipeImpl(10);
            }

            @Override
            protected DocumentReader makeDocumentReader(CoreSession documentManager, DocumentModel root)
                    throws ClientException {
                DocumentReader documentReader = new DocumentTreeReader(documentManager, root, false);
                return documentReader;
            }

            @Override
            protected DocumentWriter makeDocumentWriter(OutputStream outputStream) throws IOException {
                DocumentWriter documentWriter = new NuxeoLightArchiveWriter(outputStream);
                return documentWriter;
            }
        };
    }

}
