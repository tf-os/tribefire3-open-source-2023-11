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

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * a representation of a part (aka file) of an {@link ResolvedArtifact}
 * @author pit
 *
 */
public interface ResolvedArtifactPart extends PartIdentification {

	final EntityType<ResolvedArtifactPart> T = EntityTypes.T(ResolvedArtifactPart.class);

	/**
	 * @return - the resolved URL, so either a file or http/https protocol URL depending on the actual location of the part.
	 */
	String getUrl();
	/**
	 * @param url  - the resolved URL, so either a file or http/https protocol URL depending on the actual location of the part.
	 */
	void setUrl( String url);
}
