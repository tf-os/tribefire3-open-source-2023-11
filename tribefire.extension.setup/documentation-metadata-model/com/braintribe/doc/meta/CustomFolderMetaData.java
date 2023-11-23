package com.braintribe.doc.meta;

import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface CustomFolderMetaData extends GenericEntity {
	EntityType<CustomFolderMetaData> T = EntityTypes.T(CustomFolderMetaData.class);

	void setFiles(Map<String, FileDisplayInfo> files);
	Map<String, FileDisplayInfo> getFiles();
	
}
