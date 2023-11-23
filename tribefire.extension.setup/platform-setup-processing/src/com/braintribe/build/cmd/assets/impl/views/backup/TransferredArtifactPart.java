package com.braintribe.build.cmd.assets.impl.views.backup;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface TransferredArtifactPart extends GenericEntity {

	static EntityType<TransferredArtifactPart> T = EntityTypes.T(TransferredArtifactPart.class);

	String getName();
	void setName(String name);
	
	List<String> getRepositories();
	void setRepositories(List<String> repositories);
}