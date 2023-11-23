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
package tribefire.cortex.asset.resolving.test.wire.space;

import static com.braintribe.wire.api.scope.InstanceConfiguration.currentInstance;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.utils.FileTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

import tribefire.cortex.asset.resolving.ng.api.AssetDependencyResolver;
import tribefire.cortex.asset.resolving.ng.wire.contract.AssetResolverContract;
import tribefire.cortex.asset.resolving.test.wire.contract.AssetResolvingTestContract;

@Managed
public class AssetResolvingTestSpace implements AssetResolvingTestContract {
	@Import
	private RepoConfigSpace repoConfig;
	
	@Import
	private AssetResolverContract assetResolver;
	
	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		launcher();
	}
	
	@Override
	public RepositoryConfigurationContract repositoryConfigurationContract() {
		return repoConfig;
	}
	
	public File temporaryDataFolder() {
		File bean = new File("temp-data");
		
		if (bean.exists()) {
			try {
				FileTools.deleteDirectoryRecursively(bean);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}	
		}

		bean.mkdirs();
		
		return bean;
	}
	
	public File localRepositoryFolder() {
		return new File(temporaryDataFolder(), "local-repository");
	}
	
	public String repoUrl() {
		return launcher().getLaunchedRepolets().get("repo");
	}
	
	public AssetDependencyResolver assetResolver() {
		return assetResolver.assetDependencyResolver();
	}
	
	@Managed
	private Launcher launcher() {
		Launcher launcher = Launcher.build().repolet().name("repo").filesystem().filesystem(new File("res/repo")).close().close().done();
		
		currentInstance().onDestroy(launcher::shutdown);
		
		launcher.launch();
		
		return launcher;
	}
}
