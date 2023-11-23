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
package com.braintribe.model.artifact.processing;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * a simple container for the three identification values of an artifact, i.e. group, artifact and version <br/>
 * Bear in mind that semantically, the version is rather a version range, so you may give a range like [1.0,1.1), 
 * and that a seemingly simple version 1.0 is turned into [1.0,1.1).
 *    
 * @author pit
 *
 */
@ToStringInformation("${groupId}:${artifactId}#${version}")
public interface ArtifactIdentification extends GenericEntity{
		
	final EntityType<ArtifactIdentification> T = EntityTypes.T(ArtifactIdentification.class);
	
	String groupId = "groupId";
	String artifactId = "artifactId";
	String version = "version";
	
	/**
	 * @param groupId - the group id of the artifact
	 */
	@Mandatory
	void setGroupId( String groupId);
	/**
	 * @return - the group id of the artifact
	 */
	String getGroupId();
	
	/**
	 * @param artifactId - the artifact id of the artifact
	 */
	@Mandatory
	void setArtifactId( String artifactId);
	/**
	 * @return - the artifact id of the artifact
	 */
	String getArtifactId();
	
	// actually, this is a VersionRange!
	/**
	 * @param version - the version EXPRESSION of the artifact (may be a range)
	 */
	void setVersion( String version);
	/**
	 * @return - the version EXPRESSION of the artifact (may be a range)
	 */
	String getVersion();
	
}
