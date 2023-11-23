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
package com.braintribe.devrock.greyface.settings.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import com.braintribe.codec.CodecException;
import com.braintribe.devrock.greyface.settings.codecs.GreyfacePreferencesCodec;
import com.braintribe.logging.Logger;
import com.braintribe.model.malaclypse.cfg.preferences.gf.GreyFacePreferences;
import com.braintribe.model.malaclypse.cfg.repository.RemoteRepository;

public class GreyfacePreferenceInitializer {
	
	private static Logger log = Logger.getLogger(GreyfacePreferenceInitializer.class);


	public static void initializeDefaultPreferences( IPreferenceStore store) {
	 	
			
		GreyFacePreferences gfPreferences = GreyFacePreferences.T.create();
		gfPreferences.setTempDirectory("${java.io.tmpdir}");
		
		gfPreferences.setExcludeExisting( true);
		gfPreferences.setExcludeOptionals(true);
		gfPreferences.setExcludeTest(true);
		gfPreferences.setOverwrite(false);
		
		gfPreferences.setRepair(false);		
		gfPreferences.setPurgePoms(true);
		gfPreferences.setApplyCompileScope(false);
		
		gfPreferences.setAsyncScanMode(true);		
		gfPreferences.setEnforceLicenses(true);
		
		gfPreferences.setFakeUpload(false);
		gfPreferences.setFakeUploadTarget("${GF_TARGET}");
		gfPreferences.setSimulateErrors(false);
		
		gfPreferences.setValidatePoms( false);
		
		
		gfPreferences.setLastSelectedTargetRepo( "third-party");
					
		List<RemoteRepository> settings = new ArrayList<RemoteRepository>(3);
					
		RemoteRepository mavenCentral = RemoteRepository.T.create();
		mavenCentral.setName( "Maven central");
		mavenCentral.setUrl( "https://repo1.maven.org/maven2");
		settings.add( mavenCentral);
		
		RemoteRepository mvnRepository = RemoteRepository.T.create();
		mvnRepository.setName( "Mvn repository");
		mvnRepository.setUrl( "http://central.maven.org/maven2");
		settings.add( mvnRepository);
				
		RemoteRepository sonaTypeGoogle = RemoteRepository.T.create();
		sonaTypeGoogle.setName( "Sonatype google");
		sonaTypeGoogle.setUrl( "https://oss.sonatype.org/content/groups/google");
		settings.add( sonaTypeGoogle);
		
		RemoteRepository sonaTypeGoogleCode = RemoteRepository.T.create();
		sonaTypeGoogleCode.setName( "Sonatype google code");
		sonaTypeGoogleCode.setUrl( "https://oss.sonatype.org/content/groups/googlecode");
		settings.add(sonaTypeGoogleCode);
		
		RemoteRepository jbossRepository = RemoteRepository.T.create();
		jbossRepository.setName( "JBoss repository");
		jbossRepository.setUrl( "https://repository.jboss.org/nexus/content/repositories/releases");
		settings.add( jbossRepository);
		
		
		RemoteRepository jcenterRepository = RemoteRepository.T.create();
		jcenterRepository.setName( "JCenter repository");
		jcenterRepository.setUrl( "https://jcenter.bintray.com");
		settings.add( jcenterRepository);
		
			
		gfPreferences.setSourceRepositories(settings);
			
		try {
			GreyfacePreferencesCodec codec = new GreyfacePreferencesCodec( store);
			codec.decode(gfPreferences);
		} catch (CodecException e) {
			log.error("cannt prime preferences", e);
		}
			

		
	}

}
