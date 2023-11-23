// ========================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2021 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission.
// To this file the Braintribe License Agreement applies.
// ========================================================================
package com.braintribe.build.cmd.assets.impl;

import static com.braintribe.console.ConsoleOutputs.println;

import java.io.File;

import org.w3c.dom.Element;

import com.braintribe.build.cmd.assets.impl.UpdateGroupVersionProcessor.MajorMinorVersion;
import com.braintribe.build.cmd.assets.impl.UpdateGroupVersionProcessor.Pom;
import com.braintribe.model.platform.setup.api.IncrementRevisions;
import com.braintribe.utils.DOMTools;
import com.braintribe.utils.StringTools;

/**
 * Processes {@link IncrementRevisions} requests.
 *
 * @author michael.lafite
 */
public class IncrementRevisionsProcessor {

	/**
	 * The main method which processes the specified {@link IncrementRevisions} <code>request</code>.
	 */
	public static void process(IncrementRevisions request) {

		File artifactGroupFolder = new File(request.getGroupFolder());

		// re-use method from UpdateGroupVersionProcessor (should be moved to a neutral place)
		Pom parentPom = UpdateGroupVersionProcessor.validateGroupFolder(artifactGroupFolder);

		MajorMinorVersion currentVersion = new MajorMinorVersion(parentPom.major, parentPom.minor);

		for (File artifactFolder : artifactGroupFolder.listFiles()) {
			if (!artifactFolder.isDirectory() || artifactFolder.getName().startsWith(".")) {
				// ignore files and e.g. also '.git' folder
				continue;
			}

			File artifactPomFile = new File(artifactFolder, "pom.xml");
			if (!artifactPomFile.exists()) {
				println("Ignoring folder " + artifactFolder.getName() + ", since it's not a (valid) artifact folder (" + artifactPomFile.getPath()
						+ " doesn't exist).");
				continue;
			}

			String artifactId = artifactFolder.getName();
			if (!artifactId.matches(request.getIncludeRegex()) || artifactId.matches(request.getExcludeRegex())) {
				println("Ignoring artifact " + artifactId + ", since it is excluded by the specified regex filter.");
				continue;
			}

			println("Processing artifact " + artifactId + ".");

			try {
				// re-use method from UpdateGroupVersionProcessor (should be moved to a neutral place)
				Pom pom = UpdateGroupVersionProcessor.parsePom(artifactPomFile, currentVersion);

				String revisionString;
				if (pom.usesVersionProperties) {
					revisionString = DOMTools.getExistingElementByXPath(pom.element, "/project/properties/revision").getTextContent();
				} else {
					String version = DOMTools.getExistingElementByXPath(pom.element, "/project/version").getTextContent();
					// we could probably use some version expert from MC here
					revisionString = StringTools.getSubstringAfterLast(version, ".");
				}

				String optionalRcSuffix = "-rc";
				String optionalPcSuffix = "-pc";
				
				final String revisionStringWithoutSuffix;
				final String suffixOrEmptyString;
				
				if (revisionString.endsWith(optionalRcSuffix)) {
					revisionStringWithoutSuffix = StringTools.removeSuffix(revisionString, optionalRcSuffix);
					suffixOrEmptyString = optionalRcSuffix;
				} else if (revisionString.endsWith(optionalPcSuffix)) {
					revisionStringWithoutSuffix = StringTools.removeSuffix(revisionString, optionalPcSuffix);
					suffixOrEmptyString = optionalPcSuffix;
				} else {
					revisionStringWithoutSuffix = revisionString;
					suffixOrEmptyString = "";
				}
				
				int revision;
				try {
					revision = Integer.parseInt(revisionStringWithoutSuffix);
				} catch (NumberFormatException e) {
					throw new RuntimeException(
							"Couldn't parse revision from revision string '" + revisionString + "' in POM file " + artifactPomFile + "!");
				}

				int newRevision = revision + request.getDelta();
				String newRevisionString = newRevision + suffixOrEmptyString;

				if (pom.usesVersionProperties) {
					DOMTools.getExistingElementByXPath(pom.element, "/project/properties/revision").setTextContent(newRevisionString);
				} else {
					Element versionElement = DOMTools.getExistingElementByXPath(pom.element, "/project/version");
					// we could probably use some version expert from MC here
					String majorMinorVersion = StringTools.getSubstringBeforeLast(versionElement.getTextContent(), ".");
					versionElement.setTextContent(majorMinorVersion + "." + newRevisionString);
				}

				pom.writeBackToFile();

				println("Updated revision of POM " + pom.file.getPath() + " from '" + revisionString + "' to '" + newRevisionString + "'.");
			} catch (RuntimeException e) {
				throw new IllegalStateException("Error while updating " + artifactPomFile.getPath() + ": " + e.getMessage(), e);
			}
		}
	}
}
