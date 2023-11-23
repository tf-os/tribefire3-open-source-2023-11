package com.braintribe.build.cmd.assets.impl.check.group;

import static com.braintribe.build.cmd.assets.impl.check.group.GroupCheckDomHelpers.GROUP_IMPORT_SCRIPT;
import static com.braintribe.build.cmd.assets.impl.check.group.GroupCheckDomHelpers.extractBtImportElement;

import java.io.File;

import org.w3c.dom.Element;

import com.braintribe.build.cmd.assets.impl.check.api.GroupCheck;
import com.braintribe.build.cmd.assets.impl.check.api.GroupCheckContext;
import com.braintribe.build.cmd.assets.impl.check.process.ResultStatus;
import com.braintribe.common.lcd.NotImplementedException;

public class GroupBuildScriptExistImportRightScriptCheck implements GroupCheck {

	@Override
	public String getTitle() {
		return "Check if group build script imports correct ant script.";
	}

	@Override
	public ResultStatus execute(GroupCheckContext context) {
		File groupBuildXmlFile = context.getGroupBuildXml();

		Element extractedBtImportElement = extractBtImportElement(groupBuildXmlFile);
		if (extractedBtImportElement == null) {
			context.addResultDetailedInfo("Group build script does not have import element.");
			return ResultStatus.error;
		}
		if (!extractedBtImportElement.hasAttribute("artifact")) {
			context.addResultDetailedInfo("Group build script import element does not have 'artifact' attribute.");
			return ResultStatus.error;
		}
		if (extractedBtImportElement.getAttribute("artifact").equals(GROUP_IMPORT_SCRIPT)) {
			return ResultStatus.success;
		}
		context.addResultDetailedInfo(
				"Group build script import element does not have expected attribute value for attribute 'artifact'. Expected value: "
						+ GROUP_IMPORT_SCRIPT + ". Actual value: " + extractedBtImportElement.getAttribute("artifact"));
		return ResultStatus.error;

	}

	@Override
	public ResultStatus fixError(GroupCheckContext context) {
		throw new NotImplementedException();
	}

	@Override
	public boolean fixable() {
		return false;
	}
}
