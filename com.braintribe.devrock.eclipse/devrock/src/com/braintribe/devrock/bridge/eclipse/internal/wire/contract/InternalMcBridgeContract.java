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
package com.braintribe.devrock.bridge.eclipse.internal.wire.contract;

import com.braintribe.devrock.bridge.eclipse.api.McBridge;
import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.devrock.mc.api.repository.configuration.ArtifactChangesSynchronization;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.api.resolver.ArtifactResolver;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactResolver;
import com.braintribe.devrock.mc.api.resolver.DeclaredArtifactCompiler;
import com.braintribe.devrock.mc.api.resolver.DependencyResolver;
import com.braintribe.wire.api.space.WireSpace;

/**
 * internal wiring for {@link McBridge}
 * 
 * @author pit / dirk
 *
 */
public interface InternalMcBridgeContract extends WireSpace {	
	
	/**
	 * @return - the {@link ClasspathDependencyResolver}
	 */
	ClasspathDependencyResolver classpathResolver();
	
	/**
	 * @return - the basic {@link DependencyResolver} (redirect aware)
	 */
	DependencyResolver dependencyResolver();
	
	/**
	 * @return - the {@link CompiledArtifactResolver}
	 */
	CompiledArtifactResolver compiledArtifactResolver();
	
	/**
	 * @return - the {@link DeclaredArtifactCompiler} 
	 */
	DeclaredArtifactCompiler declaredArtifactCompiler();
	
	/**
	 * @return - the {@link ArtifactResolver}
	 */
	ArtifactResolver artifactResolver();
	
	/**
	 * @return - {@link RepositoryReflection}
	 */
	RepositoryReflection repositoryReflection();
		

	/**
	 * @return - a {@link ArtifactChangesSynchronization}
	 */
	ArtifactChangesSynchronization changesSynchronization();
}
