package com.braintribe.build.cmd.assets.impl.check.process;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ArtifactCheckResult extends CheckResult {

	EntityType<ArtifactCheckResult> T = EntityTypes.T(ArtifactCheckResult.class);

	String getArtifactId();
	void setArtifactId(String artifactId);
}
