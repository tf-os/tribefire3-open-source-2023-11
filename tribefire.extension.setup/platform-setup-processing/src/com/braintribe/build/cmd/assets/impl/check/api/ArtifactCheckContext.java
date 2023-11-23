package com.braintribe.build.cmd.assets.impl.check.api;

import java.io.File;

/**
 * Provides information to a about the artifact on which an {@link ArtifactCheck} is executed.
 */
public interface ArtifactCheckContext extends GroupCheckContext {

	Artifact getArtifact();

	File getArtifactPomXml();

	File getArtifactBuildXml();

	File getArtifactProjectXml();
}
