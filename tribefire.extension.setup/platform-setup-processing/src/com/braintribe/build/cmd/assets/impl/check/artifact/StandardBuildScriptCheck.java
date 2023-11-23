// ========================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2022 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission.
// To this file the Braintribe License Agreement applies.
// ========================================================================
package com.braintribe.build.cmd.assets.impl.check.artifact;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.braintribe.build.cmd.assets.impl.check.api.ArtifactCheck;
import com.braintribe.build.cmd.assets.impl.check.api.ArtifactCheckContext;
import com.braintribe.build.cmd.assets.impl.check.process.ResultStatus;
import com.braintribe.common.lcd.NotImplementedException;
import com.braintribe.utils.DOMTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.StringTools;

/**
 * Checks whether the respective artifacts uses a standard build script, i.e. its build script only has a single import line which imports a script
 * from {@value #STANDARD_BUILD_SCRIPTS_GROUP}.
 * 
 * @author michael.lafite
 */
public class StandardBuildScriptCheck implements ArtifactCheck {

	public static final String STANDARD_BUILD_SCRIPTS_GROUP = "com.braintribe.devrock.ant";
	private static final String IMPORT_NAME = "import";
	private static final String IMPORT_NAMESPACE = "antlib:com.braintribe.build.ant.tasks";

	@Override
	public ResultStatus execute(ArtifactCheckContext context) {
		File artifactBuildXmlFile = context.getArtifactBuildXml();

		if (!artifactBuildXmlFile.exists()) {
			context.addResultDetailedInfo("Skipping check, because build script doesn't exist. (path: " + artifactBuildXmlFile.getPath()
					+ ", absolute path: " + artifactBuildXmlFile.getAbsolutePath() + ")");
			return ResultStatus.skipped;
		}

		Document buildScriptDocument = DOMTools.parse(FileTools.readStringFromFile(artifactBuildXmlFile), true);

		Element rootElement = buildScriptDocument.getDocumentElement();

		if (!rootElement.getNodeName().equals("project")) {
			context.addResultDetailedInfo("Build script's root element unexpectedly is '" + rootElement.getNodeName() + "'!");
			return ResultStatus.error;
		}

		Node childNode = rootElement.getFirstChild();
		boolean importElementFound = false;

		while (childNode != null) {
			if (childNode instanceof Element) {
				Element childElement = (Element) childNode;

				if (IMPORT_NAMESPACE.equals(childElement.getNamespaceURI()) && IMPORT_NAME.equals(childElement.getLocalName())) {
					if (importElementFound) {
						context.addResultDetailedInfo("Multiple imports found!");
						return ResultStatus.error;
					}
					importElementFound = true;
					String artifact = childElement.getAttribute("artifact");
					if (StringTools.isBlank(artifact)) {
						context.addResultDetailedInfo("Import doesn't specify an artifact!");
						return ResultStatus.error;
					}
					if (!artifact.startsWith(STANDARD_BUILD_SCRIPTS_GROUP + ":")) {
						context.addResultDetailedInfo("Import does not specify an artifact from group " + STANDARD_BUILD_SCRIPTS_GROUP + "!");
						return ResultStatus.error;
					}
				} else {
					context.addResultDetailedInfo(
							"Unexpected element '" + childElement.getTagName() + "'! Only import element is allowed in standard build scripts.");
					return ResultStatus.error;
				}
			}
			childNode = childNode.getNextSibling();
		}
		return ResultStatus.success;
	}

	@Override
	public String getTitle() {
		return "Check if the artifact uses a standard build script.";
	}

	@Override
	public ResultStatus fixError(ArtifactCheckContext context) {
		throw new NotImplementedException();
	}

	@Override
	public boolean fixable() {
		return false;
	}
}
