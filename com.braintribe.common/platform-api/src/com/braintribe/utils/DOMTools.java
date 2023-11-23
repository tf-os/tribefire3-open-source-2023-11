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
package com.braintribe.utils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import com.braintribe.common.lcd.DOMException;
import com.braintribe.common.lcd.GenericRuntimeException;
import com.braintribe.utils.lcd.Arguments;
import com.braintribe.utils.lcd.Not;

/**
 * Provides DOM related utility methods. Please note that there is also a separate XMLUtils artifact with much more utility classes. This class only
 * provides some basic methods that don't have any special dependencies. Furthermore the methods of this class almost always only throw unchecked
 * exceptions.
 *
 * @author michael.lafite
 */
public class DOMTools {

	public static final String XML_DECLARATION_REGEX = "<\\?xml version=[^>]+\\?>";
	private static final String XML_DECLARATION_ATTRIBUTE__VERSION = "version";
	private static final String XML_DECLARATION_ATTRIBUTE__ENCODING = "encoding";
	private static final String XML_DECLARATION_ATTRIBUTE__STANDALONE = "standalone";

	private static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();
	private static final XPath XPATH = XPATH_FACTORY.newXPath();

	protected DOMTools() {
		// nothing to do
	}

	/**
	 * Returns the first {@link Element} that matches the given tagName
	 *
	 * @param parent
	 *            {@link Element} whose children are to be examined
	 * @param tagName
	 *            the tag name to be matched
	 * @return the first {@link Element} with the given tagName or null if none was found
	 */
	public static Element getFirstElement(final Element parent, final String tagName) {
		Node childNode = parent.getFirstChild();
		while (childNode != null) {
			if (childNode instanceof Element && (tagName == null || childNode.getNodeName().equals(tagName))) {
				return (Element) childNode;
			}
			childNode = childNode.getNextSibling();
		}
		return null;
	}

	/**
	 * works like {@link #getFirstElement(Element, String)} but recursively
	 *
	 * @param parent
	 *            the root {@link Element} with which the search starts
	 * @param tagNames
	 *            the list of tag names that should match on the levels
	 * @return the found {@link Element} otherwise null
	 */
	public static Element getElementByElementNames(final Element parent, final String... tagNames) {
		Element element = parent;
		for (final String tagName : tagNames) {
			element = getFirstElement(element, tagName);
			if (element == null) {
				break;
			}
		}

		return element;
	}

	/**
	 * works like {@link #getElementByElementNames(Element, String...)} but is taking the names from a path formatted string (e.g. "foo/bar/fix/foxy")
	 *
	 * @param parent
	 *            the root {@link Element} with which the search starts
	 * @param path
	 *            the '/' delimited path string
	 * @return the found {@link Element} otherwise null
	 */
	public static Element getElementByPath(final Element parent, final String path) {
		final StringTokenizer tokenizer = new StringTokenizer(path, "/");

		Element element = parent;
		while (tokenizer.hasMoreTokens()) {
			final String tagName = tokenizer.nextToken();
			element = getFirstElement(element, tagName);
			if (element == null) {
				break;
			}
		}

		return element;
	}

	/**
	 * Returns the <code>Element</code>s matching the specified <code>xpath</code> (or an empty list, if there is no match).
	 */
	public static List<Element> getElementsByXPath(final Element element, final String xpath) {
		try {
			return toJavaListOfElements((NodeList) XPATH.evaluate(xpath, element, XPathConstants.NODESET));
		} catch (final XPathExpressionException e) {
			throw new DOMException("Error while evaluating XPath!", e);
		}
	}

	/**
	 * Delegates to {@link #getElementByXPath(Element, String)}.
	 */
	public static Element getElementByXPath(final Document document, final String xpath) {
		return getElementByXPath(Not.Null(document.getDocumentElement()), xpath);
	}

	/**
	 * Returns the <code>Element</code> matching the specified <code>xpath</code> (or <code>null</code>, if there is no match).
	 */
	public static Element getElementByXPath(final Element element, final String xpath) {
		try {
			return (Element) XPATH.evaluate(xpath, element, XPathConstants.NODE);
		} catch (final XPathExpressionException e) {
			throw new DOMException("Error while evaluating XPath!", e);
		}
	}

	/**
	 * Delegates to {@link #getExistingElementByXPath(Element, String)}.
	 */
	public static Element getExistingElementByXPath(final Document document, final String xpath) {
		return getExistingElementByXPath(Not.Null(document.getDocumentElement()), xpath);
	}

	/**
	 * Same as {@link #getElementByXPath(Element, String)} but throws an exception if there is no match.
	 *
	 * @throws IllegalArgumentException
	 *             if there is no match.
	 */
	public static Element getExistingElementByXPath(final Element element, final String xpath) throws IllegalArgumentException {
		final Element result = getElementByXPath(element, xpath);
		if (result == null) {
			throw new IllegalArgumentException("No child element matches the specified xpath! "
					+ CommonTools.getParametersString("xpath", xpath, "element", toFormattedString(element)));
		}
		return result;
	}

	/**
	 * {@link #newDocumentBuilder(boolean) Creates a new DocumentBuilder} that is not {@link DocumentBuilderFactory#setNamespaceAware(boolean)
	 * namespace aware}.
	 */
	public static DocumentBuilder newDocumentBuilder() {
		return newDocumentBuilder(false);
	}

	/**
	 * Creates a new {@link DocumentBuilder}.
	 *
	 * @param namespaceAware
	 *            see {@link DocumentBuilderFactory#setNamespaceAware(boolean)}
	 */
	public static DocumentBuilder newDocumentBuilder(boolean namespaceAware) {

		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(namespaceAware);
			return Not.Null(documentBuilderFactory.newDocumentBuilder());
		} catch (final ParserConfigurationException e) {
			throw new GenericRuntimeException("Error while creating new " + DocumentBuilder.class.getSimpleName() + "!", e);
		}
	}

	/**
	 * Same as {@link #parse(String)}.
	 */
	public static Document stringToDocument(final String xmlString) throws DOMException, UncheckedIOException {
		return parse(xmlString);
	}

	/**
	 * {@link #parse(String, boolean) Parses} the passed <code>xmlString</code> (not namespace aware) and returns a {@link Document}.
	 */
	public static Document parse(final String xmlString) throws DOMException, UncheckedIOException {
		return parse(xmlString, false);
	}

	/**
	 * Parses the passed <code>xmlString</code> and returns a {@link Document}.
	 *
	 * @param namespaceAware
	 *            see {@link #newDocumentBuilder(boolean)}
	 */
	public static Document parse(final String xmlString, boolean namespaceAware) throws DOMException, UncheckedIOException {
		try {
			return Not.Null(newDocumentBuilder(namespaceAware).parse(StringTools.toInputSource(xmlString)));
		} catch (final SAXException e) {
			throw new DOMException("Error while parsing document from string '" + xmlString + "'!", e);
		} catch (final IOException e) {
			throw new UncheckedIOException("Error while parsing document from string '" + xmlString + "'!", e);
		}
	}

	/**
	 * Removes the passed <code>node</code> from it's parent node (if there is one).
	 */
	public static void remove(final Node node) {
		final Node parentNode = node.getParentNode();
		if (parentNode != null) {
			parentNode.removeChild(node);
		}
	}

	/**
	 * Works like {@link #insertBefore(Document, String, Document, Element, boolean)} but also gets the by target element by XPath
	 * (<code>targetElementXPath</code>).
	 */
	public static void insertBefore(final Document sourceDocument, final String sourceElementsXPath, final Document targetDocument,
			final String targetElementXPath, final boolean replaceEnabled) {
		final Element targetElement = getExistingElementByXPath(targetDocument, targetElementXPath);
		insertBefore(sourceDocument, sourceElementsXPath, targetDocument, targetElement, replaceEnabled);
	}

	/**
	 * Inserts the elements from the <code>sourceDocument</code> matching the <code>sourceElementsXPath</code> into the <code>targetDocument</code>
	 * before the <code>targetElement</code>. The target element is optionally replaced. The <code>sourceDocument</code> is not modified!
	 */
	public static void insertBefore(final Document sourceDocument, final String sourceElementsXPath, final Document targetDocument,
			final Element targetElement, final boolean replaceEnabled) {
		final List<Element> elementsToInsert = getElementsByXPath(Not.Null(sourceDocument.getDocumentElement()), sourceElementsXPath);

		for (final Element elementToInsert : elementsToInsert) {
			final Element importedElementToInsert = (Element) targetDocument.importNode(elementToInsert, true);
			targetElement.getParentNode().insertBefore(importedElementToInsert, targetElement);
		}

		if (replaceEnabled) {
			remove(targetElement);
		}
	}

	/**
	 * Returns a new {@link DOMImplementationRegistry}.
	 */
	private static DOMImplementationRegistry getNewDOMImplementationRegistry() {
		try {
			return Not.Null(DOMImplementationRegistry.newInstance());
		} catch (final Exception e) {
			throw new DOMException("Error while getting " + DOMImplementationRegistry.class.getSimpleName() + "!", e);
		}
	}

	/**
	 * Returns a formatted, "pretty printed" string representation of the passed <code>node</code> (without XML declaration).
	 * <p>
	 * The current implementation is based on {@link DOMImplementationLS} and {@link LSSerializer}. Formatting isn't perfect. For example, comments
	 * before the root element will be printed after the root element and sometimes there are (seemingly random) newlines.<br>
	 * Note that this may be improved in the future, which means one must not rely on the formatted string to look 100% the same in future versions!
	 * <p>
	 * Update: the issue with the comment before the root element being moved to after the root element was in Java 8 (reproduced with openjdk version
	 * 1.8.0_332 as well as Zulu package jdk-8.0.322-zulu8.60.0.21), but it's fixed in later versions and was also not reproducible with Oracle JDK
	 * 1.8.0_271.
	 *
	 * @see #toFormattedString_alternative(Node)
	 */
	public static String toFormattedString(final Node node) {
		final DOMImplementationLS impl = (DOMImplementationLS) getNewDOMImplementationRegistry().getDOMImplementation("LS");

		final LSSerializer writer = impl.createLSSerializer();
		// see https://xerces.apache.org/xerces2-j/javadocs/api/org/w3c/dom/ls/LSSerializer.html#getDomConfig()
		writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
		writer.getDomConfig().setParameter("xml-declaration", Boolean.FALSE);
		writer.getDomConfig().setParameter("comments", Boolean.TRUE);

		String result = Not.Null(writer.writeToString(node));
		return Not.Null(result);
	}

	/**
	 * Returns a string representation of the passed <code>node</code> (without XML declaration). This method does not format the xml string (e.g.
	 * indentation), but certain things such as attributes order will be changed nevertheless.
	 *
	 * @see DOMTools#toFormattedString(Node)
	 */
	public static String toString(final Node node) {
		return toString(node, false);
	}

	/**
	 * Returns a formatted string representation of the passed <code>node</code> (without XML declaration). This alternative implementation to
	 * {@link #toFormattedString(Node)} is based on {@link Transformer} (like {@link #toString(Node)}). Similar to {@link #toFormattedString(Node)}
	 * formatting may be improved in future versions.
	 */
	public static String toFormattedString_alternative(final Node node) {
		return toString(node, true);
	}

	/**
	 * Returns a formatted string representation of the passed <code>node</code> (without XML declaration), optionally formatting it.
	 */
	private static String toString(final Node node, boolean formattingEnabled) {
		final TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (final TransformerConfigurationException e) {
			throw new DOMException("Error while creating transformer!", e);
		}
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		if (formattingEnabled) {
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		}

		final StringWriter stringWriter = new StringWriter();
		try {
			transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
		} catch (final TransformerException e) {
			throw new DOMException("Error while transforming DOM node!", e);
		}

		final String result = stringWriter.toString();
		return result;
	}

	/**
	 * Same as {@link #toFormattedString(Node)}.
	 */
	public static String format(final Node node) {
		return toFormattedString(node);
	}

	/**
	 * {@link #stringToDocument(String) Converts} the passed <code>xmlString</code> into a {@link Document} and then {@link #format(Node)}s it.
	 */
	public static String format(final String xmlString) {
		return format(DOMTools.stringToDocument(xmlString));
	}

	/**
	 * {@link #stringToDocument(String) Converts} the passed <code>xmlString</code> into a {@link Document} (being namespace-aware) and then
	 * {@link #format(Node)}s it.
	 */
	public static String format(final String xmlString, boolean nameSpaceAware) {
		return format(DOMTools.parse(xmlString, nameSpaceAware));
	}

	/**
	 * Removes the XML declaration prefix (if any).
	 */
	public static String removeXmlDeclarationPrefix(String xmlString) {
		String result;
		if (xmlString.matches("(?s)" + XML_DECLARATION_REGEX + ".*")) {
			result = xmlString.replaceFirst(XML_DECLARATION_REGEX + "\\s*", "");
		} else {
			result = xmlString;
		}
		return result;
	}

	/**
	 * Converts the passed <code>nodeList</code> to a Java {@link List} of {@link Node}s.
	 */
	public static List<Node> toJavaListOfNodes(final NodeList nodeList) {
		final List<Node> nodes = new ArrayList<>();
		if (nodeList != null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				nodes.add(nodeList.item(i));
			}
		}
		return nodes;
	}

	/**
	 * Converts the passed <code>nodeList</code> to a Java {@link List} of {@link Element}s.
	 */
	public static List<Element> toJavaListOfElements(final NodeList nodeList) {
		return CollectionTools.getParameterizedList(Element.class, toJavaListOfNodes(nodeList));
	}

	/**
	 * Adds the specified child element to the passed <code>parent</code> node.
	 */
	public static Element addChildElement(final Node parent, final String tagName) {
		final Document document;
		if (parent instanceof Document) {
			document = (Document) parent;
		} else {
			document = parent.getOwnerDocument();
			Arguments.notNull(document, "The passed parent node has no owner document!");
		}

		final Element element = document.createElement(tagName);
		parent.appendChild(element);
		return element;
	}

	/**
	 * {@link #encode(String, int[]) Encodes} characters <code>&, <, >, ", =, '</code>.
	 */
	public static String encode(final String text) {
		return encode(text, new int[] { 38, 60, 62, 34, 61, 39 });
	}

	/**
	 * {@link #encode(String, int[]) Encodes} German special characters
	 */
	public static String encodeGermanSpecialCharacters(final String text) {
		return encode(text, new int[] { 196, 214, 220, 223, 228, 246, 252 });
	}

	/**
	 * Replaces the <code>charactersToEncode</code> with <code>&#</code> + [number] + <code>;</code>.
	 */
	public static String encode(String text, final int[] charactersToEncode) {
		if (StringTools.isEmpty(text)) {
			return text;
		}
		for (int element : charactersToEncode) {
			text = text.replace(String.valueOf((char) element), "&#" + element + ";");
		}
		return text;
	}

	/**
	 * Returns the XML declaration prefix, if the <code>xmlString</code> starts with the declaration, otherwise <code>null</code>.
	 */
	public static String getDeclaration(String xmlString) {
		return StringTools.getSubstringByRegex(xmlString, "^" + XML_DECLARATION_REGEX);
	}

	/**
	 * Parses the version from the <code>declaration</code>.
	 *
	 * @throws IllegalArgumentException
	 *             if the <code>declaration</code> contains no version attribute.
	 */
	public static String parseVersionFromDeclaration(String declaration) throws IllegalArgumentException {
		return parseAttributeValueFromDeclaration(declaration, XML_DECLARATION_ATTRIBUTE__VERSION, true);
	}

	/**
	 * Parses the encoding from the <code>declaration</code>.
	 */
	public static String parseEncodingFromDeclaration(String declaration) {
		return parseAttributeValueFromDeclaration(declaration, XML_DECLARATION_ATTRIBUTE__ENCODING, false);
	}

	/**
	 * Parses the version from the value of attribute <code>standalone</code>.
	 *
	 * @throws IllegalArgumentException
	 *             if the attribute is set, but the value is something else than <code>yes</code> or <code>no</code>.
	 */
	public static Boolean parseStandaloneFromDeclaration(String declaration) {
		String valueAsString = parseAttributeValueFromDeclaration(declaration, XML_DECLARATION_ATTRIBUTE__STANDALONE, false);
		Boolean result = null;
		if (valueAsString != null) {
			if (valueAsString.equals("no")) {
				result = false;
			} else if (valueAsString.equals("yes")) {
				result = true;
			} else {
				throw new IllegalArgumentException(
						"Invalid value for attribute " + XML_DECLARATION_ATTRIBUTE__STANDALONE + ": '" + valueAsString + "'");
			}
		}
		return result;
	}

	/**
	 * Parses the specified <code>attribute</code> value from the XML <code>declaration</code>.
	 *
	 * @throws IllegalArgumentException
	 *             if the attribute is mandatory, but not set.
	 */
	private static String parseAttributeValueFromDeclaration(String declaration, String attribute, boolean attributeIsMandatory)
			throws IllegalArgumentException {
		String attributeNameAndValue = StringTools.getSubstringByRegex(declaration, attribute + "=\\\"[^\\\"]*\\\"");
		String result = null;
		if (attributeNameAndValue == null) {
			if (attributeIsMandatory) {
				throw new IllegalArgumentException("The passed XML declaration doesn't contain attribute '" + attribute + "': '" + declaration + "'");
			}
		} else {
			result = StringTools.getSubstringBetween(attributeNameAndValue, "\"", "\"");
		}
		return result;
	}

	/**
	 * Creates an XML declaration string based on the specified arguments.
	 *
	 * @param version
	 *            the (mandatory) version
	 * @param encoding
	 *            the (optional) encoding
	 * @param standalone
	 *            the (optional) standalone value (which will be transformed to "yes" or "no", if set).
	 * @return the XML declaration.
	 * @throws IllegalArgumentException
	 *             if the <code>version</code> is <code>null</code>.
	 */
	public static String createDeclaration(String version, String encoding, Boolean standalone) {
		Arguments.notNullWithNames("version", version);
		StringBuilder builder = new StringBuilder();
		builder.append("<?xml " + XML_DECLARATION_ATTRIBUTE__VERSION + "=\"" + version + "\"");
		if (encoding != null) {
			builder.append(" " + XML_DECLARATION_ATTRIBUTE__ENCODING + "=\"" + encoding + "\"");
		}

		if (standalone != null) {
			builder.append(" " + XML_DECLARATION_ATTRIBUTE__STANDALONE + "=\"" + (standalone ? "yes" : "no") + "\"");
		}

		builder.append("?>");

		String result = builder.toString();
		return result;
	}

	/**
	 * Returns the {@link #XML_DECLARATION_ATTRIBUTE__ENCODING} specified in the XML declaration or <code>null</code>, if there is no declaration or
	 * no encoding.
	 */
	public static String parseEncodingFromDeclaration(File xmlFile) {
		// read first line
		String firstLine = FileTools.readFirstLineFromFile(xmlFile);
		String declaration = DOMTools.getDeclaration(firstLine);
		String encoding = null;

		if (declaration != null) {
			encoding = DOMTools.parseEncodingFromDeclaration(declaration);
		}
		return encoding;
	}
}
