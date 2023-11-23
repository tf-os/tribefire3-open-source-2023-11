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
package com.braintribe.gwt.customization.client;

import java.util.List;

import com.braintribe.gwt.async.client.RuntimeConfiguration;
import com.braintribe.gwt.customization.client.ioc.IocExtensions;
import com.braintribe.gwt.customizationui.client.startup.CustomizationStartup;
import com.braintribe.gwt.customizationui.client.startup.GmeEntryPoint;
import com.braintribe.gwt.customizationui.client.startup.TribefireRuntime;
import com.braintribe.gwt.gxt.gxtresources.whitemask.client.MaskController;
import com.braintribe.gwt.ioc.gme.client.IocExtensionPoints;
import com.braintribe.gwt.ioc.gme.client.Providers;
import com.braintribe.gwt.ioc.gme.client.Runtime;
import com.braintribe.gwt.ioc.gme.client.Startup;
import com.braintribe.gwt.tribefirejs.client.tools.StaticTools;
import com.braintribe.gwt.utils.client.RootKeyNavExpert;
import com.braintribe.gwt.utils.client.RootKeyNavExpert.RootKeyNavListener;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.Event;
import com.sencha.gxt.widget.core.client.container.Container;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class StartupEntryPoint extends GmeEntryPoint {
	
	static {
		// force usage of the class (Gxt problem on Dev. Mode - Mac)
		Container.class.getName();
		StaticTools.class.getName();
	}
	
	/**
	 * This is the entry point method.
	 */
	@Override
	public void onModuleLoad() { //The order where the stuff is called bellow is somehow VERY important
		IocExtensionPoints.INSTANCE = new IocExtensions();
		CustomizationStartup.start(Providers.packagingProvider); //Needed to start static configurations within the CustomizationStartup.
		//Panels.setUseSettingsMenu(false);
		com.braintribe.gwt.ioc.gme.client.Runtime.setAppendAccessToTitle(true);
		//com.braintribe.gwt.ioc.gme.client.Runtime.setUseHardwiredAccesses(false);
		//com.braintribe.gwt.ioc.gme.client.Runtime.setUseAccessChoice(true);
		String applicationId = RuntimeConfiguration.getInstance().getProperty("tribefireExplorerName", Runtime.explorerName);
		Runtime.setApplicationId(applicationId);
		
		String logoUrl = TribefireRuntime.getProperty("TRIBEFIRE_LOGO_URL", null, true);
		String servicesUrl = Runtime.tribefireServicesAbsoluteUrl.get();
		String accessId = Runtime.accessId.get();
		
		MaskController.setProgressBarImage(logoUrl != null ? logoUrl : getDefaultLogoUrl(servicesUrl, accessId, applicationId));
		String title = TribefireRuntime.getProperty("TRIBEFIRE_LOADING_TITLE", null, true);
		if (title != null) {
			MaskController.setProgressBarTitle(title);
			titleSet = true;
		}
		logoSet = logoUrl != null;
		
		new CustomizationStartup().startCustomization(Startup.customization, false);
		
		if (!titleSet || !logoSet)
			waitForLogoAndText(servicesUrl, accessId, applicationId);
		
		Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
			@Override
			public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
				NativeEvent nativeEvent = event.getNativeEvent();
				if (!nativeEvent.getType().equals(KeyDownEvent.getType().getName()))
					return;
				
				List<RootKeyNavListener> listeners = RootKeyNavExpert.getListeners();
				if (listeners == null)
					return;
				
				listeners.forEach(l -> l.onRootKeyPress(nativeEvent));
			}
		});
	}
	
}
