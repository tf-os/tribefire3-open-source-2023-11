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
package com.braintribe.utils.xml.dom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * xpath utilities with support for namespaces
 * 
 * @author Pit
 * 
 */
public class XPathUtils {

	private static XPathFactory xpathFactory = XPathFactory.newInstance();
	private static XPath xpath = xpathFactory.newXPath();

	/**
	 * evaluates an xpath and returns the resulting NodeList
	 * 
	 * @param parent
	 *            the parent element to scan from
	 * @param expression
	 *            the xpath expression
	 * @param namespaces
	 *            a Map<prefix,schemalocation> for the NamespaceContext
	 * @return the result as NodeList
	 * @throws DomUtilsException
	 */
	public static NodeList evaluateXPathExpression(final Element parent, final String expression,
			final Map<String, String> namespaces) throws DomUtilsException {
		final XPathNamespaceContextContainer namespaceContext = new XPathNamespaceContextContainer(namespaces);
		xpath.setNamespaceContext(namespaceContext);

		try {
			final XPathExpression xpathExpression = xpath.compile(expression);
			final NodeList nodes = (NodeList) xpathExpression.evaluate(parent, XPathConstants.NODESET);
			return nodes;
		} catch (final XPathExpressionException e) {
			throw new DomUtilsException("cannot evaluate XPath expression '" + expression + "'", e);
		}
	}

	/**
	 * same as above, but a list of w3c.dom.Element is returned
	 * 
	 * @param parent
	 * @param expression
	 * @param namespaces
	 * @return
	 * @throws DomUtilsException
	 */
	public static List<Element> evaluateXPathElementExpression(final Element parent, final String expression,
			final Map<String, String> namespaces) throws DomUtilsException {
		final NodeList nodes = evaluateXPathExpression(parent, expression, namespaces);
		final List<Element> elements = new ArrayList<Element>();
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				elements.add((Element) nodes.item(i));
			}
		}
		return elements;
	}

	/**
	 * same as aboive, but only ONE w3c.dom.Element is returned
	 * 
	 * @param parent
	 * @param expression
	 * @param namespaces
	 * @return
	 * @throws DomUtilsException
	 */
	public static Element evaluateSingleXPathElementExpression(final Element parent, final String expression,
			final Map<String, String> namespaces) throws DomUtilsException {
		final NodeList nodes = evaluateXPathExpression(parent, expression, namespaces);
		if (nodes == null) {
			throw new DomUtilsException("xpath expression '" + expression + "' yields no results");
		}
		if (nodes.getLength() != 1) {
			throw new DomUtilsException("xpath expression '" + expression + "' doesn't result in one result");
		}
		final Node node = nodes.item(0);
		if (node.getNodeType() != Node.ELEMENT_NODE) {
			throw new DomUtilsException("xpatch expression '" + expression + "' doesn't yield an w3c.dom.Element");
		}
		return (Element) node;
	}

}
