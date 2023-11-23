package com.braintribe.doc.meta;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface CustomAssetMetaData extends GenericEntity {
	EntityType<CustomAssetMetaData> T = EntityTypes.T(CustomAssetMetaData.class);

	void setHidden(boolean hidden);
	boolean getHidden();
	
	void setDisplayTitle(String displayTitle);
	String getDisplayTitle();
	
}
