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
package com.braintribe.utils.jdom2;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.Format.TextMode;
import org.jdom2.output.XMLOutputter;
import org.jdom2.output.support.AbstractXMLOutputProcessor;
import org.jdom2.output.support.FormatStack;
import org.jdom2.util.NamespaceStack;

import com.braintribe.common.lcd.uncheckedcounterpartexceptions.UncheckedIOException;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.DOMTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.StringTools;

/**
 * Formats XMLs using JDom2. The class provides multiple <code>format</code> methods to conveniently format XML strings
 * or files with specific options. Furthermore there are also several <code>prettyFormat</code> methods where one
 * doesn't have to specify any options.
 * <p>
 * When NOT to use this class: if you just want to format an XML string without any special requirements with respect to
 * formatter settings, there is no need for an additional dependency. Instead you can e.g. just use
 * {@link DOMTools#format(String)}. Performance will probably be better too.<br>
 * When to use this class: if you have special requirements such as preserving attribute order or blank lines, this
 * class provides more {@link FormattingOptions options} than most other XML libraries. This especially can be useful
 * when formatting configuration files which have to be human-readable and -comparable.
 * <p>
 * The implementation is based on JDom2, but also adds some additional tweaks which are not available in JDom2, e.g. to
 * have better control for XML declarations, to make adding a space before closing empty elements optional or to (by
 * default) re-use the line separator from the XML string/file. Due to these tweaks, formating is probably slower than
 * with most other libraries.
 *
 * @author michael.lafite
 *
 * @see #format(String, FormattingOptions)
 * @see #format(File, FormattingOptions)
 * @see #prettyFormat(String)
 * @see #prettyFormat(File)
 */
public class JDomFormatter {

	private static final String BLANKLINE_MARKER_ELEMENTNAME = "JDOMFORMATTER__BLANKLINE_MARKER_FOR_FORMATTING";
	private static final String BLANKLINE_MARKER = "<" + BLANKLINE_MARKER_ELEMENTNAME + "/>";
	private static final String BLANKLINE_MARKER_REGEX = "\\<" + BLANKLINE_MARKER_ELEMENTNAME + "(\\ ?\\/\\>|\\>\\<\\/" + BLANKLINE_MARKER_ELEMENTNAME
			+ "\\>)";

	/**
	 * Invokes {@link #prettyFormat(File, String)} with no encoding specified.
	 */
	public static void prettyFormat(File xmlFile) {
		prettyFormat(xmlFile, null);
	}

	/**
	 * This method works like {@link #format(File, String, FormattingOptions)} with {@link FormattingOptions} that
	 * create a {@link FormattingOptions#getPrettyFormattingOptions(String) pretty formatted} XML.
	 *
	 * @see #prettyFormat(String)
	 */
	public static void prettyFormat(File xmlFile, String readEncoding) {
		String xmlString = FileTools.readStringFromFile(xmlFile, getReadEncoding(xmlFile, readEncoding));
		FormattingOptions formattingOptions = FormattingOptions.getPrettyFormattingOptions(xmlString);
		String formattedXmlString = format(xmlString, formattingOptions);
		FileTools.writeStringToFile(xmlFile, formattedXmlString, formattingOptions.getEncoding());
	}

	/**
	 * Invokes {@link #format(String, FormattingOptions)} with {@link FormattingOptions} that create a
	 * {@link FormattingOptions#getPrettyFormattingOptions(String) pretty formatted} XML.
	 *
	 * @see #prettyFormat(File, String)
	 */
	public static String prettyFormat(String xmlString) {
		FormattingOptions formattingOptions = FormattingOptions.getPrettyFormattingOptions(xmlString);
		String result = format(xmlString, formattingOptions);
		return result;
	}

	/**
	 * Invokes {@link #format(File, String, FormattingOptions)} with no encoding specified.
	 */
	public static void format(File xmlFile, FormattingOptions formattingOptions) {
		format(xmlFile, null, formattingOptions);
	}

	/**
	 * Convenience method which reads, formats and writes an XML file.
	 *
	 * @param xmlFile
	 *            the file to format.
	 * @param readEncoding
	 *            the encoding used to read from the file. If not specified, the methods tries to determine the encoding
	 *            from the XML declaration. Otherwise the {@link FormattingOptions#DEFAULT_ENCODING default encoding} is
	 *            used. Note that the encoding used for writing the file is specified via
	 *            {@link FormattingOptions#setEncoding(String)}. Therefore one can use this method to change the
	 *            encoding of file.
	 * @param formattingOptions
	 *            the formatting options.
	 * @throws IllegalArgumentException
	 *             if the XML string or the formatting options are invalid.
	 * @throws UncheckedIOException
	 *             if any IO error occurs.
	 */
	public static void format(File xmlFile, String readEncoding, FormattingOptions formattingOptions)
			throws IllegalArgumentException, UncheckedIOException {
		String xmlString = FileTools.readStringFromFile(xmlFile, getReadEncoding(xmlFile, readEncoding));
		String formattedXmlString = format(xmlString, formattingOptions);
		FileTools.writeStringToFile(xmlFile, formattedXmlString, formattingOptions.getEncoding());
	}

	/**
	 * Gets the read-encoding for the specified <code>xmlFile</code>. If <code>specifiedEncoding</code> is not
	 * <code>null</code>, it will be returned. Otherwise the method tries to read the encoding from the XML declaration.
	 * If there is none or if it doesn't have an <code>encoding</code> attribute, the
	 * {@link FormattingOptions#DEFAULT_ENCODING default encoding} is returned.
	 */
	private static String getReadEncoding(File xmlFile, String specifiedEncoding) {
		String result = specifiedEncoding;
		if (result == null) {
			result = DOMTools.parseEncodingFromDeclaration(xmlFile);
			if (result == null) {
				result = FormattingOptions.DEFAULT_ENCODING; // default encoding
			}
		}
		return result;
	}

	/**
	 * Formats the passed <code>xmlString</code>.
	 *
	 * @param xmlString
	 *            the (XML) string to format. Must be well-formed!
	 * @param formattingOptions
	 *            the {@link FormattingOptions} format.
	 * @return the formatted string.
	 * @throws IllegalArgumentException
	 *             if the XML string or the formatting options are invalid.
	 */
	public static String format(String xmlString, FormattingOptions formattingOptions) throws IllegalArgumentException {
		Format format = createFormat(formattingOptions);
		String result = format(xmlString, format, formattingOptions);
		return result;
	}

	/**
	 * Formats the passed <code>xmlString</code>. Unless special {@link Format} options are required, it is recommended
	 * to use the more convenient method {@link #format(String, FormattingOptions)}.
	 *
	 * @param xmlString
	 *            the (XML) string to format. Must be well-formed!
	 * @param format
	 *            the JDOM format.
	 * @param additionalJDomFormattingOptions
	 *            additional options not supported by JDOM {@link Format}.
	 * @return the formatted string.
	 * @throws IllegalArgumentException
	 *             if the <code>xmlString</code> is invalid or any option (combination) is invalid (e.g.
	 *             <code>version</code> set <code>null</code> although XML declaration is not omitted).
	 */
	private static String format(String xmlString, Format format, AdditionalJDomFormattingOptions additionalJDomFormattingOptions)
			throws IllegalArgumentException {

		final int preservedBlankLines = additionalJDomFormattingOptions.getPreservedBlankLines();
		final String lineSeparator = format.getLineSeparator();

		String xmlStringToFormat = xmlString;

		if (preservedBlankLines != 0) {
			/* The strategy to preserve blank lines is to add empty elements (blank line markers), which will later be
			 * removed again. Before we do this, we first format the string using raw format. This preserves almost all
			 * white space, but removes blank lines outside the root element and also removes line breaks from attribute
			 * values. In both cases we'd otherwise add our blank line markers at locations where they are not
			 * permitted. */
			xmlStringToFormat = format(xmlStringToFormat, Format.getRawFormat(),
					additionalJDomFormattingOptions.isAddSpaceBeforeClosingEmptyElements());

			// make sure only target line separator is used
			xmlStringToFormat = StringTools.normalizeLineSeparators(xmlStringToFormat, lineSeparator);

			// replace blank lines with blank line marker
			xmlStringToFormat = StringTools.replaceLinesWith(xmlStringToFormat, lineSeparator, "\\s*", BLANKLINE_MARKER);

			if (preservedBlankLines != -1) {
				// --> limit maximum number of consecutive blank line markers
				xmlStringToFormat = StringTools.limitConsecutiveOccurrences(xmlStringToFormat, BLANKLINE_MARKER + lineSeparator, preservedBlankLines);
			}
		}

		String formattedXmlString = format(xmlStringToFormat, format, additionalJDomFormattingOptions.isAddSpaceBeforeClosingEmptyElements());
		// JDOM formatter doesn't set lineSeparator everywhere (see Javadoc)
		formattedXmlString = StringTools.normalizeLineSeparators(formattedXmlString, lineSeparator);

		String formattedXmlStringWithCorrectDeclaration = formattedXmlString;
		if (!format.getOmitDeclaration()) {
			// create correct declaration and replace existing one

			String version = additionalJDomFormattingOptions.getVersion();
			if (version == null) {
				throw new IllegalArgumentException("XML declaration not omitted, but version not specified!");
			}

			String encoding = null;
			if (!format.getOmitEncoding()) {
				encoding = format.getEncoding();
			}

			Boolean standalone = additionalJDomFormattingOptions.getStandalone();

			String declaration = DOMTools.createDeclaration(version, encoding, standalone);
			formattedXmlStringWithCorrectDeclaration = declaration + lineSeparator
					+ DOMTools.removeXmlDeclarationPrefix(formattedXmlStringWithCorrectDeclaration);
		}

		String result = formattedXmlStringWithCorrectDeclaration;
		if (preservedBlankLines != 0) {
			// replace blank line markers with empty lines
			result = StringTools.replaceLinesWith(formattedXmlStringWithCorrectDeclaration, lineSeparator, "^\\s*" + BLANKLINE_MARKER_REGEX, "");
		}

		return result;
	}

	/**
	 * Formats the passed <code>xmlString</code> using JDom2 and the specified <code>format</code> settings.
	 *
	 * @param addSpaceBeforeClosingEmptyElements
	 *            whether or not to add a space before closing empty elements (i.e. without any child elements, only
	 *            attributes)
	 * @throws IllegalArgumentException
	 *             if the <code>xmlString</code> is invalid.
	 */
	private static String format(String xmlString, Format format, boolean addSpaceBeforeClosingEmptyElements) throws IllegalArgumentException {
		SAXBuilder jdomBuilder = new SAXBuilder();
		Document jdomDocument;
		try {
			jdomDocument = jdomBuilder.build(new StringReader(xmlString));
		} catch (JDOMException | IOException e) {
			throw new IllegalArgumentException("Error while parsing xml string! " + CommonTools.getParametersString("string", xmlString), e);
		}
		XMLOutputter xmlOutputter = new XMLOutputter();
		if (!addSpaceBeforeClosingEmptyElements) {
			xmlOutputter.setXMLOutputProcessor(new NoSpaceBeforeClosingEmptyElementsXmlOutputProcessor());
		}

		xmlOutputter.setFormat(format);
		String result = xmlOutputter.outputString(jdomDocument);
		return result;
	}

	/**
	 * Creates a JDom {@link Format} instance for the passed <code>options</code>. Note that the
	 * {@link AdditionalJDomFormattingOptions} are ignored, since they are not supported by <code>Format</code>.
	 */
	private static Format createFormat(FormattingOptions options) {
		Format format = Format.getRawFormat();
		if (!options.isPreserveWhitespace()) {
			format.setTextMode(TextMode.TRIM_FULL_WHITE);
		}
		// indent = configured [indent] string [indentSize] times
		format.setIndent(StringTools.createStringFromCollection(CollectionTools.getFilledList(options.indent, options.indentSize), ""));

		format.setLineSeparator(options.lineSeparator);

		format.setEncoding(options.encoding);
		format.setOmitEncoding(options.omitEncoding);

		format.setOmitDeclaration(options.omitDeclaration);

		format.setExpandEmptyElements(options.expandEmptyElements);

		return format;
	}

	/**
	 * Helper class used to set and pass options which are NOT supported by JDom {@link Format}.
	 *
	 * @author michael.lafite
	 */
	public static class AdditionalJDomFormattingOptions {
		public static final Boolean DEFAULT_STANDALONE = null;
		public static final String DEFAULT_VERSION = "1.0";
		public static final int DEFAULT_PRESERVEDBLANKLINES = 0;
		public static final boolean DEFAULT_ADDSPACEBEFORECLOSINGEMPTYELEMENTS = false;

		private Boolean standalone = DEFAULT_STANDALONE;
		private String version = DEFAULT_VERSION;
		private int preservedBlankLines = DEFAULT_PRESERVEDBLANKLINES;
		private boolean addSpaceBeforeClosingEmptyElements = DEFAULT_ADDSPACEBEFORECLOSINGEMPTYELEMENTS;

		public Boolean getStandalone() {
			return standalone;
		}

		public AdditionalJDomFormattingOptions setStandalone(Boolean standalone) {
			this.standalone = standalone;
			return this;
		}

		public String getVersion() {
			return version;
		}

		public AdditionalJDomFormattingOptions setVersion(String version) {
			this.version = version;
			return this;
		}

		public int getPreservedBlankLines() {
			return preservedBlankLines;
		}

		/**
		 * Unless {@link Format#setTextMode(TextMode) whitespace is preserved}, blank (i.e. whitespace-only) lines will
		 * be removed during the formatting process. This settings controls how many (consecutive) blank lines shall be
		 * preserved. (They will be replaced with empty lines.) Default: {@value #DEFAULT_PRESERVEDBLANKLINES}.
		 */
		public AdditionalJDomFormattingOptions setPreservedBlankLines(int preservedBlankLines) {
			if (preservedBlankLines < -1) {
				throw new IllegalArgumentException(
						"Please use -1 to preserve all blank lines! " + CommonTools.getParametersString("preservedBlankLines", preservedBlankLines));
			}

			this.preservedBlankLines = preservedBlankLines;
			return this;
		}

		public boolean isAddSpaceBeforeClosingEmptyElements() {
			return addSpaceBeforeClosingEmptyElements;
		}

		public AdditionalJDomFormattingOptions setAddSpaceBeforeClosingEmptyElements(boolean addSpaceBeforeClosingEmptyElements) {
			this.addSpaceBeforeClosingEmptyElements = addSpaceBeforeClosingEmptyElements;
			return this;
		}
	}

	/**
	 * Convenience class which extends {@link AdditionalJDomFormattingOptions} and also wraps most JDom {@link Format}
	 * options and also adds some convenience. This is the preferred way to set {@link JDomFormatter} options.
	 *
	 * @author michael.lafite
	 *
	 * @see JDomFormatter#format(String, FormattingOptions)
	 */
	public static class FormattingOptions extends AdditionalJDomFormattingOptions {
		public static final int DEFAULT_INDENTSIZE = 1;
		public static final String DEFAULT_INDENT = "\t";
		public static final String DEFAULT_LINESEPARATOR = "\n";
		public static final boolean DEFAULT_OMITDECLARATION = false;
		public static final boolean DEFAULT_OMITENCODING = false;
		public static final String DEFAULT_ENCODING = "UTF-8";
		public static final boolean DEFAULT_EXPANDEMPTYELEMENTS = false;
		public static final boolean DEFAULT_PRESERVEWHITESPACE = false;

		private int indentSize = DEFAULT_INDENTSIZE;
		private String indent = DEFAULT_INDENT;
		private String lineSeparator = DEFAULT_LINESEPARATOR;
		private boolean omitDeclaration = DEFAULT_OMITDECLARATION;
		private boolean omitEncoding = DEFAULT_OMITENCODING;
		private String encoding = DEFAULT_ENCODING;
		private boolean expandEmptyElements = DEFAULT_EXPANDEMPTYELEMENTS;
		private boolean preserveWhitespace = DEFAULT_PRESERVEWHITESPACE;

		public FormattingOptions() {
			// nothing to do (keep defaults)
		}

		/**
		 * Creates a new instance based on the passed <code>xmlString</code>. If the <code>xmlString</code> doesn't
		 * start with an XML declaration, {@link #setOmitDeclaration(Boolean) omitDeclaration} will be set to
		 * <code>true</code>. Otherwise {@link #setVersion(String) version}, {@link #setEncoding(String) encoding} and
		 * {@link #setStandalone(Boolean) standAlone} will be parsed from the declaration. Furthermore the
		 * {@link #setLineSeparator(String) line separator} is set to the (first) line separator used in the
		 * <code>xmlString</code>.
		 */
		public FormattingOptions(String xmlString) {
			String declaration = DOMTools.getDeclaration(xmlString);
			setOmitDeclaration(declaration == null);

			if (!isOmitDeclaration()) {

				setVersion(DOMTools.parseVersionFromDeclaration(declaration));

				String parsedEncoding = DOMTools.parseEncodingFromDeclaration(declaration);
				setOmitEncoding(parsedEncoding == null);
				if (!isOmitEncoding()) {
					setEncoding(parsedEncoding);
				}

				Boolean standalone = DOMTools.parseStandaloneFromDeclaration(declaration);
				setStandalone(standalone);
			}

			// by default we keep the line separator used in the string
			String firstLineSeparatorUsedInFile = StringTools.getFirstLineSeparator(xmlString);
			if (firstLineSeparatorUsedInFile != null) {
				setLineSeparator(firstLineSeparatorUsedInFile);
			}
		}

		/**
		 * Returns {@link #getPrettyFormattingOptions() pretty formatting options}. XML declaration settings will be
		 * based on the passed <code>xmlString</code> (see {@link #FormattingOptions(String)}.
		 *
		 * @see #getPrettyFormattingOptions()
		 */
		public static FormattingOptions getPrettyFormattingOptions(String xmlString) {
			FormattingOptions formattingOptions = new FormattingOptions(xmlString);
			toPrettyFormattingOptions(formattingOptions);
			return formattingOptions;
		}

		/**
		 * Reads the <code>xmlFile</code> and then delegates to {@link #getPrettyFormattingOptions(String)}.
		 */
		public static FormattingOptions getPrettyFormattingOptions(File xmlFile) {
			String xmlString = FileTools.readStringFromFile(xmlFile, getReadEncoding(xmlFile, null));
			return getPrettyFormattingOptions(xmlString);
		}

		/**
		 * Returns formatting options to create a pretty-formatted XML string. This means that whitespace will be
		 * trimmed, elements will be indented, blank lines will be preserved (but only 2).
		 *
		 * @see #getPrettyFormattingOptions(String)
		 */
		public static FormattingOptions getPrettyFormattingOptions() {
			FormattingOptions formattingOptions = new FormattingOptions();
			toPrettyFormattingOptions(formattingOptions);
			return formattingOptions;
		}

		private static void toPrettyFormattingOptions(FormattingOptions formattingOptions) {
			formattingOptions.setPreservedBlankLines(2);
			formattingOptions.setPreserveWhitespace(false);
		}

		// *** super type setters ***
		@Override
		public FormattingOptions setStandalone(Boolean standalone) {
			return (FormattingOptions) super.setStandalone(standalone);
		}

		@Override
		public FormattingOptions setVersion(String version) {
			return (FormattingOptions) super.setVersion(version);
		}

		@Override
		public FormattingOptions setPreservedBlankLines(int preservedBlankLines) {
			return (FormattingOptions) super.setPreservedBlankLines(preservedBlankLines);
		}

		@Override
		public FormattingOptions setAddSpaceBeforeClosingEmptyElements(boolean addSpaceBeforeClosingEmptyElements) {
			return (FormattingOptions) super.setAddSpaceBeforeClosingEmptyElements(addSpaceBeforeClosingEmptyElements);
		}
		// **************************

		public int getIndentSize() {
			return indentSize;
		}

		public FormattingOptions setIndentSize(int indentSize) {
			this.indentSize = indentSize;
			return this;
		}

		public String getIndent() {
			return indent;
		}

		public FormattingOptions setIndent(String indent) {
			this.indent = indent;
			return this;
		}

		public String getLineSeparator() {
			return lineSeparator;
		}

		public FormattingOptions setLineSeparator(String lineSeparator) {
			this.lineSeparator = lineSeparator;
			return this;
		}

		public Boolean isOmitDeclaration() {
			return omitDeclaration;
		}

		public FormattingOptions setOmitDeclaration(Boolean omitDeclaration) {
			this.omitDeclaration = omitDeclaration;
			return this;
		}

		public String getEncoding() {
			return encoding;
		}

		public FormattingOptions setEncoding(String encoding) {
			this.encoding = encoding;
			return this;
		}

		public boolean isOmitEncoding() {
			return omitEncoding;
		}

		public void setOmitEncoding(boolean omitEncoding) {
			this.omitEncoding = omitEncoding;
		}

		public boolean isExpandEmptyElements() {
			return expandEmptyElements;
		}

		/**
		 * Whether to expand empty elements (HTML) or not (XML only).
		 */
		public FormattingOptions setExpandEmptyElements(boolean expandEmptyElements) {
			this.expandEmptyElements = expandEmptyElements;
			return this;
		}

		public boolean isPreserveWhitespace() {
			return preserveWhitespace;
		}

		public FormattingOptions setPreserveWhitespace(boolean preserveWhitespace) {
			this.preserveWhitespace = preserveWhitespace;
			return this;
		}
	}

	/**
	 * Very simple {@link AbstractXMLOutputProcessor} extension whose single purpose is to omit the space before closing
	 * empty elements, i.e. &lt;example/&gt; instead of &lt;example /&gt;.
	 *
	 * @author michael.lafite
	 */
	private static class NoSpaceBeforeClosingEmptyElementsXmlOutputProcessor extends AbstractXMLOutputProcessor {
		@Override
		protected void printElement(final Writer out, final FormatStack fstack, final NamespaceStack nstack, final Element element)
				throws IOException {
			StringWriter stringWriter = new StringWriter();
			super.printElement(stringWriter, fstack, nstack, element);
			String originalElementString = stringWriter.toString();

			final String modifiedElementString;
			if (originalElementString.endsWith(" />")) {
				modifiedElementString = StringTools.removeLastNCharacters(originalElementString, 3) + "/>";
			} else {
				modifiedElementString = originalElementString;
			}
			out.write(modifiedElementString);
		}
	}
}
