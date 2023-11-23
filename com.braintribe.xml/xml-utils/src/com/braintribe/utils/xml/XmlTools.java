// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.utils.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.MemorySaveStringWriter;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.xml.xpath.MapBasedNamespaceContext;

/**
 * This class provides XML related utility methods.
 */
public class XmlTools {

	private static Logger logger = Logger.getLogger(XmlTools.class);

	public static final Pattern pathSplitPattern = Pattern.compile("/");
	public static final XPathFactory xpathFactory = XPathFactory.newInstance();
	public static final XPath xpath = xpathFactory.newXPath();

	protected static final TransformerFactory poxTranformerFactory;
	protected static final DocumentBuilderFactory poxBuilderFactory;
	protected static DocumentBuilder poxBuilder = null;

	static {
		poxBuilderFactory = DocumentBuilderFactory.newInstance();

		boolean trace = logger.isTraceEnabled();
		try {
			poxBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		} catch (Exception e) {
			if (trace)
				logger.trace("Could not set feature disallow-doctype-decl=true", e);
		}
		try {
			poxBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		} catch (Exception e) {
			if (trace)
				logger.trace("Could not set feature external-general-entities=false", e);
		}
		try {
			poxBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		} catch (Exception e) {
			if (trace)
				logger.trace("Could not set feature external-parameter-entities=false", e);
		}
		try {
			poxBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (Exception e) {
			if (trace)
				logger.trace("Could not set feature load-external-dtd=false", e);
		}
		poxBuilderFactory.setXIncludeAware(false);
		poxBuilderFactory.setExpandEntityReferences(false);

		poxTranformerFactory = TransformerFactory.newInstance();
		try {
			poxTranformerFactory.setAttribute(XmlConstants.ACCESS_EXTERNAL_DTD, "");
		} catch (Exception e) {
			if (trace)
				logger.trace("Could not set attribute " + XmlConstants.ACCESS_EXTERNAL_DTD + "=<empty>", e);
		}
		try {
			poxTranformerFactory.setAttribute(XmlConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		} catch (Exception e) {
			if (trace)
				logger.trace("Could not set attribute " + XmlConstants.ACCESS_EXTERNAL_STYLESHEET + "=<empty>", e);
		}
	}

	public static int[] getIntsAttribute(Element e, String name, int[] def) {
		String encodedInts[] = getStringsAttribute(e, name, null);
		if (encodedInts == null)
			return def;
		else {
			int results[] = new int[encodedInts.length];
			for (int i = 0; i < results.length; i++) {
				results[i] = Integer.parseInt(encodedInts[i]);
			}
			return results;
		}
	}

	public static int[] getIntsAttribute(Element e, String name) {
		return getIntsAttribute(e, name, null);
	}

	public static String[] getStringsAttribute(Element e, String name, String[] def) {
		String encoded = getAttribute(e, name, null);
		if (encoded == null)
			return def;
		if (encoded.length() == 0)
			return new String[0];
		else
			return encoded.split(",");
	}

	public static String[] getStringsAttribute(Element e, String name) {
		return getStringsAttribute(e, name, null);
	}

	public static Date getTimestampAttribute(Element e, String name, Date def) {
		Long timestamp = getLongAttribute(e, name, null);
		if (timestamp == null)
			return def;
		else
			return new Date(timestamp);
	}

	public static Date getTimestampAttribute(Element e, String name) {
		return getTimestampAttribute(e, name, null);
	}

	public static Boolean getBooleanAttribute(Element e, String name, Boolean def) {
		String encoded = getAttribute(e, name, null);
		if (encoded == null)
			return def;
		else
			return Boolean.parseBoolean(encoded);
	}

	public static Boolean getBooleanAttribute(Element e, String name) {
		return getBooleanAttribute(e, name, null);
	}

	public static Double getDoubleAttribute(Element e, String name, Double def) {
		String encoded = getAttribute(e, name, null);
		if (encoded == null)
			return def;
		else
			return Double.parseDouble(encoded);
	}

	public static Double getDoubleAttribute(Element e, String name) {
		return getDoubleAttribute(e, name, null);
	}

	public static Long getLongAttribute(Element e, String name, Long def) {
		String encoded = getAttribute(e, name, null);
		if (encoded == null)
			return def;
		else
			return Long.parseLong(encoded);
	}

	public static Long getLongAttribute(Element e, String name) {
		return getLongAttribute(e, name, null);
	}

	public static Integer getIntegerAttribute(Element e, String name, Integer def) {
		String encoded = getAttribute(e, name, null);
		if (encoded == null)
			return def;
		else
			return Integer.parseInt(encoded);
	}

	public static Integer getIntegerAttribute(Element e, String name) {
		return getIntegerAttribute(e, name, null);
	}

	public static String getAttribute(Element e, String name) {
		return getAttribute(e, name, null);
	}

	public static String getAttribute(Element e, String name, String def) {
		if (e.getAttributeNode(name) == null)
			return def;
		return e.getAttribute(name);
	}

	public static Document loadXML(File f) throws SAXException, IOException, ParserConfigurationException {
		return loadXML(f.toURI().toURL());
	}

	public static Document loadXML(URL u) throws SAXException, IOException, ParserConfigurationException {
		InputStream in = null;
		try {
			in = u.openStream();

			Document doc = loadXML(in);

			try {
				URI uri = null;
				try {
					uri = u.toURI();
				} catch (URISyntaxException ex) {
					uri = new URI(u.getProtocol(), u.getUserInfo(), u.getHost(), u.getPort(), u.getPath(), u.getQuery(), u.getRef());
				}

				doc.setDocumentURI(uri.toString());

			} catch (URISyntaxException e) {
				/* Advise: uri construction from URL is a litte tricky because not any valid url is a valid uri. If
				 * there are any problems with exceptions here please analyse why it is an invalid uri */
				throw new Error("Error while creating base uri for xml document based on url " + u, e);
			}

			return doc;
		} finally {
			if (in != null)
				in.close();
		}
	}

	public static Document createDocument() throws ParserConfigurationException {
		synchronized (poxBuilderFactory) {
			if (poxBuilder == null)
				poxBuilder = poxBuilderFactory.newDocumentBuilder();
			return poxBuilder.newDocument();
		}
	}

	public static Document loadXML(InputSource in, boolean useSharedBuilder) throws SAXException, IOException, ParserConfigurationException {
		if (useSharedBuilder) {
			synchronized (poxBuilderFactory) {
				if (poxBuilder == null)
					poxBuilder = poxBuilderFactory.newDocumentBuilder();

				Document doc = poxBuilder.parse(in); // NOTE: poxBuilder is not thread safe, parse in sync block
				return doc;
			}
		} else {
			DocumentBuilder builder;
			synchronized (poxBuilderFactory) {
				builder = poxBuilderFactory.newDocumentBuilder();
			}

			Document doc = builder.parse(in); // NOTE: builder is safe bacause local, so parse outside sync block
			return doc;
		}
	}

	public static Document loadXML(InputStream in) throws SAXException, IOException, ParserConfigurationException {
		return loadXML(new InputSource(in), false);
	}

	public static Document parseXML(String s) throws SAXException, ParserConfigurationException {
		try {
			if (s == null || s.trim().length() == 0)
				return null;
			else
				return loadXML(new InputSource(new StringReader(s)), false);
		} catch (IOException e) {
			throw new RuntimeException("IOException while reading from string reader (strange)", e);
		}
	}

	public static Document parseXMLFile(File xmlFile, String optionalXSDFile, boolean disableValidation, EntityResolver entityResolver)
			throws Exception {

		boolean trace = logger.isTraceEnabled();

		// Get a parser capable of parsing vanilla XML into a DOM tree
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		if (trace) {
			logger.trace("Got factory " + factory.getClass());
		}

		factory.setNamespaceAware(true);

		if (disableValidation) {
			factory.setValidating(false);
			try {
				factory.setFeature("http://xml.org/sax/features/namespaces", false);
				factory.setFeature("http://xml.org/sax/features/validation", false);
				factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
				factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			} catch (Throwable e) {
				throw new Exception("We have problems setting the features. Please check the DocumentBuilderFactory implementation at "
						+ CommonTools.getClassLocation(factory.getClass()), e);
			}
		}

		DocumentBuilder builder = factory.newDocumentBuilder();

		if (trace) {
			logger.trace("Got builder: " + builder.getClass());
		}

		if (entityResolver != null) {
			if (trace) {
				logger.trace("Using entity resolver: " + entityResolver);
			}
			builder.setEntityResolver(entityResolver);
		}

		// parse the XML purely as XML and get a DOM tree represenation.
		Document document = builder.parse(xmlFile);

		if (trace) {
			logger.trace("Parsed document from " + xmlFile.getAbsolutePath());
		}

		if (!StringTools.isEmpty(optionalXSDFile)) {
			// schema is specified --> validate file

			File xsd = FileTools.getExistingFile(optionalXSDFile);
			// build an XSD-aware SchemaFactory
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			// get the custom xsd schema describing the required format for my XML files.

			Schema schemaXSD = schemaFactory.newSchema(xsd);

			// Create a Validator capable of validating XML files according to my custom schema.
			Validator validator = schemaXSD.newValidator();

			// parse the XML DOM tree againts the stricter XSD schema
			validator.validate(new DOMSource(document));
		}

		return document;
	}
	/* public static void xmlToTable(String xml, DefaultTableModel model, String tag, String[] attribs) throws
	 * SAXException, ParserConfigurationException { Document doc= parseXML( xml );
	 * 
	 * NodeList elements = doc.getElementsByTagName(tag);
	 * 
	 * for (int i = 0; i < elements.getLength(); i++) { Element element = (Element)elements.item(i);
	 * 
	 * String[] v= new String[attribs.length]; for (int j = 0; j < attribs.length; j++) { v[j]= element.getAttribute(
	 * attribs[j] ); }
	 * 
	 * model.addRow(v); } }
	 * 
	 * public static void xmlToMap(String xml, Map m, String tag, String keyAttr, String valueAttr) throws SAXException,
	 * ParserConfigurationException { Document doc= parseXML( xml );
	 * 
	 * NodeList elements = doc.getElementsByTagName(tag);
	 * 
	 * for (int i = 0; i < elements.getLength(); i++) { Element element = (Element)elements.item(i);
	 * 
	 * String k = element.getAttribute(keyAttr); String v = valueAttr.equals("*") ? element.getTextContent() :
	 * element.getAttribute(valueAttr);
	 * 
	 * m.put(k, v); } } */

	public static Element getElement(Element element, String path) throws XPathExpressionException {
		Object nodeObject = xpath.evaluate(path, element, XPathConstants.NODE);
		if (nodeObject == null)
			return null;

		if (!(nodeObject instanceof Node))
			throw new IllegalArgumentException("xpath " + path + " does not designate an XML element (tag)");
		else
			return (Element) nodeObject;

	}

	public static NodeList getElements(Element element, String path) throws XPathExpressionException {
		Object nodeListObject = xpath.evaluate(path, element, XPathConstants.NODESET);
		if (nodeListObject == null)
			return null;

		if (!(nodeListObject instanceof NodeList))
			throw new IllegalArgumentException("xpath " + path + " does not designate an XML NodeList (tag)");
		else
			return (NodeList) nodeListObject;
	}

	public static Element getFirstElement(Element parent, String tagName, String attributeName, String attributeValue) {
		Node childNode = parent.getFirstChild();
		while (childNode != null) {
			if (childNode instanceof Element && childNode.getNodeName().equals(tagName)) {
				Element childElement = (Element) childNode;
				if (childElement.getAttribute(attributeName).equals(attributeValue))
					return childElement;
			}
			childNode = childNode.getNextSibling();
		}
		return null;
	}

	public static Element getFirstElement(Element parent, String tagName, String[] attributeNames, String[] attributeValues) {
		Node childNode = parent.getFirstChild();
		while (childNode != null) {
			if (childNode instanceof Element && childNode.getNodeName().equals(tagName)) {
				Element childElement = (Element) childNode;

				boolean match = true;
				for (int i = 0; match && (i < attributeNames.length); i++) {
					if (!childElement.getAttribute(attributeNames[i]).equals(attributeValues[i]))
						match = false;
				}

				if (match)
					return childElement;
			}
			childNode = childNode.getNextSibling();
		}
		return null;
	}

	public static Element getFirstElement(Element parent, String tagName) {
		Node childNode = parent.getFirstChild();
		while (childNode != null) {
			if (childNode instanceof Element && (tagName == null || childNode.getNodeName().equals(tagName)))
				return (Element) childNode;
			childNode = childNode.getNextSibling();
		}
		return null;
	}

	public static Text getFirstText(Element parent) {
		TextIterator textIterator = new TextIterator(parent);
		if (textIterator.hasNext())
			return textIterator.next();
		else
			return null;
	}

	public static String getFirstTextAsString(Element parent) {
		return getFirstTextAsString(parent, "");
	}

	public static String getFirstTextAsString(Element parent, String def) {
		Text text = getFirstText(parent);
		if (text == null)
			return def;
		else
			return text.getData();
	}

	public static Element getNextElement(Element predecessor, String tagName) {
		Node node = predecessor.getNextSibling();
		while (node != null) {
			if (node instanceof Element && (tagName == null || node.getNodeName().equals(tagName)))
				return (Element) node;
			node = node.getNextSibling();
		}
		return null;
	}

	public static class ElementIterator implements Iterator<Element> {

		private Element element;
		private String tagName;

		public ElementIterator(Element parent, String tagName) {
			this.tagName = tagName;
			this.element = XmlTools.getFirstElement(parent, tagName);
		}

		@Override
		public boolean hasNext() {
			return element != null;
		}

		@Override
		public Element next() {
			Element retVal = element;
			element = XmlTools.getNextElement(element, tagName);
			return retVal;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	public static Iterable<Element> getChildElements(final Element parent, final String tagName) {
		return new Iterable<Element>() {
			@Override
			public Iterator<Element> iterator() {
				return getElementIterator(parent, tagName);
			}
		};
	}

	public static Iterator<Element> getElementIterator(Element parent, String tagName) {
		return new ElementIterator(parent, tagName);
	}

	public static Element getElementByPath(Element parent, String path) {
		String tagNames[] = pathSplitPattern.split(path);
		getElementByElementNames(parent, tagNames);
		Element element = parent;
		for (String tagName : tagNames) {
			element = getFirstElement(element, tagName);
			if (element == null)
				break;
		}

		return element;
	}

	public static Element getElementByElementNames(Element parent, String... tagNames) {
		Element element = parent;
		for (String tagName : tagNames) {
			element = getFirstElement(element, tagName);
			if (element == null)
				break;
		}

		return element;
	}

	public static Element getElementByPath(Element parent, String path, String attributeName, String attributeValue) {
		String tagNames[] = pathSplitPattern.split(path);
		Element element = parent;
		int index = 0;
		for (String tagName : tagNames) {
			boolean last = index == tagNames.length - 1;
			if (last)
				element = getFirstElement(element, tagName, attributeName, attributeValue);
			else
				element = getFirstElement(element, tagName);
			if (element == null)
				break;
			index++;
		}

		return element;
	}

	/**
	 * Apply xinclude processing; see http://www.w3.org/TR/xinclude/
	 * 
	 * @throws Exception
	 *             In case the xinclude processing raised an error.
	 * @deprecated Use the {@link #xinclude(Node, Function)} as this has better support for xpointer includes. Use null
	 *             for the Function parameter to maintain the same functionality.
	 */
	@Deprecated
	public static void xinclude(Node n) throws Exception { // FIXME: specific exceptions!
		if (n instanceof Element) {
			Element e = (Element) n;
			String tag = e.getTagName();

			if (tag.equals("xi:include")) {
				performeXInclude(e);
				return; // NOTE: e is gone, recursive processing must happen inside performeXInclude()
			}
		}

		Node m = n.getFirstChild();

		while (m != null) {
			Node next = m.getNextSibling(); // NOTE: remember reference; if xinclude is performed, siblings are lost

			if (m instanceof Element)
				xinclude(m);
			m = next;
		}
	}

	/**
	 * @param e
	 * @throws Exception
	 * @deprecated Use the {@link #performeXInclude(Element, Function)} method instead.
	 */
	protected static void performeXInclude(Element e) throws Exception {
		String href = getAttribute(e, "href");
		if (href == null)
			throw new Exception("missing href attribute in <xi:include> element"); // FIXME: specific exception!

		String parse = getAttribute(e, "parse", "xml");
		String encoding = getAttribute(e, "encoding");

		String xpointer = getAttribute(e, "xpointer");

		NodeList f = e.getElementsByTagName("xi:fallback");
		Element fallback = f.getLength() > 0 ? (Element) f.item(0) : null;

		String base = e.getBaseURI();
		URL u = base == null ? new URL(href) : new URL(new URL(base), href);

		try {
			if (parse.equals("xml")) {
				Document idoc = loadXML(u); // TODO: use encoding?
				Node n = idoc.getDocumentElement();

				if (xpointer != null) {
					// TODO: xpointers define ranges - a plain xpath falls short.
					NodeList nodes = (NodeList) xpath.evaluate(xpointer, n, XPathConstants.NODESET);

					if (nodes.getLength() == 0) { // TODO: allow empty nodeset?...
						// TODO: is this a resource exception (should trigger fallback?)
						// FIXME: specific exception!
						throw new Exception("failed to resolve xpath " + xpointer + " in document " + href);
					}

					Node p = e.getParentNode();
					Node at = e.getNextSibling();

					for (int i = 0; i < nodes.getLength(); i++) {
						n = nodes.item(i);
						n = e.getOwnerDocument().importNode(n, true);

						p.insertBefore(n, at);
					}

					p.removeChild(e);
				} else {
					xinclude(n); // NOTE: apply xi-processing recusively on loaded document!

					n = e.getOwnerDocument().importNode(n, true);
					e.getParentNode().replaceChild(n, e); // TODO: include leading/trailing PE/Comments/etc?!
				}
			} else {
				String text = encoding == null ? IOTools.slurp(u) : IOTools.slurp(u.openStream(), encoding);
				Document doc = e.getOwnerDocument();
				Node n = doc.createTextNode(text);

				e.getParentNode().replaceChild(n, e);
			}
		} catch (IOException ex) {
			if (fallback == null)
				throw ex;

			Node p = e.getParentNode();
			Node at = e.getNextSibling();
			Node n = fallback.getFirstChild();

			while (n != null) {
				Node next = n.getNextSibling();

				n.getParentNode().removeChild(n);
				p.insertBefore(n, at);
				n = next;
			}

			p.removeChild(e);
		}
	}

	public static void xinclude(Node n, Function<String, InputStream> resources) throws Exception {
		xinclude(n, resources, true);
	}

	/**
	 * Apply xinclude processing; see http://www.w3.org/TR/xinclude/
	 * 
	 * @throws Exception
	 *             In case the xinclude processing raised an error.
	 */
	public static void xinclude(Node n, Function<String, InputStream> resources, boolean targetMustNotBeEmpty) throws Exception { // FIXME:
																																	// specific
		// exceptions!
		if (n instanceof Element) {
			Element e = (Element) n;
			String tag = e.getTagName();

			if (tag.equals("xi:include")) {
				performeXInclude(e, resources, targetMustNotBeEmpty);
				return; // NOTE: e is gone, recursive processing must happen inside performeXInclude()
			}
		}

		Node m = n.getFirstChild();

		while (m != null) {
			Node next = m.getNextSibling(); // NOTE: remember reference; if xinclude is performed, siblings are lost

			if (m instanceof Element)
				xinclude(m, resources, targetMustNotBeEmpty);
			m = next;
		}
	}

	protected static void performeXInclude(Element e, Function<String, InputStream> resources) throws Exception {
		performeXInclude(e, resources, true);
	}

	protected static void performeXInclude(Element e, Function<String, InputStream> resources, boolean targetMustNotBeEmpty) throws Exception {
		String href = getAttribute(e, "href");
		if (href == null)
			throw new Exception("missing href attribute in <xi:include> element"); // FIXME: specific exception!

		String parse = getAttribute(e, "parse", "xml");
		String encoding = getAttribute(e, "encoding");

		String xpointer = getAttribute(e, "xpointer");
		if (xpointer != null) {
			String lowerCase = xpointer.toLowerCase();
			if (lowerCase.startsWith("xpointer(") && lowerCase.endsWith(")")) {
				xpointer = xpointer.substring(9, xpointer.length() - 1);
			}
		}

		NodeList f = e.getElementsByTagName("xi:fallback");
		Element fallback = f.getLength() > 0 ? (Element) f.item(0) : null;

		InputStream in = null;
		if (resources != null) {
			in = resources.apply(href);
		} else {
			String base = e.getBaseURI();
			URL u = base == null ? new URL(href) : new URL(new URL(base), href);
			in = u.openStream();
		}

		try {
			if (parse.equals("xml")) {
				Document idoc = loadXML(in); // TODO: use encoding?
				Node n = idoc.getDocumentElement();

				if (xpointer != null) {
					Node p = e.getParentNode();

					// TODO: xpointers define ranges - a plain xpath falls short.
					NodeList nodes = (NodeList) xpath.evaluate(xpointer, n, XPathConstants.NODESET);

					if (nodes.getLength() == 0) { // TODO: allow empty nodeset?...
						if (targetMustNotBeEmpty) {
							// TODO: is this a resource exception (should trigger fallback?)
							// FIXME: specific exception!
							throw new Exception("failed to resolve xpath " + xpointer + " in document " + href);
						}
					} else {

						Node at = e.getNextSibling();

						for (int i = 0; i < nodes.getLength(); i++) {
							n = nodes.item(i);
							n = e.getOwnerDocument().importNode(n, true);

							p.insertBefore(n, at);
						}
					}

					p.removeChild(e);
				} else {
					xinclude(n); // NOTE: apply xi-processing recusively on loaded document!

					n = e.getOwnerDocument().importNode(n, true);
					e.getParentNode().replaceChild(n, e); // TODO: include leading/trailing PE/Comments/etc?!
				}
			} else {
				String text = encoding == null ? IOTools.slurp(in, "UTF-8") : IOTools.slurp(in, encoding);
				Document doc = e.getOwnerDocument();
				Node n = doc.createTextNode(text);

				e.getParentNode().replaceChild(n, e);
			}
		} catch (IOException ex) {
			IOTools.closeCloseable(in, logger);

			if (fallback == null)
				throw ex;

			Node p = e.getParentNode();
			Node at = e.getNextSibling();
			Node n = fallback.getFirstChild();

			while (n != null) {
				Node next = n.getNextSibling();

				n.getParentNode().removeChild(n);
				p.insertBefore(n, at);
				n = next;
			}

			p.removeChild(e);
		}
	}

	public static String toString(Node node) throws TransformerException {
		MemorySaveStringWriter writer = null;
		try {
			writer = new MemorySaveStringWriter(10 * Numbers.MEGABYTE);
			writeXml(node, writer, "UTF-8"); // or use UTF-16?... //TODO: terse, no indent...
			return writer.toString();
		} finally {
			if (writer != null) {
				try {
					writer.destroy();
				} catch (Exception e) {
					logger.error("Could not destroy writer.", e);
				}
			}
		}
	}

	public static void writeXml(Node node, File f, String encoding) throws IOException, TransformerException {
		writeXml(node, f, encoding, false);
	}

	public static void writeXml(Node node, File f, String encoding, boolean writeSave) throws IOException, TransformerException {
		OutputStream out = null;

		ensureParentFolderExists(f);

		File saveFile = null;
		if (writeSave) {
			String guid = RandomTools.getRandom32CharactersHexString(true);
			saveFile = new File(f.getAbsoluteFile() + ".tmp." + guid);
			out = new FileOutputStream(saveFile);
		} else {
			out = new FileOutputStream(f);
		}

		try {
			writeXml(node, out, encoding);
		} finally {
			IOTools.closeCloseable(out, logger);
		}

		if (saveFile != null) {
			Files.move(saveFile.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static void ensureParentFolderExists(File f) throws IOException {
		File parentFolder = f.getAbsoluteFile().getParentFile();
		if (parentFolder != null) {
			FileTools.ensureDirectoryExists(parentFolder);
		}
	}

	public static void writeXml(Node node, OutputStream out, String encoding) throws TransformerException {
		try {
			Writer wr = new OutputStreamWriter(out, encoding);
			writeXml(node, wr, encoding);
			wr.close();
		} catch (IOException e) {
			throw new TransformerException(e);
		}
	}

	/**
	 * Same as {@link #writeXml(Node, OutputStream, String)} with the exception that it differentiates between
	 * transformer errors and I/O errors.
	 * 
	 * @param node
	 *            The node to be written
	 * @param out
	 *            The output stream where the marshalled result should be written to
	 * @param encoding
	 *            The encoding of the output
	 * @throws TransformerException
	 *             Thrown if the marshalling was not successful
	 * @throws IOException
	 *             Thrown in the event of an I/O error
	 */
	public static void writeXmlToOutputStream(Node node, OutputStream out, String encoding) throws TransformerException, IOException {
		try {
			Writer wr = new OutputStreamWriter(out, encoding);
			writeXml(node, wr, encoding);
			wr.close();
		} catch (IOException ioe) {
			throw ioe;
		} catch (Exception e) {
			throw new TransformerException(e);
		}
	}

	public static void writeXmlNoCloseFix(Node node, OutputStream out, String encoding) throws TransformerException {
		try {
			Writer wr = new OutputStreamWriter(out, encoding);
			writeXml(node, wr, encoding);
			wr.flush();
		} catch (IOException e) {
			throw new TransformerException(e);
		}
	}

	public static void writeXml(Node node, Writer out, String encoding) throws TransformerException {
		DOMSource domSource = new DOMSource(node);
		StreamResult streamResult = new StreamResult(out);

		// TODO: optionally use shared serializer...
		Transformer serializer;
		synchronized (poxTranformerFactory) {
			// DEACTIVATED: poxTranformerFactory.setAttribute("indent-number", new Integer(2));
			serializer = poxTranformerFactory.newTransformer();
		}

		// TODO: take properties from parameter
		serializer.setOutputProperty(OutputKeys.ENCODING, encoding);
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");

		serializer.transform(domSource, streamResult);
	}

	public static String escape(String s) {
		/* //NOTE: quite slow s = s.replaceAll("&","&amp;"); s = s.replaceAll("<","&lt;"); s = s.replaceAll(">","&gt;");
		 * s = s.replaceAll("\"","&quot;"); s = s.replaceAll("'","&apos;"); return s; */

		int c = s.length();
		if (c == 0)
			return s;

		char[] chars = new char[c];
		s.getChars(0, c, chars, 0);

		StringBuilder b = new StringBuilder(c * 2);

		for (int i = 0; i < chars.length; i++) {
			char ch = chars[i];

			switch (ch) {
				case '&':
					b.append("&amp;");
					break;
				case '<':
					b.append("&lt;");
					break;
				case '>':
					b.append("&gt;");
					break;
				case '"':
					b.append("&quot;");
					break;
				case '\'':
					b.append("&apos;");
					break;
				default:
					b.append(ch);
			}
		}

		return b.toString();
	}

	public static List<String> getAttributeNames(Element element) {
		NamedNodeMap attribs = element.getAttributes();
		int c = attribs.getLength();
		List<String> names = new ArrayList<String>(c);

		for (int i = 0; i < c; i++) {
			Attr attr = (Attr) attribs.item(i);
			String attrib = attr.getName();
			names.add(attrib);
		}

		return names;
	}

	public static Map<String, String> getAttributes(Element element) {
		NamedNodeMap attribs = element.getAttributes();
		int c = attribs.getLength();

		Map<String, String> values = new HashMap<String, String>(c);

		for (int i = 0; i < c; i++) {
			Attr attr = (Attr) attribs.item(i);
			String attrib = attr.getName();
			String value = attr.getValue();

			values.put(attrib, value);
		}

		return values;
	}

	public static void main(String[] args) {
		String urlText = "jar:file:C:/Documents%20and%20Settings/robert.grasmugg.BRAINTRIBE/Application%2520Data/Sun/Java/Deployment/cache/javaws/http/Ddmssrv3/P8081/DMoeamtc-install/DMapp/java-XMCspWorkflowClient-launch-oeamtc.jar42824tmp!/custom.cfg.xml";
		try {
			URL url = new URL(urlText);
			URI uri = null;

			uri = new URI(url.getProtocol(), url.getPath(), url.getRef());
			System.out.println(uri);

			uri = url.toURI();

			System.out.println(uri);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static XPath createNamespaceAwareXpath(Map<String, String> namespaces) {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath localXpath = xPathfactory.newXPath();

		if ((namespaces != null) && (namespaces.size() > 0)) {
			MapBasedNamespaceContext nsContext = new MapBasedNamespaceContext(namespaces);
			localXpath.setNamespaceContext(nsContext);
		}
		return localXpath;
	}

	public static String evaluateXPathToString(Node node, String xpathSpec, Map<String, String> namespaces) {
		try {
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath localXpath = xPathfactory.newXPath();

			if ((namespaces != null) && (namespaces.size() > 0)) {
				MapBasedNamespaceContext nsContext = new MapBasedNamespaceContext(namespaces);
				localXpath.setNamespaceContext(nsContext);
			}

			XPathExpression expr = localXpath.compile(xpathSpec);
			String result = (String) expr.evaluate(node, XPathConstants.STRING);

			if (logger.isTraceEnabled()) {
				logger.trace("The xpath " + xpathSpec + " produced " + result);
			}

			return result;
		} catch (Exception e) {
			logger.error("Could not get the xpath " + xpathSpec, e);
			return "";
		}
	}
}
