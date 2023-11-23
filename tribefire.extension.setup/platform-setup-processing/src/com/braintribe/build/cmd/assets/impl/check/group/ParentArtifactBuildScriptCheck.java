package com.braintribe.build.cmd.assets.impl.check.group;

import static com.braintribe.build.cmd.assets.impl.check.group.GroupCheckDomHelpers.PARENT_IMPORT_SCRIPT;
import static com.braintribe.build.cmd.assets.impl.check.group.GroupCheckDomHelpers.extractBtImportElement;
import static com.braintribe.utils.lcd.CollectionTools2.acquireList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.braintribe.build.cmd.assets.impl.check.api.Artifact;
import com.braintribe.build.cmd.assets.impl.check.api.GroupCheck;
import com.braintribe.build.cmd.assets.impl.check.api.GroupCheckContext;
import com.braintribe.build.cmd.assets.impl.check.process.ResultStatus;
import com.braintribe.common.lcd.NotImplementedException;
import com.braintribe.utils.lcd.StringTools;

public class ParentArtifactBuildScriptCheck implements GroupCheck {

	@Override
	public ResultStatus execute(GroupCheckContext context) {

		Map<String, List<String>> importScriptToArtifactNames = newMap();

		for (Artifact artifact : context.getArtifacts()) {
			File artifactFolder = new File(context.getGroupFolder(), artifact.getArtifactId());
			File artifactBuildXmlFile = new File(artifactFolder, "build.xml");

			Element extractedBtImportElement = extractBtImportElement(artifactBuildXmlFile);
			if (extractedBtImportElement == null || StringTools.isEmpty(extractedBtImportElement.getAttribute("artifact"))) {
				context.addResultDetailedInfo("Artifact " + artifact.getArtifactId() + " is missing a build script.");
			} else {
				acquireList(importScriptToArtifactNames, extractedBtImportElement.getAttribute("artifact")).add(artifact.getArtifactId());
			}
		}

		final List<String> artifactsWithParentScript = nullSafe(importScriptToArtifactNames.get(PARENT_IMPORT_SCRIPT));

		if (artifactsWithParentScript.size() == 1 && artifactsWithParentScript.get(0).equals("parent")) {
			return ResultStatus.success;
		}

		if (artifactsWithParentScript.isEmpty()) {
			context.addResultDetailedInfo("Wrong or missing import script in parent artifact.");
		} else {
			context.addResultDetailedInfo("Found more than one artifacts ( " + String.join(", ", artifactsWithParentScript)
					+ " ) with import script '" + PARENT_IMPORT_SCRIPT + "' in their build.xml files.");
		}

		return ResultStatus.error;
	}

	@Override
	public String getTitle() {
		return "Check if the artifact called parent (and only that one) imports expected ant script (i.e. parent-ant-script).";
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
