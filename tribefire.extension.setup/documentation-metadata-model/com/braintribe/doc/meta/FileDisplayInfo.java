package com.braintribe.doc.meta;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface FileDisplayInfo extends GenericEntity{
	EntityType<FileDisplayInfo> T = EntityTypes.T(FileDisplayInfo.class);

	void setDisplayTitle(String displayTitle);
	String getDisplayTitle();
	
	void setPriority(int priority);
	int getPriority();
	
	void setHidden(boolean hidden);
	boolean getHidden();
}
