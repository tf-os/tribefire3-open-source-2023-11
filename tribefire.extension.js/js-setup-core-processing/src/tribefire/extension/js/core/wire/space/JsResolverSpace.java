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
package tribefire.extension.js.core.wire.space;

import java.io.File;

import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.BuildDependencyResolutionContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.js.core.api.JsResolver;
import tribefire.extension.js.core.impl.BasicJsResolver;
import tribefire.extension.js.core.wire.contract.JsResolverConfigurationContract;
import tribefire.extension.js.core.wire.contract.JsResolverContract;

/**
 * the space for the {@link JsResolverContract}
 * @author pit
 *
 */
@Managed
public class JsResolverSpace implements JsResolverContract {
	@Import
	BuildDependencyResolutionContract buildResolution;
	
	@Import 
	JsResolverConfigurationContract jsResolverConfiguration;
	
	@Override
	@Managed
	public BasicJsResolver jsResolver() {	
		BasicJsResolver bean = new BasicJsResolver();
		bean.setBuildDependencyResolver( buildResolution.buildDependencyResolver());
		bean.setPomReader( buildResolution.pomReader());
		bean.setLocalRepositoryPath( localRepository());
		bean.setPreferMinOverPretty( jsResolverConfiguration.preferMinOverPretty());
		bean.setUseSymbolicLink(jsResolverConfiguration.useSymbolicLink());
		bean.setEnricher( buildResolution.solutionEnricher());
		return bean;
	}


	@Override
	public File localRepository() {
		return buildResolution.localRepository();		
	}
}
