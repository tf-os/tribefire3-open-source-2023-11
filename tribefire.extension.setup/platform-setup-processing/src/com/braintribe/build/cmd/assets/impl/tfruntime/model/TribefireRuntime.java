package com.braintribe.build.cmd.assets.impl.tfruntime.model;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface TribefireRuntime extends GenericEntity {
	static EntityType<TribefireRuntime> T = EntityTypes.T(TribefireRuntime.class);

	String getApiVersion();
	void setApiVersion(String apiVersion);

	String getKind();
	void setKind(String kind);

	Metadata getMetadata();
	void setMetadata(Metadata metadata);

	Spec getSpec();
	void setSpec(Spec spec);

}
