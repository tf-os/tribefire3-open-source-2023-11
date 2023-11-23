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
package tribefire.cortex.asset.resolving.ng.wire.space;

import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.asset.resolving.ng.api.AssetDependencyResolver;
import tribefire.cortex.asset.resolving.ng.impl.PlatformAssetResolver;
import tribefire.cortex.asset.resolving.ng.wire.contract.AssetResolverContract;

@Managed
public class AssetResolverSpace implements AssetResolverContract {
	@Import
	private ArtifactDataResolverContract artifactDataResolver;

	@Import
	private TransitiveResolverContract transitiveResolver;
	
	@Override
	public AssetDependencyResolver assetDependencyResolver() {
		PlatformAssetResolver bean = new PlatformAssetResolver();
		bean.setArtifactPartResolver(artifactDataResolver.artifactResolver());
		bean.setTransitiveDependencyResolver(transitiveResolver.transitiveDependencyResolver());
		bean.setRepositoryReflection(artifactDataResolver.repositoryReflection());
		return bean;
	}

	@Override
	public TransitiveResolverContract transitiveResolverContract() {
		return transitiveResolver;
	}
}
