package com.braintribe.build.cmd.assets.impl.tfruntime.model;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface EnvironmentVariable extends GenericEntity {
	static EntityType<EnvironmentVariable> T = EntityTypes.T(EnvironmentVariable.class);

	String getName();
	void setName(String name);

	String getValue();
	void setValue(String value);

}
