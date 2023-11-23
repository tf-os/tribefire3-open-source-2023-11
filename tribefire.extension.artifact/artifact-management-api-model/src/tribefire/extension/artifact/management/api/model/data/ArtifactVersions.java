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
package tribefire.extension.artifact.management.api.model.data;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.version.Version;

import tribefire.extension.artifact.management.api.model.request.GetArtifactVersions;
import tribefire.extension.artifact.management.api.model.request.GetArtifactsVersions;

/**
 * return value of the {@link GetArtifactsVersions}/ {@link GetArtifactVersions} processor
 * @author pit
 *
 */
public interface ArtifactVersions extends GenericEntity {
	
	EntityType<ArtifactVersions> T = EntityTypes.T(ArtifactVersions.class);
	
	String groupId = "groupId";
	String artifactId = "artifactId";
	String versionsAsStrings = "versionsAsStrings";
	String versions = "versions";

	String getGroupId();
	void setGroupId(String groupId);
	
	String getArtifactId();
	void setArtifactId(String value);
	
	List<String> getVersionsAsStrings();
	void setVersionsAsStrings(List<String> versionsAsStrings);

	List<Version> getVersions();
	void setVersions(List<Version> value);


}
