/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.util;

import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.BULK_CM_CONFIG_DATA_FILE;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.FILEHEADER;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.START;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.MOAttributes.ADJACENTCELL;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.MOs.MECONTEXT;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.MOs.UTRANCELL;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.MOs.UTRANRELATION;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author egergro
 */
public class BCGExportFileParser {

    private final static Logger LOGGER = LoggerFactory.getLogger(BCGExportFileParser.class);

    private String tagContent;

    private String meContextId;

    private String utranCellId;

    private String utranRelationId;

    private boolean meContextFound;

    private boolean meContextCheck;

    private TagInfo fileHeader;

    private TagInfo bulkCmConfigDataFile;

    private final Map<String, ArrayList<String>> mapOfRncAndRelations = new HashMap<String, ArrayList<String>>();

    final Set<String> meContext = new LinkedHashSet<String>();

    /**
     * Takes a file of exported UtranRelations and parses it to build a data object that contains
     * the information needed to build an import file for the given type of UtranRelation
     *
     * @param nodeRelationType
     *        Type of UtranRelation to be extracted from file
     * @param localExportFilePath
     *        File path of the UtranRelation export xml file that is to be parsed
     * @return
     *         An object containing all the necessary data to create an import xml file
     * @throws FileNotFoundException
     * @throws XMLStreamException
     * @throws IOException
     */
    public ParsedFileData getParsedFileData(final String nodeRelationType, final String localExportFilePath) throws FileNotFoundException, XMLStreamException,
    IOException {

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = null;

        try (final FileReader fr = new FileReader(localExportFilePath)) {
            reader = factory.createXMLStreamReader(fr);
            while (reader.hasNext()) {

                if (meContextFound) {
                    LOGGER.info("Parsing of the other RNC's not required, more than one RNC has already been found .");
                    break;
                }

                if (meContext.size() == 1) {
                    meContextCheck = true;
                }

                populateDataFields(nodeRelationType, reader);
            }
        } finally {

            reader.close();
        }

        final ParsedFileData parsedFileData = new ParsedFileData();
        parsedFileData.setMeContextId(meContextId);
        parsedFileData.setFileHeader(fileHeader);
        parsedFileData.setBulkCmConfigDataFile(bulkCmConfigDataFile);
        parsedFileData.setMapOfRncAndRelations(mapOfRncAndRelations);

        return parsedFileData;
    }

    private void populateDataFields(final String importType, final XMLStreamReader reader) throws XMLStreamException {

        final int event = reader.next();

        switch (event) {

            case XMLStreamConstants.START_ELEMENT:
                handleStartElement(reader);
                break;

            case XMLStreamConstants.CHARACTERS:
                tagContent = reader.getText().trim();
                break;

            case XMLStreamConstants.END_ELEMENT:
                if (reader.getLocalName().equalsIgnoreCase(ADJACENTCELL)) {
                    seperateUtranRelations(new Fdn(tagContent), importType);
                }
                break;
        }
    }

    private void handleStartElement(final XMLStreamReader reader) {
        if (MECONTEXT.equals(reader.getLocalName()) && meContext.size() == 0) {
            meContextId = reader.getAttributeValue(0);
            meContext.add(meContextId);

        } else if (MECONTEXT.equals(reader.getLocalName()) && meContext.size() > 0 && meContextCheck) {
            meContextFound = true;

        } else if (UTRANCELL.equals(reader.getLocalName())) {
            utranCellId = reader.getAttributeValue(0);

        } else if (UTRANRELATION.equals(reader.getLocalName())) {
            utranRelationId = reader.getAttributeValue(0);

        } else if (FILEHEADER.equals(reader.getLocalName())) {
            fileHeader = new TagInfo(reader, START);
            fileHeader.populateAttributeMapFromXML();

        } else if (BULK_CM_CONFIG_DATA_FILE.equals(reader.getLocalName())) {
            bulkCmConfigDataFile = new TagInfo(reader, START);
            bulkCmConfigDataFile.populateAttributeMapFromXML();
            bulkCmConfigDataFile.populateNameSpaceQueueFromXML();

        }
    }

    private void seperateUtranRelations(final Fdn adjacentCell, final String importType) {

        String meContextValueId = null;

        if (isAdjacentUtranCell(adjacentCell)) {
            meContextValueId = adjacentCell.getNameOfFdnPart("MeContext");
        } else {
            return;
        }

        if (importType.equalsIgnoreCase("intra") && !isOnSameNode(meContextValueId)) {
            return;

        } else if (importType.equalsIgnoreCase("inter") && isOnSameNode(meContextValueId)) {
            return;

        } else {
            ArrayList<String> relationsList = mapOfRncAndRelations.get(meContextValueId);
            if (relationsList == null) {
                relationsList = new ArrayList<String>();
            }
            relationsList.add(utranCellId + utranRelationId + adjacentCell);
            mapOfRncAndRelations.put(meContextValueId, relationsList);
        }
    }

    private boolean isOnSameNode(final String meContextValueId) {
        return meContextValueId.contains(meContextId);
    }

    private boolean isAdjacentUtranCell(final Fdn adjacentCell) {
        return !(adjacentCell == null) && !adjacentCell.getFdn().isEmpty() && !adjacentCell.getFdn().contains("ExternalUtranCell");
    }
};
