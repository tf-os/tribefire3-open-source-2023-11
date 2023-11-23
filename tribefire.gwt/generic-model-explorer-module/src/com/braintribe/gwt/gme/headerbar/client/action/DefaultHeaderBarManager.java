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
package com.braintribe.gwt.gme.headerbar.client.action;

import java.util.function.Supplier;

import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Loader;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.Profiling;
import com.braintribe.gwt.logging.client.ProfilingHandle;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 *
 */
public class DefaultHeaderBarManager implements Loader<Void> {
	
	private ModelEnvironmentDrivenGmSession gmSession;
	private ModelEnvironmentDrivenGmSession workbenchSession;
	private Supplier<? extends HeaderBar> headerBarSupplier;
	private Loader<Folder> folderLoader;
	
	public DefaultHeaderBarManager() {
	}
	
	/**
	 * Configures the required {@link ModelEnvironmentDrivenGmSession} used within the actions.
	 */
	@Required
	public void setPersistenceSession(ModelEnvironmentDrivenGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	/**
	 * Configures the workbench session used for preparing the {@link HeaderBar} given via {@link #setHeaderBar(Supplier)}.
	 */
	@Required
	public void setWorkbenchSession(ModelEnvironmentDrivenGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}
	
	/**
	 * Configures the required {@link HeaderBar}.
	 */
	@Required
	public void setHeaderBar(Supplier<? extends HeaderBar> headerBarSupplier) {
		this.headerBarSupplier = headerBarSupplier;
	}
	
	/**
	 * Configures the required loader for the header bar folder.
	 */
	@Required
	public void setFolderLoader(Loader<Folder> folderLoader) {
		this.folderLoader = folderLoader;
	}
	
	@Override
	public void load(final AsyncCallback<Void> asyncCallback) {
		final ProfilingHandle ph = Profiling.start(DefaultHeaderBarManager.class, "Loading headerBar folders", true);
		HeaderBar headerBar = headerBarSupplier.get();
		headerBar.configureGmSession(gmSession);
		headerBar.setWorkbenchSession(workbenchSession);
		folderLoader.load(AsyncCallbacks.of(result -> {
			headerBar.apply(result);
			ph.stop();
			asyncCallback.onSuccess(null);
		}, e -> {
			e.printStackTrace();
			headerBar.apply(null);
			ph.stop();
			asyncCallback.onSuccess(null);
		}));
	}
	
}
