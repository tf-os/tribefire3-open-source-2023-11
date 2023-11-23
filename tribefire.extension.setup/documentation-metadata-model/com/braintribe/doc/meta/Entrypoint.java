package com.braintribe.doc.meta;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Entrypoint extends GenericEntity{
	EntityType<Entrypoint> T = EntityTypes.T(Entrypoint.class);

	@Mandatory
	void setTitle(String title);
	String getTitle();
	
	@Mandatory
	void setTargetUrl(String assetSchemedUrl);
	String getTargetUrl();
	
	void setImageUrl(String assetSchemedUrl);
	String getImageUrl();
}
