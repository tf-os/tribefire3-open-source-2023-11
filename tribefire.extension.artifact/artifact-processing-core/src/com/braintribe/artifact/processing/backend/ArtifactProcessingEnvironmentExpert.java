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
package com.braintribe.artifact.processing.backend;

import java.util.List;

import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.processing.cfg.env.Override;
import com.braintribe.model.artifact.processing.cfg.env.OverridingEnvironmentVariable;
import com.braintribe.model.artifact.processing.cfg.env.OverridingSystemProperty;
import com.braintribe.model.artifact.processing.cfg.repository.RepositoryConfiguration;
import com.braintribe.ve.api.VirtualEnvironment;

public class ArtifactProcessingEnvironmentExpert {
	private static Logger log = Logger.getLogger(ArtifactProcessingEnvironmentExpert.class);
	
	public VirtualEnvironment acquireVirtualEnvironment( RepositoryConfiguration scopeConfiguration) {
		OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();

		if (scopeConfiguration == null) {
			return ove;
		}
		
		List<Override> overrides = scopeConfiguration.getEnvironmentOverrides();		
		//  
		if (overrides != null && overrides.size() > 0) {
			// add the overrides 
			overrides.stream().forEach( o -> {
				if ( o instanceof OverridingEnvironmentVariable) {					
					ove.addEnvironmentOverride( o.getName(), o.getValue());
				}
				else if (o instanceof OverridingSystemProperty) { 				
					ove.addPropertyOverride( o.getName(), o.getValue());
				}
				else {
					log.warn( "unknow override found [" + o.getClass().getName() + "], ignoring");
				}
			});
		}
		return ove;		
	}

}
