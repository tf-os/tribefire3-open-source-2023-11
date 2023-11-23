// ========================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2020 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission.
// To this file the Braintribe License Agreement applies.
// ========================================================================

package com.braintribe.build.cmd.assets.impl;

import java.io.File;

import org.junit.Test;
import org.w3c.dom.Element;

import com.braintribe.setup.tools.CheckLicenseProvider;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.testing.test.AbstractTest;

/**
 * Provides {@link CheckLicenseProcessor} tests.
 *
 * @author Ralf Ulrich
 */
public class CheckLicenseProcessorTest extends AbstractTest implements CheckLicenseProvider {

	private static final String testFileName = "/foobar.txt";

	@Override
	public Element getPomLicenseFragment() {
		return null;
	}
	
	@Test
	public void testCRLF() {
		Assertions.assertThat(checkCRLF("test \n test")).isFalse();
		Assertions.assertThat(checkCRLF("test \r\n test")).isTrue();
	}
	
	@Test
	public void testXml() {
		assertCheckResult(checkLicenseXml("<nothing/><!-- license\n -->\n<tag/>"), //
				null, "", "<nothing/><!-- license\n -->\n<tag/>", false);

		assertCheckResult(checkLicenseXml("<?strange?>\n\n   <!-- license\n -->\n<tag/>"), //
				"<?strange?>", " license\n ", "<tag/>", true);

		assertCheckResult(checkLicenseXml("<?strange?>\n\n   <!-- lcns\n -->\n<tag/>"), //
				"<?strange?>", "", "<!-- lcns\n -->\n<tag/>", false);

		assertCheckResult(checkLicenseXml("<?strange?>\n\n   <tag/>"), //
				"<?strange?>", "", "<tag/>", false);

		assertCheckResult(checkLicenseXml("<tag1/>\n<tag>nacht</tag>"), //
				null, "", "<tag1/>\n<tag>nacht</tag>", false);

		assertCheckResult(checkLicenseXml("\n\n   <!-- LiCeNsE -->\n<tag/>"), //
				null, " LiCeNsE ", "<tag/>", true);

		assertCheckResult(checkLicenseXml("\n\n   <!-- LiCeNsE -->\n<tag/>\n"), //
				null, " LiCeNsE ", "<tag/>\n", true);

		assertCheckResult(checkLicenseXml("<!--\n LiCeNsE \n blah --><tag\n>\n"), //
				null, "\n LiCeNsE \n blah ", "<tag\n>\n", true);

	}

	@Test
	public void testJava() {
		assertCheckResult(checkLicenseJava("// BS \n code"), null, "", "// BS \n code", false);
		assertCheckResult(checkLicenseJava("// BS license\n // more\n\n\t code"), null, "BS license\nmore\n", "code", true);
		assertCheckResult(checkLicenseJava("// BS license\n code\n"), null, "BS license\n", "code\n", true);
		assertCheckResult(checkLicenseJava("anything\n// BS license\n \t code"), null, "", "anything\n// BS license\n \t code", false);
		assertCheckResult(checkLicenseJava("/* BS */\n code"), null, "", "/* BS */\n code", false);
		assertCheckResult(checkLicenseJava("/* BS license\n more\n*/\n\t code"), null, "BS license\n more\n", "code", true);
		assertCheckResult(checkLicenseJava("anything\n/* BS license\n  more\n\n*/\t code"), null, "",
				"anything\n/* BS license\n  more\n\n*/\t code", false);
		assertCheckResult(checkLicenseJava("anything\n/*more\n\n*/\t code\n"), null, "", "anything\n/*more\n\n*/\t code\n", false);
	}

	@Test
	public void testLicense() {
		Assertions.assertThat(checkLicense(new CheckLicenseResult(testFileName, "one", "two", "threeeee"), new File(".")).result).isTrue();
		Assertions.assertThat(checkLicense(new CheckLicenseResult(testFileName, "", "two", "threeeee"), new File(".")).result).isTrue();
		Assertions.assertThat(checkLicense(new CheckLicenseResult(testFileName, "one", "two", ""), new File(".")).result).isTrue();
		Assertions.assertThat(checkLicense(new CheckLicenseResult(testFileName, "", getHeaderText(), ""), new File(".")).result).isFalse();
		String test = checkLicense(new CheckLicenseResult(testFileName, "", "fail copyright form this 3333", ""), new File(".")).license;

		String compareTo = "und wenn sie nicht gestorben\n sind dann leben sie noch heute\ndas copyriGht ist von Gretel 1742";
		Assertions.assertThat(test).isEqualTo(compareTo);

		String orgLic = """
				 ============================================================================
				 BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
				 Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
				 It is strictly forbidden to copy, modify, distribute or use this code without written permission
				 To this file the Braintribe License Agreement applies.
				 ============================================================================
				""";

		Assertions.assertThat(checkLicense(new CheckLicenseResult(testFileName, null, orgLic, "<tag>"), new File(".")).result).isTrue();
		Assertions.assertThat(checkLicense(new CheckLicenseResult(testFileName, null, orgLic, "<tag>"), new File(".")).license).isEqualTo(compareTo);
	}

	@Test
	public void testAssemble() {
		Assertions.assertThat(assembleNewData(new CheckLicenseResult(testFileName, null, "two", "three"), MODE.JAVA)).isEqualTo("// ============================================================================\n// two\n// ============================================================================\nthree");
		Assertions.assertThat(assembleNewData(new CheckLicenseResult(testFileName, "test\n", "two", "three"), MODE.XML)).isEqualTo("test\n\n<!--\ntwo\n-->\nthree");
		Assertions.assertThat(assembleNewData(new CheckLicenseResult(testFileName, "test", "two", "three"), MODE.OTHER)).isEqualTo("test\nthree");
	}

	private void assertCheckResult(CheckLicenseResult check, String testPre, String testLic, String testBod, boolean testResult) {
		Assertions.assertThat(check.result == testResult);
		Assertions.assertThat(check.prefix).isEqualTo(testPre);
		Assertions.assertThat(check.license).isEqualTo(testLic);
		Assertions.assertThat(check.body).isEqualTo(testBod);
	}

	/////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean getCheckOnly() {
		return true;
	}

	@Override
	public void addErrorMsg(String string) {
	}

	@Override
	public String getHeaderText() {
		return "und wenn sie nicht gestorben\n sind dann leben sie noch heute\ndas copyriGht ist von Gretel 1742";
	}

}
