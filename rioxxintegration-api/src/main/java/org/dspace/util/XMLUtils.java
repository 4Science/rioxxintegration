/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.util;

import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * Should look at existing XMLUtils too
 */

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 12 Dec 2013
 */
public class XMLUtils {

    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(XMLUtils.class);

    /**
     * @param xml The starting context (a Node or a Document, for example).
     * @return node.getTextContent() on the node that matches singleNodeXPath
     * null if nothing matches the NodeListXPath
     */
    public static String getTextContent(Node xml, String singleNodeXPath) throws XPathExpressionException {
        String text = null;
        Node node = getNode(xml, singleNodeXPath);
        if (node != null) {
            text = node.getTextContent();
        }

        return text;
    }

    /**
     * @param xml The starting context (a Node or a Document, for example).
     * @return A Node matches the NodeListXPath
     * null if nothing matches the NodeListXPath
     */
    public static Node getNode(Node xml, String NodeListXPath) throws XPathExpressionException {
        Node result = null;
        try {
            result = XPathAPI.selectSingleNode(xml, NodeListXPath);
        } catch (TransformerException e) {
            log.error("Error", e);
        }
        return result;
    }

    /**
     * @param xml The starting context (a Node or a Document, for example).
     * @return A NodeList containing the nodes that match the NodeListXPath
     * null if nothing matches the NodeListXPath
     */
    public static NodeList getNodeList(Node xml, String NodeListXPath) throws XPathExpressionException {
        NodeList nodeList = null;
        try {
            nodeList = XPathAPI.selectNodeList(xml, NodeListXPath);
        } catch (TransformerException e) {
            log.error("Error", e);
        }
        return nodeList;
    }

    public static Iterator<Node> getNodeListIterator(Node xml, String NodeListXPath) throws XPathExpressionException {
        return getNodeListIterator(getNodeList(xml, NodeListXPath));
    }

    /**
     * Creates an iterator for all direct child nodes within a given NodeList
     * that are element nodes:
     * node.getNodeType() == Node.ELEMENT_NODE
     * node instanceof Element
     */
    public static Iterator<Node> getNodeListIterator(final NodeList nodeList) {
        return new Iterator<Node>() {
            private Iterator<Node> nodeIterator;
            private Node lastNode;

            {
                ArrayList<Node> nodes = new ArrayList<Node>();
                if (nodeList != null) {
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        //if (node.getNodeType() != Node.TEXT_NODE) {
                        if (node.getNodeType() == Node.ELEMENT_NODE && node instanceof Element) {
                            nodes.add(node);
                        }
                    }
                }
                nodeIterator = nodes.iterator();
            }

            @Override
            public boolean hasNext() {
                return nodeIterator.hasNext();
            }

            @Override
            public Node next() {
                lastNode = nodeIterator.next();
                return lastNode;
            }

            @Override
            public void remove() {
                nodeIterator.remove();
                //                lastNode.drop();
            }
        };
    }

    public static Document convertStreamToXML(InputStream is) {
        Document result = null;
        if (is != null) {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder builder = domFactory.newDocumentBuilder();
                result = builder.parse(is);
            } catch (ParserConfigurationException e) {
                log.error("Error", e);
            } catch (SAXException e) {
                log.error("Error", e);
            } catch (IOException e) {
                log.error("Error", e);
            }
        }
        return result;

    }

    public static String convertXMLtoString(Document transformDocument) {
        String output = null;
        try {
            DOMSource domSource = new DOMSource(transformDocument);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(domSource, result);
            output = writer.toString();
        } catch (TransformerConfigurationException e) {
            log.error("Error", e);
        } catch (TransformerException e) {
            log.error("Error", e);
        }
        return output;
    }

    /**
     * Applies a stylesheet to a given xml document.
     *
     * @param xmlDocument  the xml document to be transformed
     * @param xsltFilename the filename of the stylesheet
     * @return the transformed xml document
     * @throws Exception
     */
    public static Document transformDocument(Document xmlDocument,
                                             String xsltFilename) throws TransformerException, ParserConfigurationException {
        return transformDocument(xmlDocument, new Hashtable<String, String>(), xsltFilename);
    }

    /**
     * Applies a stylesheet (that receives parameters) to a given xml document.
     *
     * @param xmlDocument  the xml document to be transformed
     * @param parameters   the hashtable with the parameters to be passed to the
     *                     stylesheet
     * @param xsltFilename the filename of the stylesheet
     * @return the transformed xml document
     * @throws Exception
     */
    public static Document transformDocument(Document xmlDocument, Map<String, String> parameters, String xsltFilename) throws TransformerException, ParserConfigurationException {

        File xslFile = new File(xsltFilename);
        if (xslFile.exists()) {

            // Generate a Transformer.
            Transformer transformer = TransformerFactory.newInstance()
                    .newTransformer(new StreamSource(xsltFilename));

            if (transformer != null) {


                // set transformation parameters
                if (parameters != null) {
                    for (Map.Entry<String, String> param : parameters.entrySet()) {
                        transformer.setParameter(param.getKey(), param.getValue());
                    }
                }

                // Create an empty DOMResult object for the output.
                DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
                dFactory.setNamespaceAware(true);
                DocumentBuilder dBuilder = dFactory.newDocumentBuilder();
                Document dstDocument = dBuilder.newDocument();

                DOMResult domResult = new DOMResult(dstDocument);

                // Perform the transformation.
                transformer.transform(new DOMSource(xmlDocument), domResult);
                // Now you can get the output Node from the DOMResult.
                return dstDocument;
            }
        }
        return null;
    }
}
