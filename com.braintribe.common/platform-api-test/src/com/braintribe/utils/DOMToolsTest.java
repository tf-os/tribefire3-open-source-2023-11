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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.common.lcd.DOMException;

/**
 * Provides tests for {@link DOMTools}.
 *
 * @author michael.lafite
 */
public class DOMToolsTest {

	private static final File toStringTest = new File("res/DomToolsTest/toStringTest.xml");
	private static final File toFormattedStringTest = new File("res/DomToolsTest/toFormattedStringTest.xml");

	private static final File toStringTest_pom = new File("res/DomToolsTest/toStringTest_pom.xml");

	@Test
	public void testCommentBeforeRootElement() throws Exception {
		String comment1 = "<!-- some comment line 1\n  some comment line 2 -->";
		String comment2 = "<!-- some other comment -->";
		String rootElement = "<root/>";

		String xml = comment1 + "\n" + comment2 + "\n" + rootElement;
		// white space (outside comments) before the root elements won't be preserved.
		String expectedUnformattedXml = comment1 + comment2 + rootElement;
		// only formatting we expect for this test is an additional line break at the end.
		String expectedFormattedXml = expectedUnformattedXml + "\n";
		// there may be different results with Java 8, see below.
		String expectedFormattedXml_LSSerializerJava8Bug = rootElement + "\n" + comment1 + "\n" + comment2 + "\n";

		Document document = DOMTools.parse(xml);

		assertThat(DOMTools.toString(document)).isEqualToWithVerboseErrorMessage(expectedUnformattedXml);
		try {
			assertThat(DOMTools.toFormattedString(document)).isEqualToWithVerboseErrorMessage(expectedFormattedXml);
		} catch (AssertionError e) {
			if (System.getProperty("java.specification.version").equals("1.8")) {
				// on certain versions of Java 8 (reproduced with openjdk version 1.8.0_332 as well as Zulu package jdk-8.0.322-zulu8.60.0.21),
				// formatting is quite broken. Note that not all packages are affected, e.g. Oracle JDK 1.8.0_271 works fine.
				assertThat(DOMTools.toFormattedString(document)).isEqualToWithVerboseErrorMessage(expectedFormattedXml_LSSerializerJava8Bug);
			} else {
				throw e;
			}
		}
		assertThat(DOMTools.toFormattedString_alternative(document)).isEqualToWithVerboseErrorMessage(expectedFormattedXml);
	}

	@Test
	public void testToString_pom() throws Exception {
		String xmlFileContent = FileTools.readStringFromFile(toStringTest_pom);

		/* the next check would fail on Windows, because xmlFileContent contains '\r'. TODO: unclear why! for what we want to test here, it's actually
		 * fine to just normalize the line separator. */
		xmlFileContent = StringTools.normalizeLineSeparators(xmlFileContent, "\n");

		// since we had some problems with line separators on Windows here, we first ensure that we only have unix line
		// feeds in the file
		assertThat(xmlFileContent).doesNotContain("\r");

		// get result of toString
		String toStringResult = DOMTools.toString(DOMTools.parse(xmlFileContent));

		// System.out.println(toStringResult);

		// File resultFile1 = new File("res/DomToolsTest/FormattingTest_testToString_pom.xml");
		// FileTools.writeStringToFile(resultFile1, toStringResult);
		// System.out.println("[diff_tool] " + toStringTest_pom.getAbsolutePath() + " " +
		// resultFile1.getAbsolutePath());

		/* the next check would fail on Windows, because toStringResult contains '\r'. TODO: unclear why! for what we want to test here, it's actually
		 * fine to just normalize the line separator. */
		toStringResult = StringTools.normalizeLineSeparators(toStringResult, "\n");

		// again ensure that we have only line feeds
		assertThat(toStringResult).doesNotContain("\r");

		assertThat(toStringResult).isEqualToWithVerboseErrorMessage(xmlFileContent);

		// System.out.println(DOMTools.format(DOMTools.parse(xmlFileContent)));
	}

	/**
	 * Makes sure that things like indentation (and white space in general), attribute order, etc. are properly preserved.
	 */
	@Test
	public void testToString() throws Exception {
		String xmlFileContent = FileTools.readStringFromFile(toStringTest);

		/* the next check would fail on Windows, because xmlFileContent contains '\r'. TODO: unclear why! for what we want to test here, it's actually
		 * fine to just normalize the line separator. */
		xmlFileContent = StringTools.normalizeLineSeparators(xmlFileContent, "\n");

		// since we had some problems with line separators on Windows here, we first ensure that we only have unix line
		// feeds in the file
		assertThat(xmlFileContent).doesNotContain("\r");

		// get result of toString
		String toStringResult = DOMTools.toString(DOMTools.parse(xmlFileContent));

		/* the next check would fail on Windows, because toStringResult contains '\r'. TODO: unclear why! for what we want to test here, it's actually
		 * fine to just normalize the line separator. */
		toStringResult = StringTools.normalizeLineSeparators(toStringResult, "\n");

		// again ensure that we have only line feeds
		assertThat(toStringResult).doesNotContain("\r");

		assertThat(toStringResult).isEqualToWithVerboseErrorMessage(xmlFileContent);
	}

	/**
	 * Makes sure two root elements are not allowed.
	 */
	@Test
	public void testToString_TwoRootElements() throws Exception {
		String xmlString = "<a/><b/>";
		try {
			DOMTools.stringToDocument(xmlString);
			fail();
		} catch (DOMException e) {
			// expected
		}
	}

	@Test
	public void testToFormattedString_1() throws Exception {
		// @formatter:off
		final String sampleXML =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<!--\n" +
				"\tcomment 1\n" +
				"\tcomment 2\n" +
				"-->\n" +
				"<!--\tcomment 3-->\n" +
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"" +
				" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
				" xsi:schemaLocation=\" http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\">" +
					" <bean id=\"person.pete\" class=\"com.braintribe.model.processing.deployment.expert.spring.Person\">" +
						"<property name=\"name\" value=\"Pete\"/>" +
						"<property name=\"age\" value=\"19\"/>" +
					"  </bean>" +
				" \t</beans>\t \n"+
				"<!--\tcomment 4-->";
		// @formatter:on

		final Document document = DOMTools.stringToDocument(sampleXML);
		final String formattedString = DOMTools.toFormattedString(document);
		// System.out.println(formattedString);
		final String xmlDeclaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		assertThat(formattedString).doesNotContain(xmlDeclaration);
		assertThat(formattedString).contains("person.pete");
		assertThat(formattedString).contains("    <bean"); // check indentation added
	}

	@Test
	public void testToFormattedString_namespace() {
		final String xmlDeclaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		final String namespace = "xmlns=\"urn:example/abc/XYZ\"";
		final String xml = xmlDeclaration + "<CreateNewOpportunity_Result " + namespace
				+ "><opportunityNr>1814/0036</opportunityNr></CreateNewOpportunity_Result>";
		final String formattedXml = DOMTools.format(xml, true);

		// System.out.println(formattedXml);
		// namespace must be included
		assertThat(formattedXml).contains(namespace);
	}

	@Test
	public void testToFormattedString_alternative() {
		String xml = FileTools.readStringFromFile(toFormattedStringTest);

		Document document = DOMTools.parse(xml);

		String result1 = DOMTools.toFormattedString(document);
		String result2 = DOMTools.toFormattedString_alternative(document);

		// File resultFile1 = new File("res/DomToolsTest/FormattingTest_toFormattedString.xml");
		// File resultFile2 = new File("res/DomToolsTest/FormattingTest_toFormattedString_alternative.xml");
		// System.out.println("[diff_tool] " + resultFile1.getAbsolutePath() + " " + resultFile2.getAbsolutePath());
		// FileTools.writeStringToFile(resultFile1, result1);
		// FileTools.writeStringToFile(resultFile2, result2);

		// for both implementations attributes must be ordered by name
		assertThat(result1).contains("<Attributes a=\"a\" b=\"b\" c=\"c\" d=\"d\"");
		assertThat(result2).contains("<Attributes a=\"a\" b=\"b\" c=\"c\" d=\"d\"");
	}

	@Test
	public void testToFormattedString_xmlDeclaration() {
		final String xmlDeclaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

		final String xmlStringWithoutDeclaration = "<root/>";
		final String xmlStringWithDeclaration = xmlDeclaration + xmlStringWithoutDeclaration;

		Document xmlWithoutDeclaration = DOMTools.parse(xmlStringWithoutDeclaration);
		Document xmlWithDeclaration = DOMTools.parse(xmlStringWithDeclaration);

		final String xmlStringWithoutDeclarationPlusNewLine = xmlStringWithoutDeclaration + "\n";

		assertThat(DOMTools.toString(xmlWithoutDeclaration)).isEqualTo(xmlStringWithoutDeclaration);
		assertThat(DOMTools.toString(xmlWithDeclaration)).isEqualTo(xmlStringWithoutDeclaration);

		assertThat(DOMTools.toFormattedString(xmlWithoutDeclaration)).isEqualTo(xmlStringWithoutDeclarationPlusNewLine);
		assertThat(DOMTools.toFormattedString(xmlWithDeclaration)).isEqualTo(xmlStringWithoutDeclarationPlusNewLine);

		// TODO: commented out lines work on linux, but fail on windows? (line separator issue?)
		// assertThat(DOMTools.toFormattedString_alternative(xmlWithoutDeclaration)).isEqualTo(xmlStringWithoutDeclarationPlusNewLine);
		// assertThat(DOMTools.toFormattedString_alternative(xmlWithDeclaration)).isEqualTo(xmlStringWithoutDeclarationPlusNewLine);
		assertThat(DOMTools.toFormattedString_alternative(xmlWithoutDeclaration).trim()).isEqualTo(xmlStringWithoutDeclarationPlusNewLine.trim());
		assertThat(DOMTools.toFormattedString_alternative(xmlWithDeclaration).trim()).isEqualTo(xmlStringWithoutDeclarationPlusNewLine.trim());
	}

	@Test
	public void testInsertBefore() throws Exception {
		final Document sourceDocument = DOMTools.stringToDocument("<root><b/><c><c1/><c2/></c><d/></root>");
		final Document targetDocument = DOMTools.stringToDocument("<root><a/><replace/><e/></root>");
		final Document expectedTargetDocument = DOMTools.stringToDocument("<root><a/><b/><c><c1/><c2/></c><d/><e/></root>");

		// System.out.println("Source:\n" + DOMTools.format(sourceDocument));
		// System.out.println("Target:\n" + DOMTools.format(targetDocument));

		DOMTools.insertBefore(sourceDocument, "/root/*", targetDocument, "/root/replace", true);

		// System.out.println("Source:\n" + DOMTools.format(sourceDocument));
		// System.out.println("Target:\n" + DOMTools.format(targetDocument));

		assertThat(DOMTools.format(targetDocument)).isEqualTo(DOMTools.format(expectedTargetDocument));
	}

	@Test
	public void testAppendChild() throws Exception {
		Document document = DOMTools.newDocumentBuilder().newDocument();
		Element root = document.createElement("root");
		document.appendChild(root);
		DOMTools.addChildElement(root, "child");
		String description = DOMTools.toFormattedString(document);
		assertThat(description).contains("child");
	}

	@Test
	public void testGetElementByXPath() throws Exception {
		String xmlString = "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><s:Fault><faultstring xml:lang=\"de-AT\">message</faultstring></s:Fault></s:Body></s:Envelope>";
		Document document = DOMTools.stringToDocument(xmlString);
		String xpath = "//faultstring[@*[local-name()='lang']='de-AT']";
		Element faultStringElement = DOMTools.getElementByXPath(document.getDocumentElement(), xpath);
		assertThat(faultStringElement).isNotNull();
		assertThat(faultStringElement.getTextContent()).isEqualTo("message");
	}

	@Test
	public void testGetDeclaration() {
		String declaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		assertThat(DOMTools.getDeclaration(declaration)).isEqualTo(declaration);
		assertThat(DOMTools.getDeclaration(declaration + "<root/>")).isEqualTo(declaration);
		assertThat(DOMTools.getDeclaration(" " + declaration + "<root/>")).isNull();
		assertThat(DOMTools.getDeclaration(declaration + "  \n\t<root/>")).isEqualTo(declaration);
	}

	@Test
	public void testParseDeclaration() throws Exception {
		assertThat(DOMTools.parseVersionFromDeclaration("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")).isEqualTo("1.0");
		assertThat(DOMTools.parseVersionFromDeclaration("<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"yes\" ?>")).isEqualTo("1.1");
		try {
			String result = DOMTools.parseVersionFromDeclaration("<?xml encoding=\"UTF-8\"?>");
			fail("version is mandatory, but no exception thrown! parsed version: " + result);
		} catch (IllegalArgumentException e) {
			// expected
		}

		assertThat(DOMTools.parseEncodingFromDeclaration("<?xml version=\"1.0\" encoding=\"UTF-16\" standalone=\"yes\" ?>")).isEqualTo("UTF-16");
		assertThat(DOMTools.parseEncodingFromDeclaration("<?xml version=\"1.0\" standalone=\"no\" ?>")).isEqualTo(null);

		assertThat(DOMTools.parseStandaloneFromDeclaration("<?xml version=\"1.0\" encoding=\"UTF-16\" standalone=\"yes\" ?>")).isEqualTo(true);
		assertThat(DOMTools.parseStandaloneFromDeclaration("<?xml version=\"1.0\" standalone=\"no\" ?>")).isEqualTo(false);
		assertThat(DOMTools.parseStandaloneFromDeclaration("<?xml version=\"1.0\" ?>")).isEqualTo(null);
		try {
			Boolean result = DOMTools.parseStandaloneFromDeclaration("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"INVALIDVALUE\" ?>");
			fail("standalone value is invalid, but no exception thrown! result: " + result);
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testSpecialEscape() throws Exception {
		/* This snippet was found in the POM for artifact org.codehaus.plexus:plexus-1.0.4. Standard XML parsers are not (supposed to be) able to
		 * parse this. --> */
		String xmlString = "<developer><name>Trygve Laugst&oslash;l</name></developer>";
		try {
			Document document = DOMTools.parse(xmlString);
			fail("Unexpectedly was able to parse XML with invalid escapes:\n" + DOMTools.toString(document));
		} catch (DOMException e) {
			// expected
		}

		// adding doctype fixes the issue (see https://www.w3.org/TR/xhtml1/dtds.html#h-A2)
		xmlString = "<!DOCTYPE definition [\n" + "<!ENTITY oslash \"&#248;\">\n" + "]>\n" + xmlString;

		Document document = DOMTools.parse(xmlString);
		String formattedXmlString = DOMTools.toString(document);
		assertThat(formattedXmlString.contains("ø"));
	}
}
