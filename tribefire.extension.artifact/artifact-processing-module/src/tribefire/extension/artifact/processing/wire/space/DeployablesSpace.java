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
package tribefire.extension.artifact.processing.wire.space;

import com.braintribe.artifact.processing.ArtifactProcessingExpert;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.wire.contract.PlatformResourcesContract;
import tribefire.module.wire.contract.SystemUserRelatedContract;


@Managed
public class DeployablesSpace implements WireSpace {

	@Import
	private SystemUserRelatedContract systemUser;
	
	@Import
	private PlatformResourcesContract resources;
	
	@Managed
	public ArtifactProcessingExpert artifactProcessingExpert(ExpertContext<com.braintribe.model.artifact.processing.deployment.ArtifactProcessingExpert> context) {
		ArtifactProcessingExpert bean = new ArtifactProcessingExpert();
		String externalId = context.getDeployable().getConfigurationAccess().getExternalId();
		bean.setSessionSupplier(()-> systemUser.sessionFactory().newSession( externalId));
		bean.setLocalRepositoryLocation( resources.publicResources("local-maven-repository").asFile());
		return bean;
	}

}
