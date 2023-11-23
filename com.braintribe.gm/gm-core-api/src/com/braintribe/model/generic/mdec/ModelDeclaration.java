// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.model.generic.mdec;

import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.ForwardDeclaration;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@ForwardDeclaration("com.braintribe.gm:model-declaration-model")
public interface ModelDeclaration extends GenericEntity {

	final EntityType<ModelDeclaration> T = EntityTypes.T(ModelDeclaration.class);

	void setArtifactId(String artifactId);
	String getArtifactId();

	void setGroupId(String groupId);
	String getGroupId();

	void setVersion(String version);
	String getVersion();

	void setName(String name);
	String getName();

	void setModelGlobalId(String modelGlobalId);
	String getModelGlobalId();

	void setHash(String hash);
	String getHash();

	void setTypes(Set<String> types);
	Set<String> getTypes();

	void setDependencies(List<ModelDeclaration> dependencies);
	List<ModelDeclaration> getDependencies();

}
