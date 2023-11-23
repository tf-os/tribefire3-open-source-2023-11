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
package com.braintribe.gwt.gme.verticaltabpanel.client.action;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Loader;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabPanel;
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
public class DefaultVerticalTabManager implements Loader<Void> {
	
	private ModelEnvironmentDrivenGmSession gmSession;
	private ModelEnvironmentDrivenGmSession workbenchSession;
	private List<VerticalTabPanel> listVerticalTabPanels =  new ArrayList<>();
	private Loader<Folder> folderLoader;
	private Folder rootFolder = null;
	
	public DefaultVerticalTabManager() {
	}
	
	/**
	 * Configures the required {@link ModelEnvironmentDrivenGmSession} used within the actions.
	 */
	@Required
	public void setPersistenceSession(ModelEnvironmentDrivenGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	public ModelEnvironmentDrivenGmSession getPersistenceSession() {
		return this.gmSession;
	}
	
	/**
	 * Configures the workbench session used for preparing the {@link VerticalTabPanel}.
	 */
	@Required
	public void setWorkbenchSession(ModelEnvironmentDrivenGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}
		
	public ModelEnvironmentDrivenGmSession getWorkbenchSession() {
		return this.workbenchSession;
	}
	
	public void addVerticalTabPanel(VerticalTabPanel verticalTab) {
		listVerticalTabPanels.add(verticalTab);
	}
	
	/**
	 * Configures the required loader for the VerticalTabPanel folder.
	 */
	@Required
	public void setFolderLoader(Loader<Folder> folderLoader) {
		this.folderLoader = folderLoader;
	}
	
	public Folder getRootFolder() {
		return this.rootFolder;
	}
	
	@Override
	public void load(final AsyncCallback<Void> asyncCallback) {
		final ProfilingHandle ph = Profiling.start(DefaultVerticalTabManager.class, "Loading tab folders", true);
		for (VerticalTabPanel verticalTab : listVerticalTabPanels) {
			verticalTab.configureGmSession(gmSession);
			verticalTab.setWorkbenchSession(workbenchSession);
		}

		folderLoader.load(AsyncCallbacks.of(result -> {
			rootFolder = result;
			for (VerticalTabPanel verticalTab : listVerticalTabPanels)
			   verticalTab.apply(result);
			ph.stop();
			asyncCallback.onSuccess(null);
		}, e -> {
			e.printStackTrace();
			for (VerticalTabPanel verticalTab : listVerticalTabPanels)
				verticalTab.apply(null);
			ph.stop();
			asyncCallback.onSuccess(null);
		}));
	}
	
}
