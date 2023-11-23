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
package com.braintribe.model.packaging;

import java.util.Date;
import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * a packaging reflects what is packed in a build process
 * it has a list of dependencies (in terms of the ArtifactModel: solutions and not dependencies)
 * and
 * it has a terminal artifact (the reason why the packaging exists at all)
 * 
 * @author Pit
 *
 */

@SelectiveInformation("${terminalArtifact.artifactId}")
public interface Packaging extends GenericEntity{

	EntityType<Packaging> T = EntityTypes.T(Packaging.class);

	public Date getTimestamp();
	public void setTimestamp( Date date);
	
	public String getRevision();
	public void setRevision(String revision);
	
	public Artifact getTerminalArtifact();
	public void setTerminalArtifact( Artifact artifact);
	
	public List<Dependency> getDependencies();
	public void setDependencies( List<Dependency> artifacts);
	
	public String getMD5();
	public void setMD5( String md5);
	
	/**
	 * The version of the release the terminal belongs to. Examples: <code>1.1, 1.1.6, 2.0</code><br>
	 * Note that this is just a version number (or name). It usually does not match the version of the
	 * {@link #getTerminalArtifact() terminal artifact}. Actually it has nothing to do with artifacts at all. The main
	 * purpose of the version is that it's used in tickets. For example, a customer may report a bug in tribefire
	 * Control Center and include version <code>1.1.5</code> in the ticket description. This is enough information for
	 * Braintribe QA and developers to retrieve all other required data (e.g. dependencies, artifact versions, source
	 * code revisions).
	 */
	public String getVersion();
	public void setVersion(String version);
	
}
