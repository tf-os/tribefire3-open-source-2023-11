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
package com.braintribe.common.artifact;

import java.util.Set;

/**
 * One artifact is fully described by specifying its <b>groupId</b>, <b>artifactId</b>, <b>version</b> and in addition its <b>archetype(s)</b>. 
 * From those informations the name is determined to be "groupId:artifactId" and the versioned-name to be "groupId:artifactId#versioned".   
 * 
 * @author Dirk Scheffler
 *
 */
public interface ArtifactReflection {

	String groupId();

	String artifactId();

	String version();

	Set<String> archetypes();

	/**
	 * @return "groupId:artifactId"
	 */
	String name();

	/**
	 * @return "groupId:artifactId#version"
	 */
	String versionedName();
}