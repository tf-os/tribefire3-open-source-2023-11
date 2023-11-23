package com.braintribe.build.cmd.assets.impl.tfruntime.model;

import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Metadata extends GenericEntity {
	static EntityType<Metadata> T = EntityTypes.T(Metadata.class);

	String getName();
	void setName(String name);

	String getNamespace();
	void setNamespace(String tribefire);

	Map<String, String> getLabels();
	void setLabels(Map<String, String> labels);
}
