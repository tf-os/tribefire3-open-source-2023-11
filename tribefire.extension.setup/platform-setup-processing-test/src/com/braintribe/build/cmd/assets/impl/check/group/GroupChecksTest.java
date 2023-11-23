package com.braintribe.build.cmd.assets.impl.check.group;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;
import org.w3c.dom.Element;

import com.braintribe.testing.test.AbstractTest;

public class GroupChecksTest extends AbstractTest {

	@Test
	public void GroupBuildScriptExistImportRightScriptCheck() {
		File parentBuildXmlFile = testFile("build.xml");
		Element extractBtImportElement = GroupCheckDomHelpers.extractBtImportElement(parentBuildXmlFile);
		assertThat(extractBtImportElement.hasAttribute("artifact")).isTrue();
		assertThat(extractBtImportElement.getAttribute("artifact")).isEqualTo(GroupCheckDomHelpers.GROUP_IMPORT_SCRIPT);
	}
}
