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
package com.braintribe.devrock.dmb.plugin;

import java.util.Date;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.braintribe.devrock.api.logging.LoggingCommons;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.logging.Logger;

public class DebugModuleBuilderPlugin extends AbstractUIPlugin {

	private static Logger log = Logger.getLogger(DevrockPlugin.class);

	public static final String PLUGIN_ID = "com.braintribe.devrock.DebugModuleBuilderPlugin"; //$NON-NLS-1$
	public static final String PLUGIN_RESOURCE_PREFIX = "platform:/plugin/" + PLUGIN_ID;
	
	
	private static DebugModuleBuilderPlugin instance;
	public static DebugModuleBuilderPlugin instance() {
		return instance;
	}
	
	private UiSupport uiSupport = new UiSupport();
	public UiSupport uiSupport() {
		return uiSupport;
	}
	
	@Override
	public void start(BundleContext context) throws Exception {	
		super.start(context);
		instance = this;
		
		LoggingCommons.initializeWithFallback( PLUGIN_ID);
		long startTime = System.nanoTime();		
		log.info("DebugModuleBuilderPlugin: starting : " + new Date());

		// initialization tasks here 
		
		
		long endTime = System.nanoTime();
		String msg = "DebugModuleBuilderPlugin : started after " + (endTime - startTime) / 1E6 + " ms";

		log.info(msg);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		long startTime = System.nanoTime();		
		log.info("DebugModuleBuilderPlugin: stopping : " + new Date());
		
		// de-initialization tasks here 
		uiSupport.dispose();
		
		
		long endTime = System.nanoTime();
		String msg = "DebugModuleBuilderPlugin : stopped after " + (endTime - startTime) / 1E6 + " ms";
		log.info(msg);
		
	}

	/**
	 * @param status - the {@link IStatus} to log
	 */
	public void log( IStatus status) {
		getLog().log( status);
		
		// redirect to logs 
		switch (status.getSeverity()) {
			case 0 : // OK
				log.trace(status.getMessage());
			case 1 : // INFO
				log.info(status.getMessage());
				break;
			case 2 : // WARN
				log.warn(status.getMessage());
				break;
			case 4: // ERROR
				log.error(status.getMessage(), status.getException());
				break;
			case 8:	// CANCEL ?? 	
				log.warn(status.getMessage());
				break;
				
			
		}
	}
	
}
