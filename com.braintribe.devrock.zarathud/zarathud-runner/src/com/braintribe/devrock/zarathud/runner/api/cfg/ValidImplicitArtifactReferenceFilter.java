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
package com.braintribe.devrock.zarathud.runner.api.cfg;

import java.util.function.Predicate;

import com.braintribe.zarathud.model.data.Artifact;

public class ValidImplicitArtifactReferenceFilter implements Predicate<Artifact> {
	
	private static String [] validArtifacts = new String [] {
			"com.braintribe.gm:gm-core-api",
			"com.braintribe.commons:platform-api",
	}; 
	
	public boolean test( Artifact artifact) {
		String key = artifact.getGroupId() + ":" + artifact.getArtifactId();
		for (String valid : validArtifacts) {
			if (key.equalsIgnoreCase( valid))
				return true;
		}
		return false;
	}
}