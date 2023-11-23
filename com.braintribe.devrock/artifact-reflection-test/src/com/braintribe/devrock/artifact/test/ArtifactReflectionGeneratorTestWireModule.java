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
package com.braintribe.devrock.artifact.test;

import java.io.File;
import java.util.List;

import org.assertj.core.util.Lists;

import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.resolver.ArtifactDataResolverModule;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;

public enum ArtifactReflectionGeneratorTestWireModule implements WireTerminalModule<ArtifactDataResolverContract> {
	
	INSTANCE;
	
	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		RepositoryConfiguration repositoryConfiguration = RepositoryConfiguration.T.create();
		repositoryConfiguration.setLocalRepositoryPath(new File("test-output/local-repo").getAbsolutePath());
		
		contextBuilder.bindContract(RepositoryConfigurationContract.class, () -> Maybe.complete(repositoryConfiguration));
	}
	
	@Override
	public List<WireModule> dependencies() {
		return Lists.list(ArtifactDataResolverModule.INSTANCE);
	}

}