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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.braintribe.utils.xml.dom.iterator.ElementIterator;

/**
 * a set of utility functions for DOM manipulation
 * 
 * @author Pit
 * 
 */
public class DomUtils {
	public static final Pattern pathSplitPattern = Pattern.compile("/");
	public static final XPathFactory xpathFactory = XPathFactory.newInstance();
	public static final XPath xpath = xpathFactory.newXPath();

	/**
	 * returns the first child-element of the passed element that has the matching tag name, matching attribute name and
	 * matching attriute value
	 * 
	 * @param parent
	 *            element to access the first matching type from
	 * @param tagName
	 *            name the tag name to match
	 * @param attributeName
	 *            the attribute name to match
	 * @param attributeValue
	 *            the attribute value to match
	 * @return element that matches
	 */
	public static Element getFirstElement(final Element parent, final String tagName, final String attributeName,
			final String attributeValue) {
		Node childNode = parent.getFirstChild();
		while (childNode != null) {
			if (childNode instanceof Element && childNode.getNodeName().matches(tagName)) {
				final Element childElement = (Element) childNode;
				if (childElement.getAttribute(attributeName).matches(attributeValue)) {
					return childElement;
				}
			}
			childNode = childNode.getNextSibling();
		}
		return null;
	}

	/**
	 * returns the first child element with a matching tag and matching attributes names & attribute values
	 * 
	 * @param parent
	 * @param tagName
	 * @param attributeNames
	 * @param attributeValues
	 * @return
	 */
	public static Element getFirstElement(final Element parent, final String tagName,
			final Map<String, String> attributes) {
		Node childNode = parent.getFirstChild();
		while (childNode != null) {
			if (childNode instanceof Element && childNode.getNodeName().matches(tagName)) {
				final Element childElement = (Element) childNode;

				boolean match = true;
				for (final Map.Entry<String, String> entry : attributes.entrySet()) {
					if (!childElement.getAttribute(entry.getKey()).matches(entry.getValue())) {
						match = false;
					}
				}
				if (match) {
					return childElement;
				}
			}
			childNode = childNode.getNextSibling();
		}
		return null;
	}

	/**
	 * returns the first element below the parent
	 * 
	 * @param parent
	 *            the element we want the child of
	 * @return the child-element if any
	 */
	public static Element getFirstElement(final Element parent) {
		return getFirstElement(parent, null);
	}

	/**
	 * returns the first element with the matching element of the childs of passed element
	 * 
	 * @param parent
	 *            the parent element
	 * @param tagName
	 *            the tag of the requested element or null if first element is wanted
	 * @return the first matching element
	 */
	public static Element getFirstElement(final Element parent, final String tagName) {
		Node childNode = parent.getFirstChild();
		while (childNode != null) {
			if (childNode instanceof Element && (tagName == null || childNode.getNodeName().matches(tagName))) {
				return (Element) childNode;
			}
			childNode = childNode.getNextSibling();
		}
		return null;
	}

	public static Element getNextElement(final Element predecessor) {
		Node node = predecessor.getNextSibling();
		while (node != null) {
			if (node instanceof Element) {
				return (Element) node;
			}
			node = node.getNextSibling();
		}
		return null;

	}

	/**
	 * returns the next element in sequence of the passed element (again with specified tag)
	 * 
	 * @param predecessor
	 *            the element we want the next of
	 * @param tagName
	 *            the tag of the passed element or null if next element's wanted
	 * @return
	 */
	public static Element getNextElement(final Element predecessor, final String tagName) {
		Node node = predecessor.getNextSibling();
		while (node != null) {
			if (node instanceof Element && (tagName == null || node.getNodeName().matches(tagName))) {
				return (Element) node;
			}
			node = node.getNextSibling();
		}
		return null;
	}

	/**
	 * returns an iterator for iterating over a set of elements
	 * 
	 * @param parent
	 *            the parent we want the children of
	 * @param tagName
	 *            the tag-name of children we want or null for all
	 * @return an iterator
	 */
	public static Iterator<Element> getElementIterator(final Element parent, final String tagName) {
		return new ElementIterator(parent, tagName);
	}

	public static Element getElementByPath(final Element parent, final String path, final boolean create) {
		if (path.length() == 0) {
			return parent;
		}
		final String tagNames[] = pathSplitPattern.split(path);
		Element element = parent;
		for (String tagName : tagNames) {
			final int pos = tagName.indexOf("[");
			if (pos < 0) {
				element = findElementByPath(element, tagName, create);
			} else {
				final int pos2 = tagName.indexOf("]");
				final String expr = tagName.substring(pos + 1, pos2);
				final String[] axpr = expr.split("=");
				final String attr_name = axpr[0];
				final String attr_value = axpr[1];
				tagName = tagName.substring(0, pos);
				element = findElementByPath(element, tagName, attr_name, attr_value);
			}
			if (element == null) {
				return element;
			}
		}
		return element;
	}

	/**
	 * @param parent
	 * @param path
	 * @return
	 */
	public static Element findElementByPath(final Element parent, final String path, final boolean create) {
		if (path.length() == 0) {
			return parent;
		}
		final String tagNames[] = pathSplitPattern.split(path);
		Element element = parent;
		for (final String tagName : tagNames) {
			Element suspect = getFirstElement(element, tagName);
			if (suspect == null) {
				if (create == false) {
					return null;
				}
				// create element on the fly..
				suspect = parent.getOwnerDocument().createElement(tagName);
				element.appendChild(suspect);
			}
			element = suspect;
		}

		return element;
	}

	/**
	 * @param parent
	 * @param path
	 * @param attributeName
	 * @param attributeValue
	 * @return
	 */
	public static Element findElementByPath(final Element parent, final String path, final String attributeName,
			final String attributeValue) {
		if (path.length() == 0) {
			return parent;
		}
		final String tagNames[] = pathSplitPattern.split(path);
		Element element = parent;
		int index = 0;
		for (final String tagName : tagNames) {
			final boolean last = index == tagNames.length - 1;
			if (last) {
				element = getFirstElement(element, tagName, attributeName, attributeValue);
			} else {
				element = getFirstElement(element, tagName);
			}
			if (element == null) {
				break;
			}
			index++;
		}

		return element;
	}

	/**
	 * gets the attribute or text content of a node by element path path is "element[@attribute]"
	 * 
	 * @param parent
	 *            - the parent element to find the child of
	 * @param path
	 *            - the path to follow
	 * @param create
	 *            - whether to create the path if not present
	 * @return - the string value found
	 */
	public static String getElementValueByPath(final Element parent, final String path, final boolean create) {
		final String[] paths = path.split("@");
		final Element suspect = getElementByPath(parent, paths[0], create);
		if (suspect == null) {
			return null;
		}
		if (paths.length > 1) {
			return suspect.getAttribute(paths[1]);
		} else {
			return suspect.getTextContent();
		}
	}

	/**
	 * gets the attribute or text content of a node by element path path is "element[@attribute]"
	 * 
	 * @param parent
	 *            - the parent element to find the child of
	 * @param path
	 *            - the path to follow
	 * @param attribute_name
	 *            - attribute name to check
	 * @param attribute_value
	 *            - value of the attribute to check
	 * @param create
	 *            - whether to create the path if not present
	 * @return - the string value found
	 */
	public static String getElementValueByPath(final Element parent, final String path, final String attribute_name,
		final String attribute_value, final boolean create) {
		final String[] paths = path.split("@");
		final Element suspect = findElementByPath(parent, paths[0], attribute_name, attribute_value);
		if (suspect == null) {
			return null;
		}
		if (paths.length > 1) {
			return suspect.getAttribute(paths[1]);
		} else {
			return suspect.getTextContent();
		}
	}

	public static Element getElementByPath(final Element parent, final String path, final String attribute_name,
			final String attribute_value, final boolean create) {
		final Element suspect = findElementByPath(parent, path, attribute_name, attribute_value);
		if (suspect == null) {
			return null;
		}
		return suspect;
}

	/**
	 * sets the attribute or text node of a node by element path
	 * 
	 * @param parent
	 *            - the parent element to find the child of
	 * @param path
	 *            - the path to follow
	 * @param value
	 *            - the string value to set
	 * @param create
	 *            - whether to create the path if not present
	 * @return - true if it worked (always true if create's true)
	 */
	public static boolean setElementValueByPath(final Element parent, final String path, final String value,
			final boolean create) {
		final String[] paths = path.split("@");
		final Element suspect = getElementByPath(parent, paths[0], create);
		if (suspect == null) {
			return false;
		}
		if (paths.length > 1) {
			suspect.setAttribute(paths[1], value);
		} else {
			suspect.setTextContent(value);
		}
		return true;
	}

	/**
	 * sets the attribute or text node of a node by element path
	 * 
	 * @param parent
	 *            - the parent element to find the child of
	 * @param path
	 *            - the path to follow
	 * @param attribute_name
	 *            - name of the attribute to check
	 * @param attribute_value
	 *            - value of the attribute to check
	 * @param value
	 *            - the string value to set
	 * @param create
	 *            - whether to create the path if not present
	 * @return - true if it worked (always true if create's true)
	 */
	public static boolean setElementValueByPath(final Element parent, final String path, final String attribute_name,
			final String attribute_value, final String value) {
		final String[] paths = path.split("@");
		final Element suspect = findElementByPath(parent, paths[0], attribute_name, attribute_value);
		if (suspect == null) {
			return false;
		}
		if (paths.length > 1) {
			suspect.setAttribute(paths[1], value);
		} else {
			suspect.setTextContent(value);
		}
		return true;
	}

	/**
	 * searches for the parent element of a given node actually first the parent node that is of type ELEMENT_NODE
	 * 
	 * @param element
	 *            the child element to get the parent of
	 * @param tag
	 *            the name of the parent element to search
	 * @return the parent element
	 */
	public static Element getAncestor(final Element element, final String tag) {
		Node suspect = element;
		do {
			suspect = suspect.getParentNode();
			if (suspect == null) {
				return null;
			}
			if (suspect.getNodeType() == Node.ELEMENT_NODE) {
				final Element parent = (Element) suspect;
				if (parent.getTagName().matches(tag) == true) {
					return parent;
				}
			}
		} while (true);
	}

	/**
	 * returns a set of the names of the attribute nodes appended to an element
	 * 
	 * @param element
	 *            the element we want the attribute names of
	 * @return the set with attribute names
	 */
	public static Set<String> getAttributeNames(final Element element) {
		final NamedNodeMap attribs = element.getAttributes();
		final int c = attribs.getLength();
		final Set<String> names = new LinkedHashSet<String>(c);

		for (int i = 0; i < c; i++) {
			final Attr attr = (Attr) attribs.item(i);
			final String attrib = attr.getName();
			names.add(attrib);
		}

		return names;
	}

	/**
	 * builds a Map<String, String> of the attribute name and values
	 * 
	 * @param element
	 *            the element to extract the attribute nodes of
	 * @return the map of attribute names and values
	 */
	public static Map<String, String> getAttributes(final Element element) {
		final NamedNodeMap attribs = element.getAttributes();
		final int c = attribs.getLength();

		final Map<String, String> values = new HashMap<String, String>(c);

		for (int i = 0; i < c; i++) {
			final Attr attr = (Attr) attribs.item(i);
			final String attrib = attr.getName();
			final String value = attr.getValue();

			values.put(attrib, value);
		}

		return values;
	}

	/**
	 * returns the value of the attribute of the element or null if not found
	 * 
	 * @param e
	 *            the element we want an attribute value of
	 * @param name
	 *            the name of the attribute
	 * @return the value of the attribute or null if not found
	 */
	public static String getAttribute(final Element e, final String name) {
		return getAttribute(e, name, null);
	}

	/**
	 * returns the value of the attribute of the element or the default value if not found
	 * 
	 * @param e
	 *            the element we want an attribute value of
	 * @param name
	 *            name the name of the attribute
	 * @param def
	 *            the default value to return if the attribute isn't found
	 * @return the value of the attribute or the passed default value if not found
	 */
	public static String getAttribute(final Element e, final String name, final String def) {
		if (e.getAttributeNode(name) == null) {
			return def;
		}
		return e.getAttribute(name);
	}

	/**
	 * evaluates a xpath statement and returns the resulting node
	 * 
	 * @param element
	 *            the element from which the xpath should be evaluated
	 * @param path
	 *            the xpath expression
	 * @return the node the xpath resolves to
	 * @throws DomUtilsException
	 *             (invalid xpath, not a node as result)
	 */
	public static Element getElement(final Element element, final String path) throws DomUtilsException {

		Node n = null;
		try {
			n = (Node) xpath.evaluate(path, element, XPathConstants.NODE);
		} catch (final XPathExpressionException e) {
			throw new DomUtilsException("xpath '" + path + "' is not a valid xpath expression", e);
		}
		if (n == null) {
			return null;
		}

		if (!(n instanceof Node)) {
			throw new DomUtilsException(" xpath '" + path + "' does not result in an XML element (tag)");
		} else {
			return (Element) n;
		}

	}

	/**
	 * returns the XPath of the given node if the node has an ID or a NAME, it will use this as identification
	 * otherwise, it will use the position inside the parent element
	 * 
	 * @param n
	 *            - the node
	 * @return - a fully qualified XPath that leads to the node given.
	 */
	public static String getXPath(final Node n) {
		// abort early
		if (null == n) {
			return null;
		}

		// declarations
		Node parent = null;
		final Stack<Node> hierarchy = new Stack<Node>();
		final StringBuffer buffer = new StringBuffer();

		// push element on stack
		hierarchy.push(n);

		switch (n.getNodeType()) {
		case Node.ATTRIBUTE_NODE:
			parent = ((Attr) n).getOwnerElement();
			break;
		case Node.ELEMENT_NODE:
			parent = n.getParentNode();
			break;
		case Node.DOCUMENT_NODE:
			parent = n.getParentNode();
			break;
		default:
			throw new IllegalStateException("Unexpected Node type" + n.getNodeType());
		}

		while (null != parent && parent.getNodeType() != Node.DOCUMENT_NODE) {
			// push on stack
			hierarchy.push(parent);

			// get parent of parent
			parent = parent.getParentNode();
		}

		// construct xpath
		Object obj = null;
		while (!hierarchy.isEmpty() && null != (obj = hierarchy.pop())) {
			final Node node = (Node) obj;
			boolean handled = false;

			if (node.getNodeType() == Node.ELEMENT_NODE) {
				final Element e = (Element) node;

				// is this the root element?
				if (buffer.length() == 0) {
					// root element - simply append element name
					buffer.append(node.getNodeName());
				} else {
					// child element - append slash and element name
					buffer.append("/");
					buffer.append(node.getNodeName());

					if (node.hasAttributes()) {
						// see if the element has a name or id attribute
						if (e.hasAttribute("id")) {
							// id attribute found - use that
							buffer.append("[@id='" + e.getAttribute("id") + "']");
							handled = true;
						} else if (e.hasAttribute("name")) {
							// name attribute found - use that
							buffer.append("[@name='" + e.getAttribute("name") + "']");
							handled = true;
						}
					}

					if (!handled) {
						// no known attribute we could use - get sibling index
						int prev_siblings = 1;
						Node prev_sibling = node.getPreviousSibling();
						while (null != prev_sibling) {
							if (prev_sibling.getNodeType() == node.getNodeType()) {
								if (prev_sibling.getNodeName().equalsIgnoreCase(node.getNodeName())) {
									prev_siblings++;
								}
							}
							prev_sibling = prev_sibling.getPreviousSibling();
						}
						buffer.append("[" + prev_siblings + "]");
					}
				}
			} else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				buffer.append("/@");
				buffer.append(node.getNodeName());
			}
		}
		// return buffer
		return buffer.toString();
	}

}
