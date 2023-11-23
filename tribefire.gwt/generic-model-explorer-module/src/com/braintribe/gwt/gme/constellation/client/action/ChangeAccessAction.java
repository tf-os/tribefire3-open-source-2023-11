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

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.CustomizationConstellation;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.ModelEnvironmentSetListener;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.accessapi.ModelEnvironment;
import com.braintribe.model.accessdeployment.HardwiredAccess;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.bapi.AvailableAccesses;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;

public class ChangeAccessAction extends Action implements HasSubMenu, ModelEnvironmentSetListener {
	private static final Logger logger = new Logger(ChangeAccessAction.class);
	
	private CustomizationConstellation customizationConstellation;
	private String tribeFireExplorerURL = "/tribefire-explorer";
	private Supplier<String> sessionIdProvider;
	private Menu accessIdsMenu; 
	private Map<String, MenuItem> itemCache = new FastMap<>();
	private boolean useHardwired = true;
	private Function<String, Future<ModelEnvironment>> modelEnvironmentProvider;
	private Supplier<Future<AvailableAccesses>> availableAccessesDataFutureProvider;
	
	/**
	 * Configures the provider used for loading the model environment.
	 */
	@Required
	public void setModelEnvironmentProvider(Function<String, Future<ModelEnvironment>> modelEnvironmentProvider) {
		this.modelEnvironmentProvider = modelEnvironmentProvider;
	}

	/**
	 * Configures the required {@link CustomizationConstellation} which is the parent for this action.
	 */
	@Required
	public void setCustomizationConstellation(CustomizationConstellation customizationConstellation) {
		this.customizationConstellation = customizationConstellation;
		this.customizationConstellation.addModelEnvironmentSetListener(this);
	}
	
	/**
	 * Configures the required provider used for providing the accesses information.
	 */
	@Required
	public void setAvailableAccessesDataFutureProvider(Supplier<Future<AvailableAccesses>> availableAccessesDataFutureProvider) {
		this.availableAccessesDataFutureProvider = availableAccessesDataFutureProvider;
	}
	
	@Configurable
	public void setSessionIdProvider(Supplier<String> sessionIdProvider) {
		this.sessionIdProvider = sessionIdProvider;
	}
	
	@Configurable
	public void setUseHardwired(boolean useHardwired) {
		this.useHardwired = useHardwired;
	}
	
	@Configurable
	public void setTribeFireExplorerURL(String tribeFireExplorerURL){
		this.tribeFireExplorerURL = tribeFireExplorerURL;
	}
	
	public ChangeAccessAction() {
		setName(LocalizedText.INSTANCE.switchTo());
		setTooltip(LocalizedText.INSTANCE.switchTo());
		setIcon(ConstellationResources.INSTANCE.switchToSmall());
		setHoverIcon(ConstellationResources.INSTANCE.switchToBig());
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		//NOP
	}
	
	@Override
	public void onModelEnvironmentSet() {
		MenuItem oldItem = itemCache.get(customizationConstellation.getAccessId());
		if (oldItem != null)
			oldItem.setIcon(ConstellationResources.INSTANCE.tick());
	}
	
	private void exchangeMetaModel(String accessId) {
		GlobalState.mask(LocalizedText.INSTANCE.loadingMetaData());
		
		modelEnvironmentProvider.apply(accessId) //
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

	@Override
	public Menu getSubMenu() {
		accessIdsMenu = new Menu();

		availableAccessesDataFutureProvider.get().get(getAvailableAccessessCalback());
		
		return accessIdsMenu;
	}
	
	private AsyncCallback<AvailableAccesses> getAvailableAccessessCalback() {
		return AsyncCallbacks.of(result -> {
			boolean separated = false;
			int notHardWiredCount = 0;
			
			for (IncrementalAccess access : result.getAccesses()) {
				boolean hardwired = (access instanceof HardwiredAccess);
				if (!hardwired)
					notHardWiredCount++;
				
				final MenuItem menuItem = prepareMenuItem(access.getName(), access.getExternalId(), hardwired);
				if (!separated && hardwired && notHardWiredCount > 0) {
					separated = true;
					accessIdsMenu.add(new SeparatorMenuItem());
				}
				accessIdsMenu.add(menuItem);
			}
			
			setEnabled(accessIdsMenu.getWidgetCount() > 0);
		}, e -> {
			logger.error(LocalizedText.INSTANCE.errorGettingAccessIds(), e);
			e.printStackTrace();
		});
	}
	
	private MenuItem prepareMenuItem(final String name, final String externalId, final boolean hardwired) {
		final MenuItem menuItem = new MenuItem(name != null ? name : externalId);
		itemCache.put(externalId, menuItem);
		if (customizationConstellation.getAccessId() != null && customizationConstellation.getAccessId().equals(externalId))
			menuItem.setIcon(ConstellationResources.INSTANCE.tick());
		menuItem.addSelectionHandler(prepareMenuSelectionHandler(externalId, hardwired, menuItem));
		return menuItem;
	}

	private SelectionHandler<Item> prepareMenuSelectionHandler(final String externalId, final boolean hardwired, final MenuItem menuItem) {
		return event -> {
			if (hardwired) {
				if (!customizationConstellation.getAccessId().equals(externalId)) {
					MenuItem oldItem1 = itemCache.get(customizationConstellation.getAccessId());
					if (oldItem1 != null)
						oldItem1.setIcon(null);
					exchangeMetaModel(externalId);
					menuItem.setIcon(ConstellationResources.INSTANCE.tick());
				}
				return;
			}
			
			if (!useHardwired) {
				MenuItem oldItem2 = itemCache.get(customizationConstellation.getAccessId());
				if (oldItem2 != null)
					oldItem2.setIcon(null);
				exchangeMetaModel(externalId);
				menuItem.setIcon(ConstellationResources.INSTANCE.tick());
				return;
			}
			
			MessageBox box = new MessageBox(SafeHtmlUtils.fromSafeConstant("&nbsp;") , SafeHtmlUtils.fromString(LocalizedText.INSTANCE.whereTo())) {
				@Override
				protected String getText(PredefinedButton button) {
					if (button.equals(PredefinedButton.YES))
						return LocalizedText.INSTANCE.newWindow();
					else if (button.equals(PredefinedButton.NO))
						return LocalizedText.INSTANCE.thisWindow();
					return super.getText(button);
				}
			};
			box.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO, PredefinedButton.CANCEL);
			box.setIcon(MessageBox.ICONS.question());
			box.addDialogHideHandler(event1 -> {
				if (event1.getHideButton().equals(PredefinedButton.NO)) {
					MenuItem oldItem = itemCache.get(customizationConstellation.getAccessId());
					if(oldItem != null) oldItem.setIcon(null);
					exchangeMetaModel(externalId);
					menuItem.setIcon(ConstellationResources.INSTANCE.tick());
				} else if (event1.getHideButton().equals(PredefinedButton.YES)) {
					String sessionString = "";
					if (sessionIdProvider != null) {
						String sessionId;
						try {
							sessionId = sessionIdProvider.get();
							sessionString = "&"+"sessionId"+"="+sessionId;
						} catch (RuntimeException e) {
							sessionString = "";
							ErrorDialog.show("Error while providing sessionId", e);
						}			
					}
					Window.open(tribeFireExplorerURL + "?" + "accessId" + "=" + URL.encodeQueryString(externalId) + sessionString, "_blank", "");
				}
			});
			box.show();
		};
	}
	
}
