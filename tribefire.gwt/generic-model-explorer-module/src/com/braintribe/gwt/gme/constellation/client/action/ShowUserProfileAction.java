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

import java.util.function.Supplier;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gme.constellation.client.BrowsingConstellationDialog;
import com.braintribe.gwt.gme.constellation.client.BrowsingConstellationDialog.ValueDescriptionBean;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.user.User;
import com.braintribe.processing.async.api.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;

public class ShowUserProfileAction extends Action {

	private ModelEnvironmentDrivenGmSession gmSession;
	private Supplier<BrowsingConstellationDialog> dialogProvider;
	private BrowsingConstellationDialog dialog;
	private Widget toolBar;
	private Supplier<? extends Widget> toolBarSupplier;
	private ServiceRequest request;

	@Required
	public void setGmSession(ModelEnvironmentDrivenGmSession gmSession) {
		this.gmSession = gmSession;
	}

	public void setDialogProvider(Supplier<BrowsingConstellationDialog> dialogProvider) {
		this.dialogProvider = dialogProvider;
	}

	public void setToolBarSupplier(Supplier<? extends Widget> toolBarSupplier) {
		this.toolBarSupplier = toolBarSupplier;
	}

	public void setRequest(ServiceRequest request) {
		this.request = request;
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		GlobalState.showSuccess(LocalizedText.INSTANCE.loadingUser());
		getUser();
	}

	private void getUser() {
		gmSession.eval(request).get(AsyncCallback.of( //
				future -> {
					if (!(future instanceof User))
						return;

					User user = (User) future;
					if (user.getPassword() != null)
						user.setPassword(null);
					ModelPath modelPath = new ModelPath();
					RootPathElement rootPathElement = new RootPathElement(user.entityType(), user);
					modelPath.add(rootPathElement);

					getDialog().showDialogForEntity(modelPath, new ValueDescriptionBean(user.getName(), user.getName()));
				}, Throwable::printStackTrace));
	}

	private BrowsingConstellationDialog getDialog() throws RuntimeException {
		if (dialog == null) {
			dialog = dialogProvider.get();
			dialog.getBorderLayoutContainer().setSouthWidget(getToolBar(), new BorderLayoutData(85));
		}

		return dialog;
	}

	private Widget getToolBar() {
		if (toolBar != null)
			return toolBar;

		toolBar = toolBarSupplier.get();
		return toolBar;
	}

}
