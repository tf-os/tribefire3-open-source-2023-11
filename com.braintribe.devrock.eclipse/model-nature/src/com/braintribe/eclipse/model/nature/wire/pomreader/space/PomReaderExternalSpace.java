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
package com.braintribe.eclipse.model.nature.wire.pomreader.space;

import java.util.function.Function;

import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifacts.mc.wire.pomreader.contract.PomReaderContract;
import com.braintribe.build.artifacts.mc.wire.pomreader.external.contract.PomReaderExternalContract;
import com.braintribe.devrock.virtualenvironment.VirtualEnvironmentPlugin;
import com.braintribe.eclipse.model.nature.EclipseVirtualEnvironment;
import com.braintribe.eclipse.model.nature.WorkspaceAwareDependencyResolver;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.annotation.Bean;
import com.braintribe.wire.api.annotation.Beans;
import com.braintribe.wire.api.annotation.Import;

@Beans
public class PomReaderExternalSpace implements PomReaderExternalContract {

	@Import
	private PomReaderContract pomReader;
	
	@Override
	public Function<DependencyResolver, DependencyResolver> dependencyResolverEnricher() {
		return r -> workspaceAwareDependencyResolver();
	}
	
	@Bean
	private WorkspaceAwareDependencyResolver workspaceAwareDependencyResolver() {
		WorkspaceAwareDependencyResolver bean = new WorkspaceAwareDependencyResolver();
		
		bean.setPomReader(pomReader.pomReader());
		bean.setDelegate(pomReader.standardDependencyResolver());
		
		return bean;
	}
	
	@Bean
	private ArtifactPomReader leanPomReader() {
		ArtifactPomReader bean = new ArtifactPomReader();
		return bean;
	}


	@Override
	public VirtualEnvironment virtualEnvironment() {
		if (VirtualEnvironmentPlugin.getOverrideActivation()) {
			OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
			ove.setEnvironmentOverrides( VirtualEnvironmentPlugin.getEnvironmentOverrides());
			ove.setPropertyOverrides( VirtualEnvironmentPlugin.getPropertyOverrides());
			return ove;
		}				
		return EclipseVirtualEnvironment.INSTANCE;
	}

}
