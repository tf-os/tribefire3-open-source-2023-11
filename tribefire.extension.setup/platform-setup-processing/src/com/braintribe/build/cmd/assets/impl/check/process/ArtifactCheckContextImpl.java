// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.check.process;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.build.cmd.assets.impl.check.api.Artifact;
import com.braintribe.build.cmd.assets.impl.check.api.ArtifactCheckContext;
import com.braintribe.build.cmd.assets.impl.check.api.GroupCheckContext;

/**
 * @author peter.gazdik
 */
public class ArtifactCheckContextImpl extends GroupCheckContextImpl implements ArtifactCheckContext {

	public Artifact artifact;
	public File artifactPomXmlFile;
	public File artifactBuildXmlFile;
	public File artifactProjectXmlFile;

	// @formatter:off
	@Override public Artifact getArtifact() { return artifact; }
	@Override public File getArtifactPomXml() { return artifactPomXmlFile; }
	@Override public File getArtifactBuildXml() { return artifactBuildXmlFile; }
	@Override public File getArtifactProjectXml() { return artifactProjectXmlFile; }
	// @formatter:on

	@Override
	public <R extends CheckResult> R newCheckResult() {
		ArtifactCheckResult result = newCheckResult(ArtifactCheckResult.T.create());
		result.setArtifactId(artifact.getArtifactId());
		return (R) result;
	}

	public static List<ArtifactCheckContextImpl> create(GroupCheckContext checkContext) {
		List<String> errorMessages = newList();

		List<ArtifactCheckContextImpl> artifactCheckContexts = checkContext.getArtifacts().stream().map(artifact -> {
			ArtifactCheckContextImpl artifactCheckContext = new ArtifactCheckContextImpl();
			artifactCheckContext.groupId = checkContext.getGroupId();
			artifactCheckContext.groupFolder = checkContext.getGroupFolder();
			artifactCheckContext.isFixesEnabled = checkContext.getIsFixesEnabled();
			artifactCheckContext.majorMinorVersion = checkContext.getMajorMinorVersion();
			artifactCheckContext.groupVersionProperties = checkContext.getGroupVersionProperties();
			artifactCheckContext.parentArtifact = checkContext.getParentArtifact();
			artifactCheckContext.artifacts = checkContext.getArtifacts();
			artifactCheckContext.groupParentProperties = checkContext.getGroupParentProperties();
			artifactCheckContext.groupBuildXmlFile = checkContext.getGroupBuildXml();
			artifactCheckContext.groupParentPomXmlFile = checkContext.getGroupParentPomXml();

			artifactCheckContext.artifact = artifact;

			File artifactFolder = new File(checkContext.getGroupFolder(), artifact.getArtifactId());

			File artifactBuildFile = new File(artifactFolder, "build.xml");
			if (!artifactBuildFile.exists() || !artifactBuildFile.isFile()) {
				errorMessages.add("Could not find build.xml file for artifact '" + artifact.getArtifactId() + "'!");
			} else {
				artifactCheckContext.artifactBuildXmlFile = artifactBuildFile;
			}

			File artifactPomFile = new File(artifactFolder, "pom.xml");
			if (!artifactPomFile.exists() || !artifactPomFile.isFile()) {
				errorMessages.add("Could not find pom.xml file for artifact '" + artifact.getArtifactId() + "'!");
			} else {
				artifactCheckContext.artifactPomXmlFile = artifactPomFile;
			}

			// It is OK for some artifacts to miss the .project files. Therefore, we are not strict about their
			// existence. The individual checks become responsible about the .project file existence.
			artifactCheckContext.artifactProjectXmlFile = new File(artifactFolder, ".project");

			return artifactCheckContext;
		}).collect(Collectors.toList());

		if (!errorMessages.isEmpty()) {
			throw new IllegalStateException("Probably '" + checkContext.getGroupFolder() + "' is not a valid artifact group folder.\n"
					+ errorMessages.stream().map(it -> " - " + it).collect(Collectors.joining("\n")));
		}

		return artifactCheckContexts;
	}
}
