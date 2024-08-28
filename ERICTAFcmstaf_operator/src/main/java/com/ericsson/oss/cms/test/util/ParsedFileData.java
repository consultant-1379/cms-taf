/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility object to hold the data required to create an import xml for creation of UtranRelations
 *
 * @author egergro
 */
public class ParsedFileData {

    String meContextId;

    private Map<String, ArrayList<String>> mapOfRncAndRelations = new HashMap<String, ArrayList<String>>();

    private TagInfo fileHeader;

    private TagInfo bulkCmConfigDataFile;

    /**
     * @return the meContextId
     */
    public String getMeContextId() {
        return meContextId;
    }

    /**
     * @param meContextId
     *        The meContextId to set
     */
    public void setMeContextId(final String meContextId) {
        this.meContextId = meContextId;
    }

    /**
     * @return the mapOfRncAndRelations
     */
    public Map<String, ArrayList<String>> getMapOfRncAndRelations() {
        return mapOfRncAndRelations;
    }

    /**
     * @param mapOfRncAndRelations
     *        the mapOfRncAndRelations to set
     */
    public void setMapOfRncAndRelations(final Map<String, ArrayList<String>> mapOfRncAndRelations) {
        this.mapOfRncAndRelations = mapOfRncAndRelations;
    }

    /**
     * @return the fileHeader
     */
    public TagInfo getFileHeader() {
        return fileHeader;
    }

    /**
     * @param fileHeader
     *        the fileHeader to set
     */
    public void setFileHeader(final TagInfo fileHeader) {
        this.fileHeader = fileHeader;
    }

    /**
     * @return the bulkCmConfigDataFile
     */
    public TagInfo getBulkCmConfigDataFile() {
        return bulkCmConfigDataFile;
    }

    /**
     * @param bulkCmConfigDataFile
     *        the bulkCmConfigDataFile to set
     */
    public void setBulkCmConfigDataFile(final TagInfo bulkCmConfigDataFile) {
        this.bulkCmConfigDataFile = bulkCmConfigDataFile;
    }

}
