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
package com.braintribe.devrock.commands;

import java.util.Date;

import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.gm.model.reason.Maybe;


/**
 * retrieves the current repository configuration and displays it 
 * 
 * @author pit
 *
 */
public class RepositoryConfigurationInfoCommand extends AbstractRepositoryConfigurationViewCommand {

	@Override
	protected Maybe<Container> retrieveRepositoryMaybe() {
				
		long before = System.nanoTime();
		Maybe<RepositoryReflection> repositoryReflectionMaybe = DevrockPlugin.mcBridge().reflectRepositoryConfiguration();
		long after = System.nanoTime();
		double lastProcessingTime = (after - before) / 1E6;	
		if (repositoryReflectionMaybe.isSatisfied()) {
			RepositoryReflection reflection = repositoryReflectionMaybe.get();
			RepositoryConfiguration repositoryConfiguration = reflection.getRepositoryConfiguration();
			
			Container container = new Container();
			container.rfcg = repositoryConfiguration;
			container.processingTime = lastProcessingTime;
			container.timestamp = new Date();
			container.file = null; // no file 
			
			return Maybe.complete( container);
		}
		else {
			return repositoryReflectionMaybe.cast();
		}		
	}
}
