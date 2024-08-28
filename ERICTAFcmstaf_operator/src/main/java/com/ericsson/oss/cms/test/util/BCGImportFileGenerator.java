/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.util;

import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.ATTRIBUTES;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.CONFIGDATA;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.END;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.ERICSSON;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.FILEFOOTER;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.FILEFORMATVERSION;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.FILEHEADER;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.MODIFIER;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.START;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.VENDORNAME;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.MOAttributes.ADJACENTCELL;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.MOs.MANAGEDELEMENT;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.MOs.MECONTEXT;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.MOs.RNCFUNCTION;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.MOs.SUBNETWORK;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.MOs.UTRANCELL;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.MOs.UTRANRELATION;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.NameSpace.MOCFILTER_PREFIX;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.NameSpace.UN;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.NameSpace.XN;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ericsson.oss.cms.test.operators.ActivateStKpiCliOperator;

/**
 * @author eeimacn
 */
/**
 * @author egergro
 */
public class BCGImportFileGenerator {

    private final static Logger LOGGER = LoggerFactory.getLogger(ActivateStKpiCliOperator.class);

    private static String rncToBeUsed = null;

    private int moIncrement = 0;

    private Element rncFunctionStart = null;

    private Element utranCellStart = null;

    private Element utranRelationStart = null;

    private Element attributeStart = null;

    private Element adjacentCellStart = null;

    private final ParsedFileData parsedFileData;

    private final String importType;

    private final String modifier;

    private final int totalMOs;

    public BCGImportFileGenerator(final ParsedFileData parsedFileData, final String importType, final String modifier, final int totalMOs) {
        this.parsedFileData = parsedFileData;
        this.importType = importType;
        this.modifier = modifier;
        this.totalMOs = totalMOs;
    }

    /**
     * Transforms the given export file to an import file of UtranRelations corresponding to a particular importType
     *
     * @param localExportFilePath
     *        The export file to be used when generating the import file.
     * @param localPath
     *        The file path on the local machine at which import file will be generated
     * @return <code>true</code> if create of import file successful, otherwise <code>false</code>
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */

    public boolean writeXML(final String localExportFilePath, final String localPath) throws XMLStreamException, FactoryConfigurationError,
    ParserConfigurationException, TransformerException, IOException {

        rncToBeUsed = findRNC(totalMOs, parsedFileData);
        LOGGER.info("RNC to be used is:" + rncToBeUsed);

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = null;

        final DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        documentFactory.setNamespaceAware(true);
        final DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        final Document document = documentBuilder.newDocument();

        Element rootElement = null;

        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        try (final FileReader fr = new FileReader(localExportFilePath)) {

            reader = factory.createXMLStreamReader(fr);

            rootElement = createRootAppendChild(document, parsedFileData);

            readEvents(reader, rootElement, document);

            final DOMSource domSource = new DOMSource(document);
            final StreamResult streamResult = new StreamResult(new File(localPath));
            transformer.transform(domSource, streamResult);
        } finally {
            reader.close();
        }
        return true;
    }

    private void readEvents(final XMLStreamReader reader, Element rootElement, final Document document) throws XMLStreamException {
        while (reader.hasNext()) {

            final int event = reader.next();

            if (isUtranCellTag(reader, event)) {
                rootElement = writeUtranRelation(reader, rootElement, document);
                continue;
            }

            if (isRequiredTag(reader, event) && isNotExceededRequiredRelations()) {
                final TagInfo startTag = new TagInfo(reader, START);
                startTag.populateAttributeMapFromXML();
                rootElement = createElement(document, startTag, rootElement, null);

            }

        }
    }

    private boolean isUtranCellTag(final XMLStreamReader reader, final int event) {
        return event == XMLStreamConstants.START_ELEMENT && (reader.getPrefix() + MOCFILTER_PREFIX + reader.getLocalName()).equals(UN + UTRANCELL);
    }

    private boolean isRequiredTag(final XMLStreamReader reader, final int event) {
        return event == XMLStreamConstants.START_ELEMENT && checkTag(reader.getPrefix() + MOCFILTER_PREFIX + reader.getLocalName());
    }

    private boolean isNotExceededRequiredRelations() {
        return moIncrement < totalMOs;
    }

    private String findRNC(final int totalMOs, final ParsedFileData parsedFileData) {
        final Map<String, ArrayList<String>> mapOfRncAndRelations = parsedFileData.getMapOfRncAndRelations();
        for (final String rncName : mapOfRncAndRelations.keySet()) {
            if (mapOfRncAndRelations.get(rncName).size() >= totalMOs) {
                return rncName;
            }
        }
        LOGGER.error("Two RNC not found having :" + totalMOs + "Utran Relations");
        return null;
    }

    private Element createRootAppendChild(final Document document, final ParsedFileData parsedFileData) {
        TagInfo fileHeader = null;
        TagInfo bulkCmConfigDataFile = null;
        bulkCmConfigDataFile = parsedFileData.getBulkCmConfigDataFile();
        fileHeader = parsedFileData.getFileHeader();
        final Element childElement = document.createElement(bulkCmConfigDataFile.getLocalName());
        childElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:un", bulkCmConfigDataFile.getNameSpaceQueue().poll());
        childElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:es", bulkCmConfigDataFile.getNameSpaceQueue().poll());
        childElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:xn", bulkCmConfigDataFile.getNameSpaceQueue().poll());
        childElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:gn", bulkCmConfigDataFile.getNameSpaceQueue().poll());
        childElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns", bulkCmConfigDataFile.getNameSpaceQueue().poll());
        document.appendChild(childElement);
        final Element fileHeaderTag = document.createElement(FILEHEADER);
        fileHeaderTag.setAttribute(FILEFORMATVERSION, fileHeader.getAttributeMap().get(FILEFORMATVERSION));
        fileHeaderTag.setAttribute(VENDORNAME, ERICSSON);
        childElement.appendChild(fileHeaderTag);

        return childElement;

    }

    private Element writeUtranRelation(final XMLStreamReader reader, Element rootElement, final Document document) throws XMLStreamException {

        final Queue<TagInfo> queue = new LinkedList<TagInfo>();
        String tagName = null;
        String adjacentCell = null;
        boolean relationCheck = false;
        boolean utranCellCheck = true;

        final TagInfo utranCellStartTag = new TagInfo(reader, START);
        utranCellStartTag.populateAttributeMapFromXML();

        final TagInfo attributeStart = null;
        final TagInfo adjacentCellStart = null;
        TagInfo adjacentCellContent = null;

        while (reader.hasNext()) {

            final int event = reader.next();
            if (reader.isWhiteSpace()) {
                continue;
            }

            switch (event) {

                case XMLStreamConstants.START_ELEMENT:
                    tagName = reader.getPrefix() + MOCFILTER_PREFIX + reader.getLocalName();
                    handleStartElement(tagName, reader, queue, attributeStart, adjacentCellStart);
                    break;

                case XMLStreamConstants.CHARACTERS:
                    if (tagName != null && tagName.equals(UN + ADJACENTCELL)) {
                        adjacentCell = reader.getText();
                        adjacentCellContent = new TagInfo(reader);
                        queue.add(adjacentCellContent);
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    tagName = reader.getPrefix() + MOCFILTER_PREFIX + reader.getLocalName();

                    if (tagName.equals(UN + UTRANRELATION)) {
                        relationCheck = checkRelation(adjacentCell, importType, parsedFileData);

                        if (relationCheck && isNotExceededRequiredRelations()) {

                            if (utranCellCheck) {
                                rootElement = createElement(document, utranCellStartTag, rootElement, null);
                                utranCellCheck = false;
                            }

                            while (queue.size() > 0) {
                                rootElement = createElement(document, queue.poll(), rootElement, queue);
                            }
                            moIncrement++;
                            adjacentCell = "";
                        }
                        if (!relationCheck) {
                            queue.clear();
                        }
                    } else if (tagName.equals(UN + UTRANCELL)) {
                        if (!utranCellCheck) {
                            rootElement = createElement(document, new TagInfo(reader, END), rootElement, null);
                        }
                        return rootElement;
                    }
                    break;
            }
        }
        return rootElement;
    }

    private void handleStartElement(String tagName, final XMLStreamReader reader, final Queue<TagInfo> queue, TagInfo attributeStart, TagInfo adjacentCellStart) {
        tagName = reader.getPrefix() + MOCFILTER_PREFIX + reader.getLocalName();
        if (tagName.equals(UN + UTRANRELATION)) {
            final TagInfo utranRelationStart = new TagInfo(reader, START);
            utranRelationStart.populateAttributeMapFromXML();
            queue.add(utranRelationStart);
        } else if (tagName.equals(UN + ATTRIBUTES)) {
            attributeStart = new TagInfo(reader, START);

            queue.add(attributeStart);
        } else if (tagName.equals(UN + ADJACENTCELL)) {
            adjacentCellStart = new TagInfo(reader, START);
            queue.add(adjacentCellStart);
        }
    }

    private Element createElement(final Document document, final TagInfo tag, final Element element, final Queue<TagInfo> queue) {

        Element childElement = null;

        if (tag.getTagType().equals(START)) {

            if (tag.getLocalName().equals(RNCFUNCTION)) {
                rncFunctionStart = document.createElement(tag.getTagPrefix() + MOCFILTER_PREFIX + tag.getLocalName());
                addAttributes(document, tag, rncFunctionStart);
                element.appendChild(rncFunctionStart);
                return element;
            }

            if (tag.getLocalName().equals(UTRANCELL)) {

                utranCellStart = document.createElement(tag.getTagPrefix() + MOCFILTER_PREFIX + tag.getLocalName());
                addAttributes(document, tag, utranCellStart);
                return element;
            }

            if (tag.getLocalName().equals(UTRANRELATION)) {

                utranRelationStart = document.createElement(tag.getTagPrefix() + MOCFILTER_PREFIX + tag.getLocalName());
                addAttributes(document, tag, utranRelationStart);
                final Attr attribute = document.createAttribute(MODIFIER);
                attribute.setValue(modifier);
                utranRelationStart.setAttributeNode(attribute);
                return element;
            }

            if (tag.getLocalName().equals(ATTRIBUTES)) {

                attributeStart = document.createElement(tag.getTagPrefix() + MOCFILTER_PREFIX + tag.getLocalName());
                addAttributes(document, tag, attributeStart);

                return element;
            }

            if (tag.getLocalName().equals(ADJACENTCELL)) {

                adjacentCellStart = document.createElement(tag.getTagPrefix() + MOCFILTER_PREFIX + tag.getLocalName());
                adjacentCellStart.appendChild(document.createTextNode(queue.poll().getCharacterContent()));
                attributeStart.appendChild(adjacentCellStart);
                utranRelationStart.appendChild(attributeStart);
                utranCellStart.appendChild(utranRelationStart);
                return element;
            }

        }

        if (tag.getLocalName().equals(UTRANCELL) && tag.getTagType().equals(END)) {

            rncFunctionStart.appendChild(utranCellStart);
            return rncFunctionStart;
        }

        if (!tag.getTagPrefix().isEmpty()) {
            childElement = document.createElement(tag.getTagPrefix() + MOCFILTER_PREFIX + tag.getLocalName());
        } else {
            childElement = document.createElement(tag.getLocalName());
        }

        element.appendChild(childElement);
        addAttributes(document, tag, childElement);
        if (tag.getLocalName().equals(CONFIGDATA)) {

            final Element fileFooter = document.createElement(FILEFOOTER);
            fileFooter.setAttribute("dateTime", getTime());
            element.appendChild(fileFooter);
        }

        return childElement;

    }

    private boolean checkTag(final String tagName) {
        final String[] tags = { MOCFILTER_PREFIX + CONFIGDATA, XN + SUBNETWORK, XN + MECONTEXT, XN + MANAGEDELEMENT, UN + RNCFUNCTION, UN + UTRANRELATION,
                UN + ATTRIBUTES, UN + ADJACENTCELL };
        final boolean tagFound = false;
        for (final String tag : tags) {
            if (tagName.contains(tag)) {
                return true;
            }
        }
        return tagFound;
    }

    private boolean checkRelation(final String adjacentCell, final String importType, final ParsedFileData parsedFileData) {

        String meContextValueId = "";

        if (!(adjacentCell == null) && !adjacentCell.isEmpty() && !adjacentCell.contains("ExternalUtranCell")) {
            final String[] fdnMOs = adjacentCell.split(",");
            meContextValueId = fdnMOs[2].split("=")[1];
        } else {
            return false;
        }

        if (importType.equalsIgnoreCase("intra") && !meContextValueId.contains(parsedFileData.getMeContextId())) {
            return false;
        } else if (importType.equalsIgnoreCase("inter") && meContextValueId.contains(parsedFileData.getMeContextId())) {
            return false;
        } else if (meContextValueId.equalsIgnoreCase(rncToBeUsed)) {
            return true;
        }
        return false;
    }

    private void addAttributes(final Document document, final TagInfo tag, final Element childElement) {

        final Set<String> attributeNames = tag.getAttributeMap().keySet();
        for (final String attributeName : attributeNames) {
            final Attr attribute = document.createAttribute(attributeName);
            attribute.setValue(tag.getAttributeMap().get(attributeName));
            childElement.setAttributeNode(attribute);
        }

    }

    private String getTime() {

        final Date tempDate = new Date();
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        final String currentTime = formatter.format(tempDate);
        return currentTime;

    }

    /**
     * @return
     *         The rnc that is being used in the create and delete xmls
     */
    public static String getRncToBeUsed() {
        return rncToBeUsed;
    }

}
