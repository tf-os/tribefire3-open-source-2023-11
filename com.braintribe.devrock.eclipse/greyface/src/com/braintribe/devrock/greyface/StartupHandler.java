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
package com.braintribe.devrock.greyface;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IStartup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import com.braintribe.devrock.virtualenvironment.VirtualEnvironmentPlugin;

/**
 * used to register early contribution to VE.. 
 * @author Pit
 *
 */
public class StartupHandler implements IStartup {
	private static final String PLUGIN_ID = "com.braintribe.devrock.VirtualEnvironment";
	
	@Override
	public void earlyStartup() {
		final Bundle bundle = Platform.getBundle(PLUGIN_ID);
		if (bundle != null && bundle.getState() == Bundle.ACTIVE) {
			// register resolvers
			registerContribution();
		} else {
			// register listener to get informed, when plug-in becomes active
			final BundleContext bundleContext = GreyfacePlugin.getInstance().getBundle().getBundleContext();
			bundleContext.addBundleListener(new BundleListener() {
				@Override
				public void bundleChanged(final BundleEvent pEvent) {
					final Bundle notifyingBundle = pEvent.getBundle();
					String symbolicName = notifyingBundle.getSymbolicName();
					if (!symbolicName.equals(PLUGIN_ID)) {
						return;
					}
					if (notifyingBundle.getState() == Bundle.ACTIVE) {
						registerContribution();
						bundleContext.removeBundleListener(this);
					}
				}
			});
		}
			
	}
	
	private void registerContribution() {
		VirtualEnvironmentPlugin.getInstance().addContributionDeclaration( GreyfacePlugin.getInstance());
	}
	

}
