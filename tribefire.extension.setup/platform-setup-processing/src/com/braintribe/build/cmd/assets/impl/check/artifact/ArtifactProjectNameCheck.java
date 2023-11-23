package com.braintribe.build.cmd.assets.impl.check.artifact;

import static com.braintribe.build.cmd.assets.impl.check.group.GroupCheckDomHelpers.extractProjectNameElement;

import java.io.File;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.braintribe.build.cmd.assets.impl.check.api.ArtifactCheck;
import com.braintribe.build.cmd.assets.impl.check.api.ArtifactCheckContext;
import com.braintribe.build.cmd.assets.impl.check.process.ResultStatus;
import com.braintribe.utils.DOMTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.StringTools;

public class ArtifactProjectNameCheck implements ArtifactCheck {

	@Override
	public ResultStatus execute(ArtifactCheckContext context) {
		File artifactProjectXmlFile = context.getArtifactProjectXml();
		if (!artifactProjectXmlFile.exists()) {
			context.addResultDetailedInfo("Skipping check, because .project file doesn't exist. (path: " + artifactProjectXmlFile.getPath()
					+ ", absolute path: " + artifactProjectXmlFile.getAbsolutePath() + ")");
			return ResultStatus.skipped;
		}
		Element extractedProjectNameElement = extractProjectNameElement(artifactProjectXmlFile);

		if (extractedProjectNameElement == null) {
			context.addResultDetailedInfo("Artifact .project file does not contain name element.");
			return ResultStatus.error;
		}
		String expectedProjectName = context.getArtifact().getArtifactId() + " - " + context.getGroupId();
		String actualProjectName = extractedProjectNameElement.getTextContent();

		if (!actualProjectName.equals(expectedProjectName)) {
			context.addResultDetailedInfo(
					"Project name '" + actualProjectName + "' in .project file is not equal to expected value: " + expectedProjectName);
			return ResultStatus.error;
		}
		return ResultStatus.success;
	}

	@Override
	public String getTitle() {
		return "Check if artifact .project file contains correct name (e.g. example-artifact - org.example).";
	}

	@Override
	public ResultStatus fixError(ArtifactCheckContext context) {
		File artifactProjectXmlFile = context.getArtifactProjectXml();

		Node nameElement = extractProjectNameElement(artifactProjectXmlFile);

		if (nameElement == null) {
			context.addResultDetailedInfo("Project file " + artifactProjectXmlFile.getPath() + " doesn't even contain a project name element."
					+ " This is unexpected and must be fixed manually.");
			return ResultStatus.error;
		}

		String expectedProjectName = context.getArtifact().getArtifactId() + " - " + context.getGroupId();
		nameElement.setTextContent(expectedProjectName);

		// since we are already normalizing things, we also format the file
		// (this is fine, but should be moved to another check/fix, which in general ensures correct indentation in XML files, e.g. also POMs or .classpath files)
		String domElementAsXmlString = DOMTools.format(nameElement.getOwnerDocument());
		// ensure line separators not mixed and line separator at end of file
		domElementAsXmlString = StringTools.normalizeLineSeparatorsInTextFileString(domElementAsXmlString, "\n");
		// enforce tabs
		domElementAsXmlString = replaceSpacesWithTabs(domElementAsXmlString);
		
		FileTools.writeStringToFile(artifactProjectXmlFile, replaceSpacesWithTabs(domElementAsXmlString));

		return ResultStatus.success;
	}

	/**
	 * Replaces each 4 leading spaces with a tab character.
	 */
	private String replaceSpacesWithTabs(String inputString) {
		Pattern pattern = Pattern.compile("^(    )+", Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(inputString);

		// TODO: switch to StringBuilder after switch to Java 9+
		StringBuffer result = new StringBuffer();

		while (matcher.find()) {
			int length = matcher.group().length() / 4;
			String replacement = String.join("", Collections.nCopies(length, "\t"));
			matcher.appendReplacement(result, replacement);
		}
		matcher.appendTail(result);
		return result.toString();
	}

	@Override
	public boolean fixable() {
		return true;
	}
}
