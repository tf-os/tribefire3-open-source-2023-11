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
package com.braintribe.common;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import com.braintribe.common.lcd.DOMException;
import com.braintribe.utils.DOMTools;

/**
 * Little helper whose purpose is to {@link #compare(Document, Document) compare} two {@link Document}s (or XML strings) and find the first
 * difference. This is an extension of {@link StringDiff} with further, optional DOM related pre-processing steps such as
 * {@link #setFormattingEnabled(boolean) formatting}.
 *
 * @author michael.lafite
 */
public class DomDiff extends StringDiff {

	public static final boolean DEFAULTS__FORMATTING_ENABLED = true;
	public static final boolean DEFAULTS__COMMENTS_INCLUDED = true;

	boolean formattingEnabled = DEFAULTS__FORMATTING_ENABLED;
	boolean commentsIncluded = DEFAULTS__COMMENTS_INCLUDED;

	public DomDiff() {
		formattingEnabled = DEFAULTS__FORMATTING_ENABLED;
		commentsIncluded = DEFAULTS__COMMENTS_INCLUDED;
	}

	public DomDiff(boolean formattingEnabled, boolean commentsIncluded) {
		this.formattingEnabled = formattingEnabled;
		this.commentsIncluded = commentsIncluded;
	}

	public boolean isFormattingEnabled() {
		return formattingEnabled;
	}

	/**
	 * Whether or not to format the XML strings to be compared. Default: {@value #DEFAULTS__FORMATTING_ENABLED}.<br>
	 * Note that some minimal formatting (e.g. attributes order or comments outside root element) may always be occur.
	 */
	public void setFormattingEnabled(boolean formattingEnabled) {
		this.formattingEnabled = formattingEnabled;
	}

	public boolean isCommentsIncluded() {
		return commentsIncluded;
	}

	/**
	 * Whether or not to include comments when creating the XML strings to be compared. Default: {@value #DEFAULTS__COMMENTS_INCLUDED}.
	 */
	public void setCommentsIncluded(boolean commentsIncluded) {
		this.commentsIncluded = commentsIncluded;
	}

	/**
	 * Creates {@link #toComparableXmlString(Node) string representation} of the passed documents and then {@link StringDiff#compare(String, String)
	 * compares} them.
	 */
	public DiffResult compare(Document first, Document second) {
		String firstAsString = toComparableXmlString(first);
		String secondAsString = toComparableXmlString(second);
		DiffResult result = super.compare(firstAsString, secondAsString);
		return result;
	}

	/**
	 * {@link DOMTools#parse(String) Parses} the passed XML strings and then {@link #compare(Document, Document) compares} them.
	 */
	@Override
	public DiffResult compare(String firstXmlString, String secondXmlString) {
		Document first = DOMTools.parse(firstXmlString);
		Document second = DOMTools.parse(secondXmlString);

		return compare(first, second);
	}

	/**
	 * {@link DOMTools#parse(String) Parses} the passed <code>xmlString</code> into a {@link Document} and then creates a
	 * {@link #toComparableXmlString(Node) comparable string}.
	 */
	public String toComparableXmlString(final String xmlString) {
		Document document = DOMTools.parse(xmlString);
		return toComparableXmlString(document);
	}

	/**
	 * Creates string representation of the passed <code>node</code> (optionally {@link #setFormattingEnabled(boolean) formatting} and/or removing
	 * {@link #setCommentsIncluded(boolean) comments}).
	 */
	public String toComparableXmlString(final Node node) {
		DOMImplementationRegistry domImplementationRegistry;
		try {
			domImplementationRegistry = DOMImplementationRegistry.newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException e) {
			throw new DOMException("Error while getting " + DOMImplementationRegistry.class.getSimpleName() + "!", e);
		}

		final DOMImplementationLS impl = (DOMImplementationLS) domImplementationRegistry.getDOMImplementation("LS");

		final LSSerializer writer = impl.createLSSerializer();
		// see https://xerces.apache.org/xerces2-j/javadocs/api/org/w3c/dom/ls/LSSerializer.html#getDomConfig()
		writer.getDomConfig().setParameter("xml-declaration", Boolean.FALSE);
		writer.getDomConfig().setParameter("format-pretty-print", formattingEnabled);
		writer.getDomConfig().setParameter("comments", commentsIncluded);

		String result = writer.writeToString(node);
		return result;
	}

}
