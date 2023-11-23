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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.math.BigDecimal;
import java.util.Map;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

import com.braintribe.common.DomDiff;
import com.braintribe.common.StringDiff.DiffResult;
import com.braintribe.logging.Logger;
import com.braintribe.testing.tools.TestTools;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.DOMTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.MapTools;
import com.braintribe.utils.OsTools;
import com.braintribe.utils.saxon.SaxonTools.FormattingOptions;

/**
 * Provides tests for {@link SaxonTools}.
 *
 * @author michael.lafite
 */
public class SaxonToolsTest {

	private static Logger logger = Logger.getLogger(SaxonToolsTest.class);

	private static final File TEXTONLY = new File("res/textonly.xml");
	private static final File TEXTONLY_TRANSFORMATION = new File("res/textonly.xslt");
	private static final File PERSON = new File("res/person.xml");
	private static final File PERSON_TRANSFORMATION = new File("res/person.xslt");
	private static final File PARAM = new File("res/param.xml");
	private static final File PARAM_TRANSFORMATION = new File("res/param.xslt");

	private static final File FORMATTEST = new File("res/formattest.xml");
	private static final File FORMATTEST_XSLTFORMATTED = new File("res/transformationResults/formattest-xsltformatted.xml");

	private static final File ICC_LOCAL = new File("res/icc-local.spring.xml");
	private static final File ICC_LOCAL_LOCAL2FIDES_TRANSFORMATION = new File("res/icc-local.spring.xslt");
	private static final File ICC_FIDES = new File("res/icc-fides.spring.xml");
	private static final File ICC_LOCAL_FORMATTED = new File("res/transformationResults/icc-local-formatted.spring.xml");
	private static final File ICC_FIDES_FORMATTED = new File("res/transformationResults/icc-fides-formatted.spring.xml");

	private static final File ICC_LOCAL_XSLTFORMATTED = new File("res/transformationResults/icc-local-xsltformatted.spring.xml");
	private static final File ICC_FIDES_XSLTFORMATTED = new File("res/transformationResults/icc-fides-xsltformatted.spring.xml");

	private static final File ICC_LOCAL2FIDES = new File("res/transformationResults/icc-local2fides.spring.xml");
	private static final File ICC_LOCAL2FIDES_FORMATTED = new File("res/transformationResults/icc-local2fides-formatted.spring.xml");

	// setting to true enables logging and file writing (only needed for local tests)
	private static final boolean LOCAL_DEVELOPMENT_MODE_ENABLED = false;

	@Test
	public void testApplyXslt_textonly() {
		String result = applyXslt(TEXTONLY, TEXTONLY_TRANSFORMATION);
		assertThat(result).isEqualTo("This test will be extracted using XSLT.");
	}

	@Test
	public void testApplyXslt_person() {
		String result = applyXslt(PERSON, PERSON_TRANSFORMATION);
		assertThat(result).startsWith("<!-- Example Header Comment --><Person");
		assertThat(result).endsWith("</Person>");
	}

	@Test
	public void testApplyXslt_param() {

		Map<String, Object> parameters = MapTools.getParameterizedMap(String.class, Object.class, "someString", "abc", "someInteger", 123, "someLong",
				Long.MAX_VALUE, "someFloat", 1234.56f, "someDouble", 128376.1278936981d, "someBigDecimal",
				new BigDecimal("178236178263781236781236781623781623.12389712398718923718927389173897123897"), "someBoolean", true, "someURI",
				new File("/a/b/c").toURI());

		String result = applyXslt(PARAM, PARAM_TRANSFORMATION, parameters);
		assertThat(result).startsWith("<ParamTest");
		assertThat(result).contains("someString>abc");
		assertThat(result).contains("someInteger>123");
		assertThat(result).contains("someLong>" + Long.MAX_VALUE);
		assertThat(result).contains("someFloat>1234.56");
		assertThat(result).contains("someDouble>128376.1278936981");
		assertThat(result).contains("someBigDecimal>178236178263781236781236781623781623.12389712398718923718927389173897123897");
		assertThat(result).contains("someBoolean>true");
		if (OsTools.isWindowsOperatingSystem()) {
			assertThat(result).contains("someURI>file:/C:/a/b/c");
		} else {
			assertThat(result).contains("someURI>file:/a/b/c");
		}
		assertThat(result).endsWith("</ParamTest>");
	}

	@Test
	public void testApplyXslt_icc() {

		String iccFides = FileTools.readStringFromFile(ICC_FIDES);
		String iccLocal2Fides = applyXslt(ICC_LOCAL, ICC_LOCAL_LOCAL2FIDES_TRANSFORMATION);

		DomDiff domDiff = new DomDiff();
		domDiff.setCommentsIncluded(false);
		domDiff.setFormattingEnabled(true);

		DiffResult diffResult = domDiff.compare(iccLocal2Fides, iccFides);

		if (LOCAL_DEVELOPMENT_MODE_ENABLED) {
			String iccLocal2FidesFormatted = diffResult.getFirst();
			String iccFidesFormatted = diffResult.getSecond();
			String iccLocalFormatted = domDiff.toComparableXmlString(FileTools.readStringFromFile(ICC_LOCAL));

			FileTools.writeStringToFile(ICC_FIDES_FORMATTED, iccFidesFormatted);
			FileTools.writeStringToFile(ICC_LOCAL_FORMATTED, iccLocalFormatted);
			FileTools.writeStringToFile(ICC_LOCAL2FIDES, iccLocal2Fides);
			FileTools.writeStringToFile(ICC_LOCAL2FIDES_FORMATTED, iccLocal2FidesFormatted);

			if (!diffResult.hasDifference()) {
				logger.info("Successfully transformed icc-local to icc-fides. Formatted transformation result:\n" + iccLocal2Fides);
			} else {
				logger.info(diffResult.getFirstDifferenceDescription());
				logger.info("Transformation Result Diff:\n[diff] " + ICC_FIDES.getAbsolutePath() + " " + ICC_LOCAL2FIDES.getAbsolutePath());
				logger.info("Transformation Result Diff (formatted):\n[diff] " + ICC_FIDES_FORMATTED.getAbsolutePath() + " "
						+ ICC_LOCAL2FIDES_FORMATTED.getAbsolutePath());
			}
		}

		assertThat(diffResult.hasDifference()).isFalse();
	}

	@Test
	public void testFormat() {
		File iccLocalDestination;
		File iccFidesDestination;
		File formattestDestination;

		if (!LOCAL_DEVELOPMENT_MODE_ENABLED) {
			iccLocalDestination = newTempXmlFile("icc-local-xsltformatted");
			iccFidesDestination = newTempXmlFile("icc-fides-xsltformatted");
			formattestDestination = newTempXmlFile("formattest-xsltformatted");
		} else {
			iccLocalDestination = ICC_LOCAL_XSLTFORMATTED;
			iccFidesDestination = ICC_FIDES_XSLTFORMATTED;
			formattestDestination = FORMATTEST_XSLTFORMATTED;

			iccLocalDestination.delete();
			iccFidesDestination.delete();
			formattestDestination.delete();
		}

		FormattingOptions options = new FormattingOptions().omitXmlDeclaration(false).standalone(null);

		SaxonTools.formatToFile(ICC_LOCAL, iccLocalDestination, options);
		SaxonTools.formatToFile(ICC_FIDES, iccFidesDestination, options);
		SaxonTools.formatToFile(FORMATTEST, formattestDestination, options);

		if (LOCAL_DEVELOPMENT_MODE_ENABLED) {
			logger.info("[diff] " + ICC_LOCAL.getAbsolutePath() + " " + iccLocalDestination.getAbsolutePath());
			logger.info("[diff] " + ICC_FIDES.getAbsolutePath() + " " + iccFidesDestination.getAbsolutePath());
			logger.info("[diff] " + FORMATTEST.getAbsolutePath() + " " + formattestDestination.getAbsolutePath());
		}
	}

	@Test
	public void testInvalidEscape() {
		/*
		 *  This snippet was found in the POM for artifact org.codehaus.plexus:plexus-1.0.4.
		 *  Standard XML parsers are not (supposed to be) able to parse this. -->
		 */
		String xmlString = "<developer><name>Trygve Laugst&oslash;l</name></developer>";
		
		FormattingOptions options = new FormattingOptions().omitXmlDeclaration(false).standalone(null);
		
		try {
			String formattedXmlString = SaxonTools.formatToString(xmlString, options);
			fail("Unexpectedly was able to parse XML with invalid escapes: " + formattedXmlString);
		} catch (UncheckedSaxonApiException e) {
			// expected
		}
	}
	
	private static String applyXslt(File xml, File xslt) {
		return applyXslt(xml, xslt, null);
	}

	private static String applyXslt(File xml, File xslt, Map<String, Object> parameters) {
		String domModeResult = null;
		{
			Document xmlDocument = DOMTools.parse(FileTools.readStringFromFile(xml));
			Document xsltDocument = DOMTools.parse(FileTools.readStringFromFile(xslt));
			DocumentFragment resultDocumentFragment = SaxonTools.applyXslt(xmlDocument, xsltDocument, parameters);
			domModeResult = DOMTools.toString(resultDocumentFragment);
		}

		String fileModeResult = null;
		{
			File destination = newTempXmlFile();
			SaxonTools.applyXslt(xml, xslt, destination, parameters);
			fileModeResult = FileTools.readStringFromFile(destination);

			if (LOCAL_DEVELOPMENT_MODE_ENABLED) {
				logger.info("Transformed File Content:\n" + fileModeResult);
			}
		}

		/* Attributes ordered by name in DomMode, but order is not changed for file mode. Therefore there can be
		 * differences between the modes. */
		// String fileModeResultWithoutDeclaration = DOMTools.removeXmlDeclarationPrefix(fileModeResult);
		// assertThat(fileModeResultWithoutDeclaration).isEqualTo(domModeResult);

		return domModeResult;
	}

	private static File newTempXmlFile(String filenNamePart) {
		return TestTools.newTempFile(SaxonToolsTest.class.getSimpleName() + "_" + filenNamePart + ".xml");
	}

	private static File newTempXmlFile() {
		return newTempXmlFile(CommonTools.getInvokingMethodName());
	}
}
