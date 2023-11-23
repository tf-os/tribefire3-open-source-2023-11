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
package com.braintribe.utils.html;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;

import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.logging.io.LoggingPrintWriter;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;

public class HtmlTools {
	
	private static Logger logger = Logger.getLogger(HtmlTools.class);

	public final static String extractTextXslt = 
			"<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">\n" + 
			"<xsl:output method=\"text\"/>\n" + 
			"<xsl:template match=\"/\">\n" + 
			"<xsl:value-of select=\".\"/>\n" + 
			"</xsl:template>\n" + 
			"</xsl:stylesheet>";
	
	/**
	 * Uses the {@link StringEscapeUtils#escapeHtml(String)} method (of commons-lang) to HTML-escape a text. In addition, it replaces
	 * all occurrences of CR/LF (or CR, or LF) by {@code <br />}.
	 *  
	 * @param text The raw text that should be HTML-escaped.
	 * @return The HTML-escaped text.
	 */
	public static String escapeHtml(String text) {
		if (text == null) {
			return null;
		}
		String escapedXml = StringEscapeUtils.escapeHtml(text);
		escapedXml = escapedXml.replaceAll("(\\r\\n|\\n|\\r)", "<br />");
		return escapedXml;
	}
	/**
	 * Uses the {@link StringEscapeUtils#escapeXml(String)} method (of commons-lang) to XML-escape a text. 
	 *  
	 * @param text The raw text that should be XML-escaped.
	 * @return The XML-escaped text.
	 */
	public static String escapeXml(String text) {
		if (text == null) {
			return null;
		}
		String escapedXml = StringEscapeUtils.escapeXml(text);
		return escapedXml;
	}
	
	/**
	 * Uses the {@link StringEscapeUtils#unescapeHtml(String)} method (of commons-lang) to unescape a HTML text.
	 * 
	 * @param htmlText The text that potentially contains HTML-encoded characters (e.g. starting with %, or &#)
	 * @return The unescaped text.
	 */
	public static String unescapeHtml(String htmlText) {
		if (htmlText == null) {
			return null;
		}
		String unescapedText = StringEscapeUtils.unescapeHtml(htmlText);
		return unescapedText;
	}
	
	/**
	 * Reads an HTML file and returns the content as a String. If no encoding is
	 * provided, the method tries to guess the encoding by invoking
	 * {@link #guessSourceEncoding(File)}. If this method fails, the provided
	 * fallbackEncoding will be used. If this is empty, UTF-8 will be used by
	 * default.
	 * 
	 * @param sourceFile
	 *            The File that should be read.
	 * @param encoding
	 *            The encoding of the file.
	 * @param fallbackEncoding
	 *            The fallback encoding if encoding is empty and the encoding of
	 *            the file could not be guessed.
	 * @return The HTML file content.
	 * @throws Exception
	 *             Thrown if the file does not exist or could not be read.
	 */
	public static String readHTML(File sourceFile, String encoding, String fallbackEncoding) throws Exception {

		if (sourceFile == null) {
			throw new IllegalArgumentException("No HTML file provided.");
		}
		if (!sourceFile.exists()) {
			throw new IllegalArgumentException("The file " + sourceFile.getAbsolutePath() + " does not exist.");
		}

		boolean trace = logger.isTraceEnabled();

		if (StringTools.isEmpty(encoding)) {

			if (trace) {
				logger.trace(
						"The encoding of file " + sourceFile.getAbsolutePath() + " is not provided. Trying to guess.");
			}

			Charset cs = guessSourceEncoding(sourceFile);
			if (cs != null) {

				if (trace) {
					logger.trace("Guessed encoding " + cs.name());
				}

				encoding = cs.name();
			} else {

				if (trace) {
					logger.trace("Could not guess encoding of file " + sourceFile.getAbsolutePath()
							+ ". Using fallback " + fallbackEncoding);
				}

				encoding = fallbackEncoding;
			}
		}
		if (StringTools.isEmpty(encoding)) {

			if (trace) {
				logger.trace("Using default UTF-8 encoding.");
			}

			encoding = "UTF-8";
		}

		String html = IOTools.slurp(sourceFile, encoding);

		return html;
	}

	/**
	 * Guesses the file encoding of an HTML file by looking for the charset=
	 * part in the header.
	 * 
	 * @param sourceFile
	 *            The HTML file that should be read.
	 * @return The charset of the file or null, if the charset could not be
	 *         guessed.
	 */
	public static Charset guessSourceEncoding(File sourceFile) {
		boolean trace = logger.isTraceEnabled();

		try {
			String content = IOTools.slurp(sourceFile, "UTF-8");
			long regExMesuring = System.currentTimeMillis();

			if (trace) {
				logger.trace("Read " + content.length() + " bytes from source file " + sourceFile);
			}

			Pattern pattern = Pattern.compile(".*charset=([0-9a-zA-Z\\-]+).*", Pattern.MULTILINE);
			Matcher matcher = pattern.matcher(content);
			matcher.find();

			if (trace) {
				regExMesuring = System.currentTimeMillis() - regExMesuring;
				logger.trace("RegEx took " + regExMesuring + " milliseconds");
			}

			String encoding = matcher.group(1);

			if (trace) {
				logger.trace("Found encoding " + encoding + " in source file " + sourceFile);
			}

			return Charset.forName(encoding);
		} catch (Exception e) {
			logger.debug("Could not get charset from source file " + sourceFile + ": " + e.getMessage(), e);
		}

		return null;
	}

	/**
	 * Cleans up the HTML markup and return XHTML markup. This method tries to
	 * call all of the internal normalization methods until one returns a
	 * non-empty result.
	 * 
	 * @param html
	 *            The raw HTML markup.
	 * @return The XHTML markup.
	 */
	public static String normalizeHTML(String html) {

		String xhtml = normalizeHTMLWithJTidy(html);
		if (StringTools.isEmpty(xhtml)) {
			xhtml = normalizeHTMLWithTagSoup(html);
		}

		if (StringTools.isEmpty(xhtml)) {
			return html;
		}

		return xhtml;
	}

	/**
	 * Internal service method that is used by {@link #normalizeHTML(String)}.
	 * 
	 * @param html
	 *            The HTML markup.
	 * @return The XHTML markup or null, if the method fails to produce XHTML.
	 */
	protected static String normalizeHTMLWithTagSoup(String html) {

		try {
			StringWriter sw = new StringWriter();
			
			SAXTransformerFactory f = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
			TransformerHandler t = f.newTransformerHandler();
			Transformer transformer = t.getTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
	        transformer.setOutputProperty(OutputKeys.INDENT,"yes");
			t.setResult(new StreamResult(sw));

			org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
			parser.setContentHandler(t);
			
			parser.parse(new InputSource(new StringReader(html)));

			String xhtml = sw.toString();

			return xhtml;
		} catch (Exception e) {
			logger.warn("Could not parse HTML.", e);
		}

		return null;
	}

	/**
	 * Internal service method that is used by {@link #normalizeHTML(String)}.
	 * 
	 * @param html
	 *            The HTML markup.
	 * @return The XHTML markup or null, if the method fails to produce XHTML.
	 */
	protected static String normalizeHTMLWithJTidy(String html) {
		if (html == null) {
			return null;
		}

		// obtain a new Tidy instance
		Tidy tidy = new Tidy();
		// set desired config options using tidy setters
		tidy.setXHTML(true);
		tidy.setShowErrors(0);
		tidy.setQuiet(true);
		tidy.setErrout(new LoggingPrintWriter(logger, LogLevel.DEBUG));
		tidy.setDocType("omit");

		// run tidy
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Reader reader = new StringReader(html);
		tidy.parse(reader, baos);
		String xhtml = new String(baos.toByteArray());

		return xhtml;
	}

	public static boolean guessHTML(String text) {
		if (StringTools.isEmpty(text)) {
			return false;
		}

		try {
			
			StringWriter sw = new StringWriter();
			
			SAXTransformerFactory f = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
			
			TransformerHandler t = f.newTransformerHandler(new StreamSource(new StringReader(extractTextXslt)));
			t.setResult(new StreamResult(sw));

			org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
			parser.setContentHandler(t);
			
			parser.parse(new InputSource(new StringReader(text)));

			String extractedText = sw.toString();

			int originalLength = text.length();
			int extractedLength = extractedText.length();

			// No change, no HTML
			int diff = originalLength - extractedLength;
			if (diff == 0) {
				return false;
			}

			// When there is more than 10% difference, it was likely HTML
			double origDouble = originalLength;
			double diffDouble = diff;
			if ((origDouble / 10) < diffDouble) {
				return true;
			}

			return false;
		} catch (Exception e) {
			logger.debug("Could not guess HTML.", e);
			return false;
		}

		/*
		 * Alternative code that counts some HTML tags and int length =
		 * text.length(); int blockLength = Math.min(length, 1000); if
		 * (blockLength < length) { text = text.substring(0, blockLength); }
		 * text = text.toLowerCase();
		 * 
		 * int htmlCount = 0; htmlCount += StringTools.countOccurences(text,
		 * "<br", true); htmlCount += StringTools.countOccurences(text, "<div",
		 * true); htmlCount += StringTools.countOccurences(text, "<font", true);
		 * htmlCount += StringTools.countOccurences(text, "<td", true);
		 * 
		 * //If count > (some magic value) ==> HTML
		 */
	}

	public static String removeHtmlIncompatibleCharacters(String str) {
		if (str == null) {
			return null;
		}
		str = str.replace('<', ' ');
		str = str.replace('>', ' ');
		str = str.replace('&', ' ');
		return str;
	}

}
