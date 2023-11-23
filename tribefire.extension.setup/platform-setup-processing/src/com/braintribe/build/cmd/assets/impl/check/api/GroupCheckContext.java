package com.braintribe.build.cmd.assets.impl.check.api;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Provides information to a about the artifact group on which a {@link GroupCheck} is executed.
 */
public interface GroupCheckContext {

	boolean getIsFixesEnabled();

	String getGroupFolder();

	String getGroupId();

	String getMajorMinorVersion();

	Map<String, String> getGroupVersionProperties();

	Artifact getParentArtifact();

	List<Artifact> getArtifacts();

	void addResultDetailedInfo(String detailedInfo);

	Map<String, String> getGroupParentProperties();
	
	File getGroupParentPomXml();

	File getGroupBuildXml();
}
