// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.check.process;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.braintribe.build.cmd.assets.impl.check.api.Artifact;
import com.braintribe.build.cmd.assets.impl.check.api.GroupCheckContext;
import com.braintribe.build.cmd.assets.impl.check.group.GroupCheckDomHelpers;
import com.braintribe.common.Constants;
import com.braintribe.utils.DOMTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.lcd.StringTools;

/**
 * @author peter.gazdik
 */
public class GroupCheckContextImpl implements GroupCheckContext {

	public boolean isFixesEnabled;
	public String groupFolder;
	public String groupId;
	public String majorMinorVersion;
	public Map<String, String> groupVersionProperties;
	public Artifact parentArtifact;
	public List<Artifact> artifacts;
	public Map<String, String> groupParentProperties;
	public File groupParentPomXmlFile;
	public File groupBuildXmlFile;

	public CheckResult currentResult;

	// @formatter:off
	@Override public boolean getIsFixesEnabled() { return isFixesEnabled; }
	@Override public String getGroupFolder() { return groupFolder; }
	@Override public String getGroupId() { return groupId; }
	@Override public String getMajorMinorVersion() { return majorMinorVersion; }
	@Override public Map<String, String> getGroupVersionProperties() { return groupVersionProperties; }
	@Override public Artifact getParentArtifact() { return parentArtifact; }
	@Override public List<Artifact> getArtifacts() { return artifacts; }
	@Override public Map<String, String> getGroupParentProperties() { return groupParentProperties; }
	@Override public File getGroupParentPomXml() { return groupParentPomXmlFile; }
	@Override public File getGroupBuildXml() { return groupBuildXmlFile; }
	// @formatter:on

	public <R extends CheckResult> R newCheckResult() {
		return (R) newCheckResult(GroupCheckResult.T.create());
	}

	protected <R extends CheckResult> R newCheckResult(R result) {
		currentResult = result;
		result.setGroup(groupId);
		return result;
	}

	@Override
	public void addResultDetailedInfo(String detailedInfo) {
		if (currentResult.getDetailedInfo() != null) {
			currentResult.setDetailedInfo(currentResult.getDetailedInfo() + "\n" + detailedInfo);
		} else {
			currentResult.setDetailedInfo(detailedInfo);
		}
	}

	static GroupCheckContextImpl create(String groupFolder, boolean fixesEnabled) {
		GroupCheckContextImpl groupCheckContext = new GroupCheckContextImpl();

		List<String> errorMessages = newList();

		File parentArtifactDir = new File(groupFolder, "parent");
		if (!parentArtifactDir.exists() || !parentArtifactDir.isDirectory()) {
			errorMessages.add("Could not find parent directory (i.e. parent artifact)!");
		}

		File buildFile = new File(groupFolder, "build.xml");
		if (!buildFile.exists() || !buildFile.isFile()) {
			errorMessages.add("Could not find build.xml file!");
		} else {
			groupCheckContext.groupBuildXmlFile = buildFile;
		}

		failOnError(errorMessages, "Please make sure that '" + parentArtifactDir + "' points to a valid artifact group folder.");

		// if files exists, read it and ignore all listed artifacts.
		// expected format is [group-id]:[artifact-id], e.g. org.example:example-artifact (one line per artifact)
		File dontBuildFile = new File(groupFolder, ".dontbuild");
		Set<String> notBuildArtifacts = new HashSet<>();
		if (dontBuildFile.exists()) {
			notBuildArtifacts.addAll(FileTools.readLines(dontBuildFile, Constants.ENCODING_UTF8));
		}
				
		File parentArtifactPomFile = new File(parentArtifactDir, "pom.xml");
		final String absolutePath = parentArtifactPomFile.getAbsolutePath();
		if (!parentArtifactPomFile.exists() || !parentArtifactPomFile.isFile()) {
			errorMessages.add("Could not find parent pom xml file (i.e. parent artifact)!");
		} else {
			groupCheckContext.groupParentPomXmlFile = parentArtifactPomFile;
		}

		failOnError(errorMessages, "Please make sure that '" + parentArtifactDir + "' has a valid parent.");

		groupCheckContext.isFixesEnabled = fixesEnabled;
		groupCheckContext.groupFolder = groupFolder;

		Document parentPomDocument = DOMTools.parse(FileTools.readStringFromFile(parentArtifactPomFile));

		groupCheckContext.groupId = extractedElementValueOrSetError(parentPomDocument, "/project/groupId", errorMessages, absolutePath);

		Version parentVersion = parseVersionInfo(parentPomDocument, absolutePath, errorMessages);
		groupCheckContext.majorMinorVersion = parentVersion.major + "." + parentVersion.minor;
		
		Node propertiesElements = parentPomDocument.getElementsByTagName("properties").item(0);
		groupCheckContext.groupParentProperties = newMap();
		groupCheckContext.groupVersionProperties = newMap();
		NodeList childPropertiesElements = propertiesElements.getChildNodes();
		for (int j = 0; j < childPropertiesElements.getLength(); j++) {
			final Node propertyElement = childPropertiesElements.item(j);
			String propertyName = propertyElement.getNodeName();
			groupCheckContext.groupParentProperties.put(propertyName, propertyElement.getTextContent());
			if (propertyName.startsWith("V.")) {
				groupCheckContext.groupVersionProperties.put(propertyName, propertyElement.getTextContent());
			}
		}

		// find the sub folders that contain a build.xml (basically finding the artifacts)
		List<File> artifactDirs = Arrays.asList(new File(groupFolder).listFiles((file, name) -> {
			final File listedFile = new File(file, name);
			if (listedFile.isDirectory()) {
				File buildFileInSubDir = new File(listedFile, "build.xml");
				return buildFileInSubDir.exists() && buildFileInSubDir.isFile()
						&& !notBuildArtifacts.contains(groupCheckContext.groupId + ":" + listedFile.getName());
			}
			return false;
		}));
		artifactDirs.sort((File f1, File f2) -> f1.getName().compareTo(f2.getName()));
		
		groupCheckContext.artifacts = newList();

		for (File artifactDir : artifactDirs) {
			Artifact artifact = new Artifact();

			File pomXmlFile = new File(artifactDir, "pom.xml");
			if (pomXmlFile.exists()) {
				artifact.setPomXmlFile(pomXmlFile);
			}

			File buildXmlFile = new File(artifactDir, "build.xml");
			if (buildXmlFile.exists()) {
				artifact.setBuildXmlFile(buildXmlFile);
			}

			File projectXmlFile = new File(artifactDir, ".project");
			if (projectXmlFile.exists()) {
				artifact.setProjectXmlFile(projectXmlFile);
			}

			String pomXmlAbsolutePath = pomXmlFile.getAbsolutePath();

			Document pomXmlDocument = DOMTools.parse(FileTools.readStringFromFile(pomXmlFile));
			artifact.setArtifactId(extractedElementValueOrSetError(pomXmlDocument, "/project/artifactId", errorMessages, pomXmlAbsolutePath));

			Version version = parseVersionInfo(pomXmlDocument, pomXmlAbsolutePath, errorMessages);
			artifact.setVersion(version.major + "." + version.minor + "." + version.revision);

			groupCheckContext.artifacts.add(artifact);
			if (artifact.getArtifactId() != null && artifact.getArtifactId().equals("parent")) {
				groupCheckContext.parentArtifact = artifact;
				artifact.setGroupId(groupCheckContext.groupId);
			} else {
				boolean hasParent = GroupCheckDomHelpers.getElementByXPath(pomXmlDocument, "/project/parent") != null;
				// TODO: improve how we check for type view and move decision whether parent is required or not to a central place 
				if (hasParent || !artifact.getArtifactId().endsWith("-view")) {
					// almost all (non-parent) artifacts have a parent, so that's the normal case
					artifact.setGroupId(extractedElementValueOrSetError(pomXmlDocument, "/project/parent/groupId", errorMessages, pomXmlAbsolutePath));
				} else {
					// artifact is a view without a parent. this is allowed.
					artifact.setGroupId(extractedElementValueOrSetError(pomXmlDocument, "/project/groupId", errorMessages, pomXmlAbsolutePath));
				}
			}
		}

		failOnError(errorMessages, "See error message(s) below and manually apply respective fixes first.");
		return groupCheckContext;
	}

	private static Version parseVersionInfo(Document pom, String absolutePath, List<String> errorMessages) {
		String major;
		String minor;
		String revision;
		boolean hasVersionProperties = GroupCheckDomHelpers.getElementByXPath(pom, "/project/properties/major") != null;
		if (hasVersionProperties) {
			// old style
			major = extractedElementValueOrSetError(pom, "/project/properties/major", errorMessages,
					absolutePath);
			minor = extractedElementValueOrSetError(pom, "/project/properties/minor", errorMessages,
					absolutePath);
			revision = extractedElementValueOrSetError(pom, "/project/properties/revision", errorMessages,
					absolutePath);
		} else {
			// new style
			String version = extractedElementValueOrSetError(pom, "/project/version", errorMessages,
					absolutePath);
			major = StringTools.getSubstringBefore(version, ".");
			minor = StringTools.getSubstringBetween(version, ".", ".");
			revision = StringTools.getSubstringAfterLast(version, ".");
		}
		
		Version version = new Version();
		version.major = major;
		version.minor = minor;
		version.revision = revision;
		return version;
	}
	
	private static class Version {
		String major;
		String minor;
		String revision;
	}
	
	private static void failOnError(List<String> errorMessages, String instructionMessage) {
		if (!errorMessages.isEmpty()) {
			throw new IllegalStateException("Error before checking group! " + instructionMessage + "\n"
					+ errorMessages.stream().map(it -> " - " + it).collect(Collectors.joining("\n")));
		}
	}

	private static String extractedElementValueOrSetError(Document document, String xpath, List<String> errorMessages, String pomXmlAbsolutePath) {
		Element foundElement = DOMTools.getElementByXPath(document, xpath);
		if (foundElement == null || StringTools.isEmpty(foundElement.getTextContent())) {
			errorMessages.add("Could not parse " + xpath + " element in pom.xml " + pomXmlAbsolutePath);
			return null;
		}
		return foundElement.getTextContent();
	}
}
