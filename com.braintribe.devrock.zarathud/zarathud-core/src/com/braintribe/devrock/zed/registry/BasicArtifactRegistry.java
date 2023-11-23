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
package com.braintribe.devrock.zed.registry;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.devrock.zed.api.context.CommonZedCoreContext;
import com.braintribe.devrock.zed.api.core.ArtifactRegistry;
import com.braintribe.devrock.zed.commons.Commons;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.zarathud.model.data.Artifact;

public class BasicArtifactRegistry implements ArtifactRegistry {
	
	private Artifact runtimeArtifact;
	private Artifact unknownArtifact;
	private Map<String, Artifact> namedUnknownArtifacts = new HashMap<>();
	

	@Override
	public Artifact artifact(CommonZedCoreContext context, String signature) {
		return acquireArtifact(context, signature);
	}


	@Override
	public Artifact runtimeArtifact(CommonZedCoreContext context) {	
		return acquireJavaArtifact(context);
	}


	@Override
	public Artifact unknownArtifact(CommonZedCoreContext context) {
		return acquireUnknownSourceArtifact(context);
	}

	

	@Override
	public Artifact unknownArtifact(CommonZedCoreContext context, String name) {	
		return acquireUnknownSourceArtifact(context, name);
	}


	private boolean isJavaType( String signature) {
		//TODO : find a better way to do that.. 
		if (
				signature.startsWith( "java.") ||
				signature.startsWith( "java/") ||
				signature.startsWith( "javax.") ||
				signature.startsWith( "javax/")) {
			return true;
		}
		return false;
	}

	
	private Artifact acquireArtifact(CommonZedCoreContext context, String signature) {
		if (isJavaType(signature)) {
			return acquireJavaArtifact(context);
		}
		else if (signature.equalsIgnoreCase("void")) {
			return acquireJavaArtifact( context);
		}
		return acquireUnknownSourceArtifact( context);
	}

	private Artifact acquireJavaArtifact(CommonZedCoreContext context) {
		if (runtimeArtifact == null) {
			runtimeArtifact = Commons.create( context, Artifact.T);
			runtimeArtifact.setArtifactId("rt");
		}
		return runtimeArtifact;
	}

	private Artifact acquireUnknownSourceArtifact(CommonZedCoreContext context) {
		if (unknownArtifact == null) {
			unknownArtifact = Commons.create( context, Artifact.T);
			unknownArtifact.setArtifactId("unknown");
			unknownArtifact.setIsIncomplete(true);
		}
		return unknownArtifact;	
	}
	
	private Artifact acquireUnknownSourceArtifact(CommonZedCoreContext context, String name) {
		Artifact namedUnknownArtifact = this.namedUnknownArtifacts.get(name);
		if (namedUnknownArtifact == null) {
			namedUnknownArtifact = Commons.create( context, Artifact.T);
			ArtifactIdentification artifactIdentification = ArtifactIdentification.parse(name);
			namedUnknownArtifact.setGroupId( artifactIdentification.getGroupId());
			namedUnknownArtifact.setArtifactId( artifactIdentification.getArtifactId());
			namedUnknownArtifact.setVersion( "0.0.0");
			namedUnknownArtifact.setIsIncomplete(true);
			this.namedUnknownArtifacts.put ( name, namedUnknownArtifact);
		}
		return namedUnknownArtifact;	
	}

	
}
