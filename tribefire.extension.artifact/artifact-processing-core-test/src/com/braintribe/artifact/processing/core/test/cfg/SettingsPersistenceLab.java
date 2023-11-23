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
package com.braintribe.artifact.processing.core.test.cfg;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.artifact.processing.cfg.repository.RepositoryConfiguration;
import com.braintribe.model.artifact.processing.cfg.repository.SimplifiedRepositoryConfiguration;
import com.braintribe.model.artifact.processing.cfg.repository.details.Repository;
import com.braintribe.model.maven.settings.Settings;

/**
 * @author pit
 *
 */
public class SettingsPersistenceLab extends AbstractPersistenceLab {
	

	@Test
	public void testSingleModelled() {
		
		SimplifiedRepositoryConfiguration msCfg = SimplifiedRepositoryConfiguration.T.create();
		
		Repository r = RepoHelper.createRepoOne();
				
		msCfg.setRepositories( Collections.singletonList(r));
		
		//
		Validator validator = new Validator() {			
			@Override
			public void validate(RepositoryConfiguration scopeConfiguration, Settings settings) {
				//											
				RepoHelper.validateRepo(r, settings);							
			}
		};
		
		test( msCfg, validator);
	}
	
	
	@Test
	public void testDoubleModelled() {
		
		SimplifiedRepositoryConfiguration msCfg = SimplifiedRepositoryConfiguration.T.create();
		
		Repository rOne = RepoHelper.createRepoOne();
		
		Repository rTwo = RepoHelper.createRepoTwo();
						
		msCfg.setRepositories( Arrays.asList( rOne, rTwo));
		
		Validator validator = new Validator() {			
			@Override
			public void validate(RepositoryConfiguration scopeConfiguration, Settings settings) {
				//				
				for (Repository repo : ((SimplifiedRepositoryConfiguration) scopeConfiguration).getRepositories()) {					
					RepoHelper.validateRepo(repo, settings);
				}				
			}
		};
		
		test( msCfg, validator);
	}


	
	@Test
	public void testStandardResource() {
		Validator validator = new Validator() {			
			@Override
			public void validate(RepositoryConfiguration scopeConfiguration, Settings settings) {
				Assert.assertTrue( "no settings transferred", settings != null);				
			}
		};
		testResource( "settings.xml", validator);
	}
	

}
