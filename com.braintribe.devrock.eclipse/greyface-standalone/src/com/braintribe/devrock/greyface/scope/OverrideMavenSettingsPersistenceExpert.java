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
package com.braintribe.devrock.greyface.scope;

import java.io.File;

import org.eclipse.core.runtime.IStatus;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.AbstractMavenSettingsPersistenceExpert;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpert;
import com.braintribe.build.artifact.virtualenvironment.VirtualPropertyResolver;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.greyface.GreyfacePlugin;
import com.braintribe.devrock.greyface.GreyfaceStatus;
import com.braintribe.model.maven.settings.Settings;

/**
 * an implementation of the {@link MavenSettingsPersistenceExpert} that allows for overrides via the preferences, and the {@link VirtualPropertyResolver}
 * @author pit
 *
 */
public class OverrideMavenSettingsPersistenceExpert extends AbstractMavenSettingsPersistenceExpert implements MavenSettingsPersistenceExpert, HasMavenTokens {

	private VirtualPropertyResolver propertyResolver;
	
	@Configurable  @Required
	public void setPropertyResolver(VirtualPropertyResolver propertyResolver) {
		this.propertyResolver = propertyResolver;
	}

	@Override
	public Settings loadSettings() throws RepresentationException {		
		String userOverride = LOCAL_SETTINGS;
		
		String resolvedUserOverride = propertyResolver.resolve(userOverride);
		File userFile = new File( resolvedUserOverride);
		Settings localSettings = null;
		if (userFile.exists()) { 
			localSettings = loadSettings( userFile);
		}
		String confOverride  = REMOTE_SETTINGS;		
		String resolvedConfOverride = propertyResolver.resolve(confOverride);
		File confFile = new File( resolvedConfOverride);
		Settings confSettings = null;
		if (confFile.exists()) { 
			confSettings = loadSettings( confFile);
		}
		if (localSettings == null && confSettings == null) {
			String msg = "no settings found for [" + resolvedUserOverride + "] & [" + resolvedConfOverride +"]";
			GreyfaceStatus status = new GreyfaceStatus( msg, IStatus.WARNING);
			GreyfacePlugin.getInstance().getLog().log(status);
			throw new RepresentationException( msg);
		}
		if (localSettings != null && confSettings != null){ 
			return mergeSettings(localSettings, confSettings);
		} else {
			if (localSettings != null) {
				return localSettings;
			} else { 
				return confSettings;
			}
		}		
	}

}
