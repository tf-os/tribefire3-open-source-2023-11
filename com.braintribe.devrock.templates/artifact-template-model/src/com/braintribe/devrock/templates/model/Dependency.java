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
package com.braintribe.devrock.templates.model;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.PositionalArguments;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@PositionalArguments({"groupId", "artifactId", "version"})
public interface Dependency extends GenericEntity {

	EntityType<Dependency> T = EntityTypes.T(Dependency.class);
	
	@Description("The group id of the dependency.")
	@Mandatory
	@Alias("gid")
	String getGroupId();
	void setGroupId(String groupId);

	@Description("The artifact id of the dependency.")
	@Mandatory
	@Alias("aid")
	String getArtifactId();
	void setArtifactId(String artifactId);
	
	@Description("The version of the dependency.")
	@Alias("v")
	String getVersion();
	void setVersion(String version);
	
	@Description("The scope of the dependency.")
	@Alias("s")
	String getScope();
	void setScope(String scope);
	
	@Description("The classifier of the dependency.")
	@Alias("c")
	String getClassifier();
	void setClassifier(String classifier);
	
	@Description("The type of the dependency.")
	@Alias("t")
	String getType();
	void setType(String type);
	
	@Description("The tags of the dependency, e.g. asset, ...")
	List<String> getTags();
	void setTags(List<String> tags);
	
	@Description("The excluded transitive dependencies of the dependency.")
	List<Dependency> getExclusions();
	void setExclusions(List<Dependency> exclusions);
	
}
