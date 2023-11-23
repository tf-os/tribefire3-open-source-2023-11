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
package com.braintribe.gwt.accessdeploymentmodeluisupport.client;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;

public class OpenGmeForAccessInNewTabAction extends ModelAction {
	
	private String clientUrl;
	private String accesIdParameterName = "accessId";
	private String sessionIdParameterName = "sessionId";
	private Supplier<String> sessionIdProvider;
	private String tribeFireExplorerURL = "../tribefire-explorer";
	private IncrementalAccess currentAccess;
	private PersistenceGmSession gmSession;
	private ManipulationListener manipulationListener;
	
	public OpenGmeForAccessInNewTabAction() {
		setName(LocalizedText.INSTANCE.switchTo());
		setIcon(GmViewActionResources.INSTANCE.globe());
		setHoverIcon(GmViewActionResources.INSTANCE.globeBig());
		setHidden(true);
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
		
		manipulationListener = manipulation -> Scheduler.get().scheduleDeferred(this::updateVisibility);
	}
	
	@Configurable
	public void setTribeFireExplorerURL(String tribeFireExplorerURL) {
		this.tribeFireExplorerURL = tribeFireExplorerURL;
	}
	
	@Configurable
	public void setClientUrl(String clientUrl) {
		this.clientUrl = clientUrl;
	}
	
	/**
	 * Configures the session used for listening for manipulations.
	 */
	@Configurable
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	public String getClientUrl() {
		if (clientUrl == null) {
			String href = Location.getHref();
			int index = href.indexOf("?");
			clientUrl = index != -1 ? href.substring(0, index) : href;
		}
		return clientUrl;
	}
	
	@Configurable
	public void setSessionIdParameterName(String sessionIdParameterName) {
		this.sessionIdParameterName = sessionIdParameterName;
	}
	
	@Configurable
	public void setSessionIdProvider(Supplier<String> sessionIdProvider) {
		this.sessionIdProvider = sessionIdProvider;
	}
	
	@Configurable
	public void setAccesIdParameterName(String accesIdParameterName) {
		this.accesIdParameterName = accesIdParameterName;
	}

	@Override
	protected void updateVisibility() {
		setHidden(true);
		
		if (currentAccess != null && gmSession != null)
			gmSession.listeners().entity(currentAccess).remove(manipulationListener);
		
		currentAccess = null;
		if (modelPaths == null || modelPaths.size() != 1)
			return; //check model paths for certain constellation
		
		List<ModelPath> selection = modelPaths.get(0);
		if (selection.isEmpty())
			return;
		
		ModelPath modelPath = selection.get(selection.size() - 1);
		if (modelPath != null && !modelPath.isEmpty() && modelPath.last().getValue() instanceof IncrementalAccess) {
			currentAccess = modelPath.last().getValue();
			if (gmSession != null)
				gmSession.listeners().entity(currentAccess).add(manipulationListener);
			
			//TODO: originally checked for getDeployed() -> if this check actually needs to be aware of the deployment state an according DDSA request needs to be performed
			if (currentAccess.getAutoDeploy() && currentAccess.getExternalId() != null && !currentAccess.getExternalId().isEmpty()) {
				setHidden(false);
				return;
			}
		}
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		String sessionString = "";
		if (sessionIdProvider != null) {
			String sessionId;
			sessionId = sessionIdProvider.get();
			sessionString = "&"+sessionIdParameterName+"="+sessionId;
		}
		
		Window.open(tribeFireExplorerURL + "?" + accesIdParameterName + "=" + URL.encodeQueryString(currentAccess.getExternalId()) + sessionString, "_blank", "");
	}	
}
