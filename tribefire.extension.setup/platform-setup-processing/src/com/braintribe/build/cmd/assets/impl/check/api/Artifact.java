package com.braintribe.build.cmd.assets.impl.check.api;

import java.io.File;

public class Artifact {

	private String groupId;
	private String artifactId;
	private String version;
	private File pomXmlFile;
	private File buildXmlFile;
	private File projectXmlFile;

	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getArtifactId() {
		return artifactId;
	}
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public File getPomXmlFile() {
		return pomXmlFile;
	}
	public void setPomXmlFile(File pomXmlFile) {
		this.pomXmlFile = pomXmlFile;
	}
	public File getBuildXmlFile() {
		return buildXmlFile;
	}
	public void setBuildXmlFile(File buildXmlFile) {
		this.buildXmlFile = buildXmlFile;
	}
	public File getProjectXmlFile() {
		return projectXmlFile;
	}
	public void setProjectXmlFile(File projectXmlFile) {
		this.projectXmlFile = projectXmlFile;
	}
}
