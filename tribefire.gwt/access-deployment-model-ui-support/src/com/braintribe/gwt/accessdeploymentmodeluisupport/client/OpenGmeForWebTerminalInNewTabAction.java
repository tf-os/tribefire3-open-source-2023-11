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
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.extensiondeployment.WebTerminal;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;

public class OpenGmeForWebTerminalInNewTabAction extends ModelAction {
	private String clientUrl;
	private WebTerminal terminal;
	private PersistenceGmSession gmSession;
	private ManipulationListener manipulationListener;
	private Supplier<String> servicesUrlSupplier;
	
	public OpenGmeForWebTerminalInNewTabAction() {
		setName(LocalizedText.INSTANCE.switchTo());
		setIcon(GmViewActionResources.INSTANCE.globe());
		setHoverIcon(GmViewActionResources.INSTANCE.globeBig());
		setHidden(true);
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
		
		manipulationListener = manipulation -> Scheduler.get().scheduleDeferred(this::updateVisibility);
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
	
	/**
	 * Configures the required supplier for the services URL.
	 */
	@Required
	public void setServicesUrlSupplier(Supplier<String> servicesUrlSupplier) {
		this.servicesUrlSupplier = servicesUrlSupplier;
	}
	
	public String getClientUrl() {
		if (clientUrl == null) {
			String href = Location.getHref();
			int index = href.indexOf("?");
			clientUrl = index != -1 ? href.substring(0, index) : href;
		}
		return clientUrl;
	}
	
	@Override
	protected void updateVisibility() {
		setHidden(true);
		
		if (terminal != null && gmSession != null)
			gmSession.listeners().entity(terminal).remove(manipulationListener);
		
		terminal = null;
		
		if (this.modelPaths == null || this.modelPaths.size() != 1)
			return;
		
		List<ModelPath> selection = modelPaths.get(0);
		if (selection.isEmpty())
			return;
		
		ModelPath modelPath = selection.get(selection.size() - 1);
		if (modelPath != null && !modelPath.isEmpty() && modelPath.last().getValue() instanceof WebTerminal) {
			terminal = modelPath.last().getValue();
			if (gmSession != null)
				gmSession.listeners().entity(terminal).add(manipulationListener);
			
			//TODO: originally checked for getDeployed() -> if this check actually needs to be aware of the deployment state an according DDSA request needs to be performed
			if (terminal.getAutoDeploy() && terminal.getExternalId() != null && !terminal.getExternalId().isEmpty()) {
				setHidden(false);
				return;
			}
		}
		setHidden(true);
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {		
		if (terminal != null) {
			String url = servicesUrlSupplier.get();
			if (!url.endsWith("/"))
				url = url + "/";
			
			url = url + "component/";
			url = url + URL.encode(terminal.getPathIdentifier());
			Window.open(url,"_blank","");
		}		
	}	

}
