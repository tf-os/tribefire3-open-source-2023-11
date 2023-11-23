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
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.BrowsingConstellationDialog;
import com.braintribe.gwt.gme.constellation.client.BrowsingConstellationDialog.ValueDescriptionBean;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.packaging.Packaging;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.menu.Menu;

public class ShowPackagingInfoAction extends Action implements InitializableBean {

	private static final Logger logger = new Logger(ShowPackagingInfoAction.class);

	private Packaging packaging;
	private Supplier<BrowsingConstellationDialog> dialogProvider;
	private Menu parentMenu;
	private BrowsingConstellationDialog dialog;
	private Supplier<String> accessIdProvider;
	private Supplier<? extends Widget> toolBarSupplier;
	private Widget toolBar;
	private Supplier<Future<Packaging>> packagingProvider;

	public ShowPackagingInfoAction() {
		setName(LocalizedText.INSTANCE.about());
		setIcon(ConstellationResources.INSTANCE.info());
		setHoverIcon(ConstellationResources.INSTANCE.infoBig());
		setTooltip(LocalizedText.INSTANCE.packagingInfoDescription());
	}

	/**
	 * Configures the required provider for the Packaging.
	 */
	@Required
	public void setPackagingProvider(Supplier<Future<Packaging>> packagingProvider) {
		this.packagingProvider = packagingProvider;
	}

	@Required
	public void setBrowsingConstellationDialogProvider(Supplier<BrowsingConstellationDialog> dialogProvider) {
		this.dialogProvider = dialogProvider;
	}

	@Required
	public void setParentMenu(Menu parentMenu) {
		this.parentMenu = parentMenu;
	}

	/**
	 * Configures the required provider for accessId.
	 */
	@Required
	public void setAccessIdProvider(Supplier<String> accessIdProvider) {
		this.accessIdProvider = accessIdProvider;
	}

	/**
	 * Configures the toolBar to be placed in the south position.
	 */
	@Configurable
	public void setToolBarSupplier(Supplier<? extends Widget> toolBarSupplier) {
		this.toolBarSupplier = toolBarSupplier;
	}

	@Override
	public void intializeBean() throws Exception {
		getPackaging() //
				.andThen(result -> packaging = result) //
				.onError(e -> {
					logger.error("Packaging Info not available.", e);
					packaging = null;
				});
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		if (parentMenu != null)
			parentMenu.hide();
		if (accessIdProvider.get() == null) {
			GlobalState.showWarning(LocalizedText.INSTANCE.version(getVersionString()));
			return;
		}
		
		if (packaging != null) {
			ModelPath modelPath = new ModelPath();
			RootPathElement rootElement = new RootPathElement(packaging.entityType(), packaging);
			modelPath.add(rootElement);
			getDialog().showDialogForEntity(modelPath, new ValueDescriptionBean(LocalizedText.INSTANCE.packagingInfo(getVersionString()),
					LocalizedText.INSTANCE.packagingInfoDescription()));
		} else {
			MessageBox box = new MessageBox(LocalizedText.INSTANCE.packagingInfo(""),
					LocalizedText.INSTANCE.packagingInfoNotAvailable(getVersionString()));
			box.setIcon(MessageBox.ICONS.info());
			box.show();
		}
	}

	private String getVersionString() {
		if (packaging != null) {
			StringBuilder versionString = new StringBuilder();
			versionString.append(packaging.getVersion()).append(" - ").append(packaging.getTerminalArtifact().getArtifactId()).append("-");
			versionString.append(packaging.getTerminalArtifact().getVersion());
			return "- " + LocalizedText.INSTANCE.tribefireRelease(versionString.toString());
		}

		return "";
	}

	private Future<Packaging> getPackaging() throws RuntimeException {
		return packagingProvider.get();
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
