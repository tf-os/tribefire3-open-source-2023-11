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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

import com.braintribe.utils.DOMTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.jdom2.JDomFormatter.FormattingOptions;

/**
 * Provides tests for {@link JDomFormatter}.
 *
 * @author michael.lafite
 */
public class JDomFormatterTest {

	@Test
	public void testFormatStrings() {

		// new line added at the end
		format("<root/>", "<root/>\n");

		// new line added after comments outside root element
		format("<!--a--><!--b--><root/><!--a--><!--b-->", "<!--a-->\n<!--b-->\n<root/>\n<!--a-->\n<!--b-->\n");

		// new lines and white space in comments are completely preserved
		format("<!--x\nx\n\n\n\t\t\t   x-->\n<root/>\n<!--a\n\nb-->\n<!--c\n\nd-->\n");

		// specified number of blank lines preserved
		{
			final String xmlString = "\n\n<!-- x -->\n\n\t<root><a>   \n   \n\n\n\n\n</a><b> </b></root>   \n\n  \n  ";
			format(xmlString, "<!-- x -->\n<root>\n\t<a/>\n\t<b/>\n</root>\n", 0);
			format(xmlString, "<!-- x -->\n<root>\n\t<a>\n\n\t</a>\n\t<b/>\n</root>\n", 1);
			format(xmlString, "<!-- x -->\n<root>\n\t<a>\n\n\n\t</a>\n\t<b/>\n</root>\n", 2);
		}

		// blank lines + options for empty elements
		{
			final String xmlString = "\n\n<!-- x -->\n\n\t<root><a>   \n   \n\n\n\n\n</a><b> </b></root>   \n\n  \n  ";
			FormattingOptions options = new FormattingOptions();
			options.setAddSpaceBeforeClosingEmptyElements(false);
			options.setPreservedBlankLines(2);
			options.setOmitDeclaration(true);
			format(xmlString, "<!-- x -->\n<root>\n\t<a>\n\n\n\t</a>\n\t<b/>\n</root>\n", options);
			options.setAddSpaceBeforeClosingEmptyElements(true);
			format(xmlString, "<!-- x -->\n<root>\n\t<a>\n\n\n\t</a>\n\t<b />\n</root>\n", options);
			options.setExpandEmptyElements(true);
			format(xmlString, "<!-- x -->\n<root>\n\t<a>\n\n\n\t</a>\n\t<b></b>\n</root>\n", options);
		}

		// line breaks in attributes are normalized and thus blank lines are NOT preserved
		format("<root x=\"a\nb\"/>\n", "<root x=\"a b\"/>\n", -1);
		format("<root x=\"a\nb\n\nc\"/>\n", "<root x=\"a b  c\"/>\n", -1);

		// blank lines outside root element removed
		format("\n\n<!--a-->\n\n<!--b-->\n\n<root/>\n\n<!--a-->\n\n<!--b-->\n\n", "<!--a-->\n<!--b-->\n<root/>\n<!--a-->\n<!--b-->\n", -1);

		// blank lines replaced with empty lines
		format("<root>\n  \n\t\n</root>", "<root>\n\n\n</root>\n", -1);

		// XML declaration is is kept
		format("<?xml version=\"1.0\"?>\n<root/>\n");
		format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root/>\n");
		format("<?xml version=\"1.0\" standalone=\"no\"?>\n<root/>\n");
		format("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<root/>\n");

		// no line wrapping
		format("<root x=\"111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\" y=\"111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\" z=\"111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\"/>\n");

		// line separator reused
		format("<root>\n\t<sub/>\n</root>\n");
		format("<root>\r\n\t<sub/>\r\n</root>\r\n");
		format("<root>\r\t<sub/>\r</root>\r");
		// first line separator wins
		format("<root>\r\n\t<sub/>\r</root>\n", "<root>\r\n\t<sub/>\r\n</root>\r\n");

		// <![CDATA[]]>
		// no automatic newlines before/after, but newlines before/after are preserved
		format("<root><![CDATA[abc]]></root>\n");
		format("<root>\n<![CDATA[abc]]></root>\n");
		format("<root>\n<![CDATA[abc]]>\n</root>\n");
		format("<root>\n<![CDATA[abc]]>\n</root>\n");
		format("<root>\n\t<![CDATA[abc]]>\n</root>\n");
		// special characters
		format("<root><![CDATA[<>/&äöüß]]></root>\n");
	}

	@Test
	public void testInvalidEscape() {
		/*
		 *  This snippet was found in the POM for artifact org.codehaus.plexus:plexus-1.0.4.
		 *  Standard XML parsers are not (supposed to be) able to parse this. -->
		 */
		String xmlString = "<developer><name>Trygve Laugst&oslash;l</name></developer>";
		
		try {
			format(xmlString);
			fail("Unexpectedly was able to parse XML with invalid escapes: " + xmlString);
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	/**
	 * {@link JDomFormatter#prettyFormat(File) Pretty-formats} all unformatted files from the input folder and writes
	 * the results to the output folder. This test just makes sure that there are no exceptions and that the written
	 * files are still valid xmls.
	 */
	@Test
	public void testFormatFiles() {
		String relativeBaseFolderPath = "res/formatFilesTest";
		File sourceFolder = new File(relativeBaseFolderPath + "/input");
		File targetFolder = new File(relativeBaseFolderPath + "/output");
		targetFolder.mkdirs();

		// System.out.println("[diff] " + sourceFolder.getAbsolutePath() + " " + targetFolder.getAbsolutePath());

		for (File sourceFile : sourceFolder.listFiles()) {
			File targetFile = new File(targetFolder, sourceFile.getName());
			FileTools.copyFile(sourceFile, targetFile);

			FormattingOptions formattingOptions = FormattingOptions.getPrettyFormattingOptions(sourceFile);
			if (sourceFile.getName().matches("icc.*\\.spring\\.xml")) {
				formattingOptions.setAddSpaceBeforeClosingEmptyElements(true);
			}
			JDomFormatter.format(targetFile, formattingOptions);

			// parse XML to make sure it's valid
			DOMTools.parse(FileTools.readStringFromFile(targetFile));
		}
	}

	private static void format(String inputAndExpectedOutput) {
		format(inputAndExpectedOutput, inputAndExpectedOutput);
	}

	private static void format(String input, String expectedOutput) {
		format(input, expectedOutput, 0);
	}

	@SuppressWarnings("unused")
	private static void format(String inputAndExpectedOutput, int preservedBlankLines) {
		format(inputAndExpectedOutput, inputAndExpectedOutput, preservedBlankLines);
	}

	private static void format(String input, String expectedOutput, int preservedBlankLines) {
		FormattingOptions formattingOptions = new FormattingOptions(input).setPreservedBlankLines(preservedBlankLines);
		format(input, expectedOutput, formattingOptions);
	}

	@SuppressWarnings("unused")
	private static void format(String inputAndExpectedOutput, FormattingOptions formattingOptions) {
		format(inputAndExpectedOutput, inputAndExpectedOutput, formattingOptions);
	}

	private static void format(String input, String expectedOutput, FormattingOptions formattingOptions) {
		assertThat(JDomFormatter.format(input, formattingOptions)).isEqualTo(expectedOutput);
	}
}
