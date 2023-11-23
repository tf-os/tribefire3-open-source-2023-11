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
package com.braintribe.gwt.gme.constellation.client;

import java.util.function.Supplier;

import com.braintribe.gwt.gme.tetherbar.client.TetherBar;
import com.braintribe.gwt.gme.tetherbar.client.TetherBarElement;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ClosableWindow;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.user.client.Timer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;

public class BrowsingConstellationDialog extends ClosableWindow {
	
	private Supplier<BrowsingConstellation> browsingConstellationProvider;
	private Supplier<MasterDetailConstellation> localModeMasterDetailConstellationProvider;
	private BrowsingConstellation browsingConstellation;
	private PersistenceGmSession gmSession;
	private BorderLayoutContainer borderLayoutContainer;
	
	public BrowsingConstellationDialog() {
		int width = Math.max(com.google.gwt.user.client.Window.getClientWidth() - 300, 1000);
		int height = Math.max(com.google.gwt.user.client.Window.getClientHeight() - 250, 750);
		addStyleName("gmeSelectionConstellationDialog");
		setSize(width + "px", height + "px");
		setModal(true);
		setBodyBorder(false);
		setBorders(false);
		setClosable(false);
		getHeader().setHeight(20);
		
		borderLayoutContainer = new BorderLayoutContainer();
		borderLayoutContainer.setBorders(false);
		this.add(borderLayoutContainer);
	}
	
	/**
	 * Configures the required {@link BrowsingConstellation} provider.
	 */
	@Required
	public void setBrowsingConstellationProvider(Supplier<BrowsingConstellation> browsingConstellationProvider) {
		this.browsingConstellationProvider = browsingConstellationProvider;
	}
	
	/**
	 * Configures the required provider for {@link MasterDetailConstellation} (local mode).
	 */
	@Required
	public void setLocalModeMasterDetailConstellationProvider(Supplier<MasterDetailConstellation> localModeMasterDetailConstellationProvider) {
		this.localModeMasterDetailConstellationProvider = localModeMasterDetailConstellationProvider;
	}
	
	/**
	 * Configures the required session used for configuring the browsingConstellation and the masterDetailConstellation.
	 */
	@Required
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	public void showDialogForEntity(ModelPath modelPath, ValueDescriptionBean nameAndDescription) {
		initBrowsingConstellation(modelPath, nameAndDescription);
		this.show();
	}
	
	/**
	 * Returns the BorderLayoutContainer used within this dialog. Further items can be added to every position, except for the center.
	 */
	public BorderLayoutContainer getBorderLayoutContainer() {
		return borderLayoutContainer;
	}
	
	private void initBrowsingConstellation(ModelPath modelPath, ValueDescriptionBean nameAndDescription) throws RuntimeException {
		if (browsingConstellation == null) {
			browsingConstellation = browsingConstellationProvider.get();
			browsingConstellation.configureGmSession(gmSession);
			borderLayoutContainer.setCenterWidget(browsingConstellation);
		} else
			browsingConstellation.getTetherBar().clearTetherBarElements();
		
		prepareLocalModeTetherBarElement(modelPath, nameAndDescription);
	}
	
	private void prepareLocalModeTetherBarElement(ModelPath modelPath, ValueDescriptionBean nameAndDescription) {
		TetherBarElement element = new TetherBarElement(modelPath, nameAndDescription.getValue(), nameAndDescription.getDescription(),
				getMasterDetailConstellationProvider(localModeMasterDetailConstellationProvider, modelPath));
		TetherBar tetherBar = browsingConstellation.getTetherBar();
		tetherBar.insertTetherBarElement(0, element);
		tetherBar.setSelectedThetherBarElement(element);
	}
	
	private Supplier<MasterDetailConstellation> getMasterDetailConstellationProvider(final Supplier<MasterDetailConstellation> originalProvider,
			final ModelPath modelPath) {
		return new Supplier<MasterDetailConstellation>() {
			@Override
			public MasterDetailConstellation get() {
				final MasterDetailConstellation masterDetailConstellation = originalProvider.get();
				masterDetailConstellation.configureGmSession(gmSession);
				//masterDetailConstellation.addWorkWithEntityActionListener(getWorkWithEntityActionListener(browsingConstellation, masterDetailConstellation));
				//masterDetailConstellation.addInstantiatedEntityListener(getInstantiatedEntityListener(masterDetailConstellation));
				masterDetailConstellation.setContent(modelPath);
				new Timer() {
					@Override
					public void run() {
						masterDetailConstellation.getCurrentMasterView().select(0, false);
					}
				}.schedule(500);
				return masterDetailConstellation;
			}
		};
	}
	
	public static class ValueDescriptionBean {
		private String value;
		private String description;
		
		public ValueDescriptionBean(String value, String description) {
			setValue(value);
			setDescription(description);
		}
		
		public String getValue() {
			return value;
		}
		
		public void setValue(String value) {
			this.value = value;
		}
		
		public String getDescription() {
			return description;
		}
		
		public void setDescription(String description) {
			this.description = description;
		}
	}

}
