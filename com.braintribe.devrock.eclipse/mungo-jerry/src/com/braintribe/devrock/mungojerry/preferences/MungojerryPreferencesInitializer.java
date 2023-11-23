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
package com.braintribe.devrock.mungojerry.preferences;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.IPreferenceStore;

import com.braintribe.codec.CodecException;
import com.braintribe.devrock.mungojerry.plugin.Mungojerry;
import com.braintribe.logging.Logger;
import com.braintribe.model.malaclypse.cfg.preferences.gwt.GwtPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.mj.MungojerryPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.mv.MavenPreferences;

public class MungojerryPreferencesInitializer {
	private static Logger log = Logger.getLogger(MungojerryPreferencesInitializer.class);
	
	public static void initializeDefaultPreferences(IPreferenceStore store) {		
		MungojerryPreferences preferences = initializeMungojerryPreferences();
		MungojerryPreferencesCodec codec = new MungojerryPreferencesCodec(store);
		try {
			codec.decode(preferences);
		} catch (CodecException e) {
			String msg="cannot decode initial preferences";
			log.error( msg, e);
			Mungojerry.log( IStatus.ERROR, msg + " as " + e.getMessage());
		}
	}
	
	public static GwtPreferences initializeGwtPreferences() {
		GwtPreferences preferences = GwtPreferences.T.create();
		preferences.setAutoInjectLibrary( "com.google.gwt:gwt-user#2.8.0.jar");
		return preferences;
	}
	
	public static MavenPreferences initializeMavenPreferences() {
		return MavenPreferences.T.create();
	}
	
	
	public static MungojerryPreferences initializeMungojerryPreferences() {
		MungojerryPreferences preferences = MungojerryPreferences.T.create();
		GwtPreferences gwtPreferences = initializeGwtPreferences();
		preferences.setGwtPreferences(gwtPreferences);
		MavenPreferences mvPreferences = initializeMavenPreferences();
		preferences.setMavenPreferences(mvPreferences);		
		return preferences;
	}
}
