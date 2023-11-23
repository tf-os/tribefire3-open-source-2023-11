// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.model;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface CreateModelManipulations extends GenericEntity {
EntityType<UpdateModelDeclaration> T = EntityTypes.T(UpdateModelDeclaration.class);
	
	@Mandatory
	String getClasspath();
	void setClasspath(String classpath);
	
	@Mandatory
	String getTargetFolder();
	void setTargetFolder(String targetFolder);
	
	@Mandatory
	String getPomFile();
	void setPomFile(String pomFile);
	
	@Mandatory
	String getModelName();
	void setModelName(String modelName);
}
