package com.braintribe.build.cmd.assets.impl.views.backup;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface TransferredArtifact extends GenericEntity {

	static EntityType<TransferredArtifact> T = EntityTypes.T(TransferredArtifact.class);

	String getName();
	void setName(String name);
	
	List<TransferredArtifactPart> getParts();
	void setParts(List<TransferredArtifactPart> parts);
}

