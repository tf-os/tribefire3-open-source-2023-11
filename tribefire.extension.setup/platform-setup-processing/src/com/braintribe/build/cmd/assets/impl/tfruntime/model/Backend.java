package com.braintribe.build.cmd.assets.impl.tfruntime.model;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Backend extends GenericEntity {
	static EntityType<Backend> T = EntityTypes.T(Backend.class);

	String getType();
	void setType(String type);

	// Map<String, String> getParams();
	// void setParams(Map<String, String> params);

}
