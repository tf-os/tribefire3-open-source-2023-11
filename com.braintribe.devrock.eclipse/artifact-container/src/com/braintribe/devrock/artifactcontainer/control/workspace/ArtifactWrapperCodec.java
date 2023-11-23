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
package com.braintribe.devrock.artifactcontainer.control.workspace;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.cc.lcd.HashSupportWrapperCodec;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

public class ArtifactWrapperCodec extends HashSupportWrapperCodec<Artifact> {
	public ArtifactWrapperCodec() {
		super( true);
	}

	@Override
	protected int entityHashCode(Artifact e) {
		return NameParser.buildName(e).hashCode();
	}

	@Override
	protected boolean entityEquals(Artifact e1, Artifact e2) {
		
		if (!e1.getGroupId().equalsIgnoreCase(e2.getGroupId()))
			return false;
		if (!e1.getArtifactId().equalsIgnoreCase(e2.getArtifactId()))
			return false;
		
		if (!VersionProcessor.matches(e1.getVersion(), e2.getVersion()))
			return false;
		
		return true;
	}
}
