/*
 *   File Name: NuxeoLightArchiveWriter.java
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

package org.nuxeo.ecm.core.io.impl.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.DWord;

/**
 * This version of the NuxeoArchiveWriter is used to write a Binary export
 * of a tree's (folder) content (attached files) in a zip.  The zip will essentially
 * resemble the folder structure seen in Windows Explorer.
 * 
 * @author mhoffman
 */
public class NuxeoLightArchiveWriter extends NuxeoArchiveWriter {

    public NuxeoLightArchiveWriter(OutputStream out) throws IOException {
        super(new ZipOutputStream(out), Deflater.DEFAULT_COMPRESSION);
    }
    
    @Override
    protected void writeDocument(String path, ExportedDocument doc) throws IOException {

        if (path.equals("/") || path.length() == 0) {
            path = "";
        } else { 
            
        	//writing folder here (folders don't have blobs) 
            if(doc.getBlobs().isEmpty()){
                path += '/';
	            ZipEntry entry = new ZipEntry(path);
	            // store the number of child as an extra info on the entry
	            entry.setExtra(new DWord(doc.getFilesCount()).getBytes());
	            out.putNextEntry(entry);
	            out.closeEntry();
            }
            // System.out.println(">> add entry: "+entry.getName());
        }


        // write this document's blobs
        Map<String, Blob> blobs = doc.getBlobs(); 
        for (Map.Entry<String, Blob> blobEntry : blobs.entrySet()) {

        	//filename of the blob (attachment)
            String fileName = blobEntry.getValue().getFilename(); 
            
            //the path provided inherently has the file name as the last part of the path,
            //because we don't want to create a folder in the path that wouldn't exist otherwise
            ZipEntry entry = new ZipEntry(path.substring(0, path.lastIndexOf("/")+1) + fileName);
            out.putNextEntry(entry);
            InputStream in = null;
            try {
                in = blobEntry.getValue().getStream();
                FileUtils.copy(in, out);
            } finally {
                if (in != null) {
                    in.close();
                }
                out.closeEntry();
                // System.out.println(">> add entry: "+entry.getName());
            }
        }
    }

}
