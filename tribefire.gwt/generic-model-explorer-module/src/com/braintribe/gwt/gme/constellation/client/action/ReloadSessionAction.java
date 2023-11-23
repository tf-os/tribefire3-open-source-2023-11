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
package com.braintribe.gwt.gme.constellation.client.action;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gme.constellation.client.CustomizationConstellation;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.SettingsMenu;
import com.braintribe.gwt.gme.constellation.client.expert.ModelEnvironmentProvider;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.ioc.client.Required;
import com.google.gwt.core.client.Scheduler;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;

public class ReloadSessionAction extends Action {
	
	private CustomizationConstellation customizationConstellation;
	private SettingsMenu settingsMenu;
	private ModelEnvironmentProvider modelEnvironmentProvider;
	
	/**
	 * Configures the provider used for loading the model environment.
	 */
	@Required
	public void setModelEnvironmentProvider(ModelEnvironmentProvider modelEnvironmentProvider) {
		this.modelEnvironmentProvider = modelEnvironmentProvider;
	}
	
	@Required
	public void setCustomizationConstellation(CustomizationConstellation customizationConstellation) {
		this.customizationConstellation = customizationConstellation;
	}
	
	@Required
	public void setSettingsMenu(SettingsMenu settingsMenu) {
		this.settingsMenu = settingsMenu;
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		Scheduler.get().scheduleDeferred(() -> { //Needs to be deferred because we are later clearing the parent menu (thus, it is not hidden if not deferred).
			if (customizationConstellation.getAccessId() != null)
				exchangeMetaModel();
			settingsMenu.clear();
			settingsMenu.initializeActions();
		});
	}
	
	private void exchangeMetaModel() {
		GlobalState.mask(LocalizedText.INSTANCE.loadingMetaData());
		
		modelEnvironmentProvider.reload() //
				.andThen(modelEnvironment -> {
					customizationConstellation.exchangeModelEnvironment(modelEnvironment) //
							.andThen(result -> {
								Scheduler.get().scheduleDeferred(() -> {
									GlobalState.unmask();
									if (!result)
										new AlertMessageBox(LocalizedText.INSTANCE.information(), LocalizedText.INSTANCE.notAllowedAccess()).show();
								});
							}).onError(e -> GlobalState.unmask());
				}).onError(e -> GlobalState.unmask());
	}

}
