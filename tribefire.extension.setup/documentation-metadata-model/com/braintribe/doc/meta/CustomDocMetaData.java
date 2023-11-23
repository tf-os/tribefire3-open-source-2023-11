package com.braintribe.doc.meta;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface CustomDocMetaData extends GenericEntity {
	EntityType<CustomDocMetaData> T = EntityTypes.T(CustomDocMetaData.class);

	void setTitle(String title);
	String getTitle();
	
	void setEntrypoints(List<Entrypoint> entrypoints);
	List<Entrypoint> getEntrypoints();
}
