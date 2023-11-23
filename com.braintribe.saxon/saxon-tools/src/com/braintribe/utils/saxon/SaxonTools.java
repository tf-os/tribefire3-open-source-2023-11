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
package com.braintribe.utils.saxon;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

import com.braintribe.utils.CommonTools;
import com.braintribe.utils.DOMTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.NullSafe;

import net.sf.saxon.s9api.DOMDestination;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.Serializer.Property;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

/**
 * Provides <code>Saxon</code> related helpers and convenience methods.
 *
 * @author michael.lafite
 */
public class SaxonTools {

	private static final String XSLT__RESOURCES_PATH_PREFIX = "/" + SaxonTools.class.getPackage().getName().replace('.', '/') + "/xslt/";

	private static final String XSLT__REMOVE_WHITESPACE;

	// shared (thread-safe)
	private static Processor processor = new Processor(false);

	static {
		String resourcePath = XSLT__RESOURCES_PATH_PREFIX + "remove_whitespace.xslt";
		try {
			XSLT__REMOVE_WHITESPACE = new String(IOTools.inputStreamToByteArray(SaxonTools.class.getResourceAsStream(resourcePath)));
		} catch (IOException e) {
			throw new UncheckedIOException("Error while reading resource " + resourcePath + "!", e);
		}
	}

	private SaxonTools() {
		// no instantiation required
	}

	/**
	 * Performs a {@link File} based XSLT transformation. Delegates to {@link #applyXslt(File, File, File, Map)}.
	 *
	 * @see #applyXslt(Source, Source, Destination)
	 * @see #applyXslt(Document, Document)
	 */
	public static void applyXslt(File xml, File xslt, File destination) {
		applyXslt(xml, xslt, destination, null);
	}

	/**
	 * Performs a {@link File} based XSLT transformation. Delegates to
	 * {@link #applyXslt(Source, Source, Destination, Map)}.
	 *
	 * @see #applyXslt(Source, Source, Destination, Map)
	 * @see #applyXslt(Document, Document, Map)
	 */
	public static void applyXslt(File xml, File xslt, File destination, Map<String, Object> parameters) {
		if (!xslt.exists()) {
			throw new IllegalArgumentException("The specified xslt file " + xslt.getAbsolutePath() + " doesn't exist!");
		}
		applyXslt(xml, new StreamSource(xslt), destination, parameters);
	}

	/**
	 * Performs a {@link File} based XSLT transformation, but the <code>xslt</code> can be provided as any
	 * {@link Source}.
	 */
	private static void applyXslt(File xml, Source xslt, File destinationFile, Map<String, Object> parameters) {
		if (!xml.exists()) {
			throw new IllegalArgumentException("The specified xml file " + xml.getAbsolutePath() + " doesn't exist!");
		}

		if (destinationFile.exists() && destinationFile.length() > 0) {
			throw new IllegalArgumentException("The specified destination file " + destinationFile.getAbsolutePath()
					+ " already exists (and is not empty). Please specify a new file!");
		}

		Serializer destination = processor.newSerializer(destinationFile);

		applyXslt(new StreamSource(xml), xslt, destination, parameters);
	}

	/**
	 * Performs a {@link Document} based XSLT transformation. Delegates to {@link #applyXslt(Document, Document, Map)}.
	 *
	 * * @see #applyXslt(Source, Source, Destination)
	 *
	 * @see #applyXslt(File, File, File)
	 */
	public static DocumentFragment applyXslt(Document xml, Document xslt) {
		DocumentFragment resultDocumentFragment = DOMTools.newDocumentBuilder().newDocument().createDocumentFragment();

		applyXslt(new DOMSource(xml), new DOMSource(xslt), new DOMDestination(resultDocumentFragment));

		return resultDocumentFragment;
	}

	/**
	 * Performs a {@link Document} based XSLT transformation. Delegates to
	 * {@link #applyXslt(Source, Source, Destination, Map)}.
	 *
	 * @return a {@link DocumentFragment} (and not a {@link Document}, because the transformation result doesn't
	 *         necessarily have to be a valid XML (could e.g. also just be text).
	 * @see #applyXslt(Source, Source, Destination, Map)
	 * @see #applyXslt(File, File, File)
	 */
	public static DocumentFragment applyXslt(Document xml, Document xslt, Map<String, Object> parameters) {
		DocumentFragment resultDocumentFragment = DOMTools.newDocumentBuilder().newDocument().createDocumentFragment();

		applyXslt(new DOMSource(xml), new DOMSource(xslt), new DOMDestination(resultDocumentFragment), parameters);

		return resultDocumentFragment;
	}

	/**
	 * Applies an <code>xslt</code> to a <code>source</code> and writes the result to the <code>destination</code>.
	 * Delegates to {@link #applyXslt(Source, Source, Destination, Map)}.
	 */
	public static void applyXslt(Source xml, Source xslt, Destination destination) throws UncheckedSaxonApiException {
		applyXslt(xml, xslt, destination, null);
	}

	/**
	 * Applies an <code>xslt</code> to a <code>source</code> and writes the result to the <code>destination</code>. One
	 * may pass an optional map of <code>parameters</code>. Supported value types are {@link String}, {@link Integer},
	 * {@link Long}, {@link Float}, {@link Double}, {@link BigDecimal}, {@link Boolean} and {@link URI}.
	 */
	public static void applyXslt(Source xml, Source xslt, Destination destination, Map<String, Object> parameters) throws UncheckedSaxonApiException {

		XsltCompiler xsltCompiler = processor.newXsltCompiler();

		XsltExecutable xsltExecutable;
		try {
			xsltExecutable = xsltCompiler.compile(xslt);
		} catch (SaxonApiException e) {
			throw new UncheckedSaxonApiException("Error while compiling xslt " + xslt + "!", e);
		}

		Map<QName, XdmValue> convertedParameters = convertParameters(parameters);

		Xslt30Transformer xsltTransformer = xsltExecutable.load30();

		try {
			xsltTransformer.setStylesheetParameters(convertedParameters);
		} catch (SaxonApiException e) {
			throw new UncheckedSaxonApiException(
					"Error while setting stylesheet parameters!" + CommonTools.getParametersString("xslt", xslt, "parameters", parameters), e);
		}

		try {
			xsltTransformer.applyTemplates(xml, destination);
		} catch (SaxonApiException e) {
			throw new UncheckedSaxonApiException(
					"Error while applying templates!" + CommonTools.getParametersString("xml", xml, "xslt", xslt, "destination", destination), e);
		}
	}

	/**
	 * Converts a String-to-Object map of parameter names and values to a QName-to-XdmValue map (which is expected by
	 * {@link Xslt30Transformer}.
	 */
	private static Map<QName, XdmValue> convertParameters(Map<String, Object> parameters) {
		Map<QName, XdmValue> result = new HashMap<>();
		for (String paramName : NullSafe.keySet(parameters)) {
			Object paramValue = parameters.get(paramName);

			QName paramQName;
			try {
				paramQName = new QName(paramName);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Error while converting parameter named '" + paramName + "' with value " + CommonTools.getStringRepresentation(paramValue)
				+ ": cannot use param name as local name to instantiate QName instance!", e);
			}
			
			XdmAtomicValue paramXdmAtomicValue;
			if (paramValue instanceof String) {
				paramXdmAtomicValue = new XdmAtomicValue((String) paramValue);
			} else if (paramValue instanceof Integer) {
				paramXdmAtomicValue = new XdmAtomicValue((Integer) paramValue);
			} else if (paramValue instanceof Long) {
				paramXdmAtomicValue = new XdmAtomicValue((Long) paramValue);
			} else if (paramValue instanceof Float) {
				paramXdmAtomicValue = new XdmAtomicValue((Float) paramValue);
			} else if (paramValue instanceof Double) {
				paramXdmAtomicValue = new XdmAtomicValue((Double) paramValue);
			} else if (paramValue instanceof BigDecimal) {
				paramXdmAtomicValue = new XdmAtomicValue((BigDecimal) paramValue);
			} else if (paramValue instanceof Boolean) {
				paramXdmAtomicValue = new XdmAtomicValue((Boolean) paramValue);
			} else if (paramValue instanceof URI) {
				paramXdmAtomicValue = new XdmAtomicValue((URI) paramValue);
			} else {
				throw new IllegalArgumentException("Parameter '" + paramName + "' has value " + CommonTools.getStringRepresentation(paramValue)
						+ " of type " + NullSafe.className(paramValue) + " which is not supported.");
			}

			result.put(paramQName, paramXdmAtomicValue);
		}
		return result;
	}

	/**
	 * Helper class used to conveniently specify formatting options.
	 *
	 * @see SaxonTools#formatToString(String, FormattingOptions)
	 * @see SaxonTools#formatToFile(File, File, FormattingOptions)
	 *
	 * @author michael.lafite
	 */
	public static class FormattingOptions {
		private boolean omitXmlDeclaration;
		private Boolean standalone;
		private String method = "xml";

		/**
		 * Sets the output method, e.g. "xml", "html", "text". An important difference is that XML uses self-closing
		 * tags (<code>&lt;a/&gt;</code>) whereas HTML doesn't (<code>&lt;a&gt;&lt;/a&gt;</code>).
		 *
		 * See also {@link Property#METHOD}.
		 */
		public FormattingOptions method(String method) {
			this.method = method;
			return this;
		}

		public FormattingOptions omitXmlDeclaration(boolean omitXmlDeclaration) {
			this.omitXmlDeclaration = omitXmlDeclaration;
			return this;
		}

		public FormattingOptions standalone(Boolean standalone) {
			this.standalone = standalone;
			return this;
		}

		private Map<Property, String> getSerializerProperties() {
			Map<Property, String> result = new HashMap<>();

			result.put(Property.METHOD, method);
			result.put(Property.OMIT_XML_DECLARATION, omitXmlDeclaration ? "yes" : "no");

			String standaloneValue;
			if (standalone == null) {
				standaloneValue = "omit";
			} else if (standalone) {
				standaloneValue = "yes";
			} else {
				standaloneValue = "no";
			}
			result.put(Property.STANDALONE, standaloneValue);

			// not configurable for now
			result.put(Property.ENCODING, "UTF-8");
			result.put(Property.INDENT, "yes");

			// Special Saxon properties (e.g. indent-spaces, line length or attribute order) require PE edition

			return result;
		}
	}

	/**
	 * Formats the <code>xmlString</code> using the specified <code>serializerOptions</code>.<br>
	 * Note that special Saxon properties, e.g. {@link Property#SAXON_ATTRIBUTE_ORDER}, are unfortunately not supported
	 * in the free version of Saxon.
	 */
	public static String formatToString(String xmlString, Map<Property, String> serializerOptions) {
		Source xml = new StreamSource(new StringReader(xmlString));
		Source xslt = new StreamSource(new StringReader(XSLT__REMOVE_WHITESPACE));

		StringWriter stringWriter = new StringWriter();
		Serializer destination = processor.newSerializer(stringWriter);
		for (Entry<Property, String> entry : serializerOptions.entrySet()) {
			destination.setOutputProperty(entry.getKey(), entry.getValue());
		}

		applyXslt(xml, xslt, destination);

		String result = stringWriter.toString();
		return result;
	}

	/**
	 * Formats the <code>xmlString</code> using the specified <code>options</code> and returns the result as a string.
	 */
	public static String formatToString(String xmlString, FormattingOptions options) {
		return formatToString(xmlString, options.getSerializerProperties());
	}

	/**
	 * Reads the <code>xmlFile</code>, {@link #formatToString(String, FormattingOptions) formats} and then writes the
	 * result to the <code>destinationFile</code>.
	 */
	public static void formatToFile(File xmlFile, File destinationFile, FormattingOptions options) {
		String xmlString = FileTools.readStringFromFile(xmlFile);
		String result = formatToString(xmlString, options);
		FileTools.writeStringToFile(destinationFile, result);

	}

	/**
	 * Delegates to {@link #evaluateXpath(File, String, Map)} passing no namespaces (i.e. <code>null</code>).
	 */
	public static XdmValue evaluateXpath(File xmlFile, String xpath) throws SaxonApiException {
		return evaluateXpath(xmlFile, xpath, null);
	}

	/**
	 * Evaluates the specified <code>xpath</code> and returns the matching nodes from an XML document. One can explicitly
	 * set the namespaces with their prefixes.
	 * 
	 * @param xmlFile
	 *            the file to create the XML document.
	 * @param xpath
	 *            the <code>xpath</code> to evaluate
	 * @param prefixToNamespace
	 *            the maps of prefixes to URIs for the namespaces
	 * @return the nodes matching the xpath
	 * @throws SaxonApiException
	 *             if an error occurs during the expression evaluation.
	 */
	public static XdmValue evaluateXpath(File xmlFile, String xpath, Map<String, String> prefixToNamespace) throws SaxonApiException {
		Processor processor = new Processor(false);
		XdmItem documentElement = processor.newDocumentBuilder().build(xmlFile);
		XPathCompiler xPathCompiler = processor.newXPathCompiler();
		for (Entry<String, String> entry : NullSafe.map(prefixToNamespace).entrySet()) {
			xPathCompiler.declareNamespace(entry.getKey(), entry.getValue());
		}
		XPathSelector selector = xPathCompiler.compile(xpath).load();
		selector.setContextItem(documentElement);
		return selector.evaluate();
	}
}
