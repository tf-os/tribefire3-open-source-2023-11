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
package com.braintribe.devrock.bridge.eclipse.internal.wire.space;

import com.braintribe.devrock.bridge.eclipse.internal.wire.contract.InternalMcBridgeContract;
import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.devrock.mc.api.repository.configuration.ArtifactChangesSynchronization;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.api.resolver.ArtifactResolver;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactResolver;
import com.braintribe.devrock.mc.api.resolver.DeclaredArtifactCompiler;
import com.braintribe.devrock.mc.api.resolver.DependencyResolver;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class InternalMcBridgeSpace implements InternalMcBridgeContract {
	
	@Import
	ClasspathResolverContract classpathResolver;
	
	@Override
	public ClasspathDependencyResolver classpathResolver() {
		return classpathResolver.classpathResolver();
	}

	@Override
	public DependencyResolver dependencyResolver() {
		return classpathResolver.transitiveResolverContract().dataResolverContract().dependencyResolver();
	}

	@Override
	public CompiledArtifactResolver compiledArtifactResolver() {
		return classpathResolver.transitiveResolverContract().dataResolverContract().redirectAwareCompiledArtifactResolver();
	}

	@Override
	public DeclaredArtifactCompiler declaredArtifactCompiler() {		
		return classpathResolver.transitiveResolverContract().dataResolverContract().declaredArtifactCompiler();
	}

	@Override
	public ArtifactResolver artifactResolver() {
		return classpathResolver.transitiveResolverContract().dataResolverContract().artifactResolver();
	}

	@Override
	public RepositoryReflection repositoryReflection() {	
		return classpathResolver.transitiveResolverContract().dataResolverContract().repositoryReflection();
	}
	
	@Override
	public ArtifactChangesSynchronization changesSynchronization() {
		return classpathResolver.transitiveResolverContract().dataResolverContract().changesSynchronization();
	}

}
