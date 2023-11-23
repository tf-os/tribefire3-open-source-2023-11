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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.repository.probingstates;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.model.mc.reason.configuration.RepositoryUnauthenticated;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.repolet.launcher.Launcher;

/**
 * tests that unauthenticated repository : 401
 *  
 * @author pit
 *
 */
public class RepositoryUnauthenticatedTest extends AbstractRepositoryConfigurationProbingTest {
	
	@Override
	protected Launcher launcher() {
		Launcher launcher = Launcher.build()
				.repolet()
				.name("archive")
					.descriptiveContent()
						.descriptiveContent(archiveInput())
					.close()
				.close()
								
				.repolet()
				.name("failing-archive")
					.descriptiveContent()
						.descriptiveContent(archiveInput())
					.close()
					.overridingResponseCode(401)
				.close()

			.done();		
		return launcher;
	}

	@Test
	public void testUnauthorizedAccess() {
		try {
			RepositoryReflection repositoryReflection = getReflection();
			RepositoryConfiguration repositoryConfiguration = repositoryReflection.getRepositoryConfiguration();

			validate( repositoryConfiguration, "failing-archive", RepositoryUnauthenticated.T);
			
					
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("unexpectedly an exception is thrown");
		}
		
	}
	
	

}
