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
package com.braintribe.devrock.virtualenvironment.variable;

import java.util.Iterator;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.ui.IStartup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import com.braintribe.devrock.virtualenvironment.VirtualEnvironmentPlugin;
import com.braintribe.devrock.virtualenvironment.variable.resolver.ToUpperCaseTemplateResolver;
import com.braintribe.devrock.virtualenvironment.variable.resolver.UuidCodeTemplateVariableResolver;

/**
 * @author Pit
 *
 */
@SuppressWarnings("restriction")
public class VariableResolverRegisterer implements IStartup {

	static final String PLUGIN_ID = "org.eclipse.jdt.ui";
	@Override
	public void earlyStartup() {

		final Bundle bundle = Platform.getBundle(PLUGIN_ID);
		if (bundle != null && bundle.getState() == Bundle.ACTIVE) {
			// register resolvers
			registerResolvers();
		} else {
			// register listener to get informed, when plug-in becomes active
			final BundleContext bundleContext = VirtualEnvironmentPlugin.getInstance().getBundle().getBundleContext();
			bundleContext.addBundleListener(new BundleListener() {
				@Override
				public void bundleChanged(final BundleEvent pEvent) {
					final Bundle bundle2 = pEvent.getBundle();
					String symbolicName = bundle2.getSymbolicName();
					if (!symbolicName.equals(PLUGIN_ID)) {
						return;
					}
					if (bundle2.getState() == Bundle.ACTIVE) {
						registerResolvers();
						bundleContext.removeBundleListener(this);
					}
				}
			});
		}
	

	}

	/**
	   * 
	   * Internal method to register resolvers with all context types.
	   * 
	   */
	  private void registerResolvers() {
	    final ContextTypeRegistry codeTemplateContextRegistry = JavaPlugin.getDefault().getCodeTemplateContextRegistry();
	    final Iterator<TemplateContextType> ctIter = codeTemplateContextRegistry.contextTypes();
	    while (ctIter.hasNext()) {
	      final TemplateContextType contextType = ctIter.next();
	      contextType.addResolver(new UuidCodeTemplateVariableResolver());
	      contextType.addResolver( new ToUpperCaseTemplateResolver());
	    }
	  }
}
