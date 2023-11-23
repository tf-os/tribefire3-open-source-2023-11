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
package com.braintribe.gwt.gme.servicerequestpanel.client;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionBar;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.GmCheckListener;
import com.braintribe.gwt.gmview.client.GmCheckSupport;
import com.braintribe.gwt.gmview.client.GmContentSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmContentViewListener;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.GmInteractionListener;
import com.braintribe.gwt.gmview.client.GmInteractionSupport;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.GmMouseInteractionEvent;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.ModelPathNavigationListener;
import com.braintribe.gwt.gmview.client.ReloadableGmView;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedBorderLayoutContainer;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;

/**
 * Constellation which wires up a {@link GmContentView} and a {@link ServiceRequestPanel} together.
 * @author michel.docouto
 *
 */
public class ServiceRequestConstellation extends BorderLayoutContainer implements GmEntityView, InitializableBean, DisposableBean,
		ModelPathNavigationListener, GmCheckSupport, GmInteractionSupport, ReloadableGmView, GmContentSupport, GmViewActionProvider {
	
	private ExtendedBorderLayoutContainer wrapperPanel;
	private ServiceRequestPanel serviceRequestPanel;
	private GmContentView defaultContentView;
	private Supplier<? extends GmContentView> defaultContentViewSupplier;
	private GmContentView currentContentView;
	private GmSelectionListener selectionListener;
	private Set<GmSelectionListener> selectionListeners;
	private GmCheckListener checkListener;
	private Set<GmCheckListener> checkListeners;
	private GmInteractionListener interactionListener;
	private Set<GmInteractionListener> interactionListeners;
	private String useCase;
	private PersistenceGmSession gmSession;
	private Supplier<GmViewActionBar> gmViewActionBarSupplier;
	private Set<GmContentViewListener> contentViewListeners;
	private ExplorerConstellation explorerConstellation;
	private boolean addingContent;
	private boolean reloadPending;
	
	/**
	 * Configures the {@link ServiceRequestPanel} used for editing the {@link ServiceRequest} data.
	 */
	@Required
	public void setServiceRequestPanel(ServiceRequestPanel serviceRequestPanel) {
		this.serviceRequestPanel = serviceRequestPanel;
	}
	
	/**
	 * Configures the default {@link GmContentView} that should be used within.
	 */
	@Required
	public void setDefaultContentView(Supplier<? extends GmContentView> defaultContentViewSupplier) {
		this.defaultContentViewSupplier = defaultContentViewSupplier;
	}
	
	/**
	 * Configures the required {@link GmViewActionBar} that will be updated if the {@link ServiceRequestConstellation} current content view changes.
	 */
	@Required
	public void setGmViewActionBar(Supplier<GmViewActionBar> gmViewActionBarSupplier) {
		this.gmViewActionBarSupplier = gmViewActionBarSupplier;
	}
	
	/**
	 * Configures the required view for handling validation.
	 */
	@Required
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}
	
	public ServiceRequestConstellation() {
		setBorders(false);
	}

	@Override
	public void intializeBean() throws Exception {
		wrapperPanel = new ExtendedBorderLayoutContainer();
		wrapperPanel.setBorders(false);
		
		BorderLayoutData northData = new BorderLayoutData(150);
		northData.setMaxSize(500);
		northData.setMinSize(100);
		northData.setSplit(true);
		BorderLayoutContainer wrapperNorthPanel = new ExtendedBorderLayoutContainer();
		wrapperNorthPanel.addStyleName(ConstellationResources.INSTANCE.css().queryConstellationNorth());
		wrapperNorthPanel.setBorders(false);
		
		wrapperNorthPanel.setCenterWidget(serviceRequestPanel);
		wrapperPanel.setNorthWidget(wrapperNorthPanel, northData);
		
		serviceRequestPanel.configureExplorerConstellation(explorerConstellation);
	}
	
	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
	}
	
	/**
	 * Configures the required {@link PersistenceGmSession}.
	 */
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
		
		exchangeCurrentContentView(getDefaultContentView());
		setCenterWidget(wrapperPanel);
	}
	
	@Override
	public void setContent(ModelPath modelPath) {
		serviceRequestPanel.configureRequestData(modelPath.last().getValue());
		fireEntityContentSet();
	}
	
	@Override
	public void onOpenModelPath(ModelPath modelPath) {
		handleOpenModelPath(modelPath);
	}
	
	@Override
	public void onOpenModelPath(ModelPath modelPath, TabInformation tabInformation) {
		handleOpenModelPath(modelPath);
	}
	
	@Override
	public void reloadGmView() {
		serviceRequestPanel.evaluateServiceRequest();
		reloadPending = false;
	}
	
	@Override
	public boolean isReloadPending() {
		return reloadPending;
	}
	
	@Override
	public void setReloadPending(boolean reloadPending) {
		this.reloadPending = reloadPending;
	}
	
	private GmContentView getDefaultContentView() {
		if (defaultContentView != null)
			return defaultContentView;
		
		defaultContentView = defaultContentViewSupplier.get();
		return defaultContentView;
	}
	
	private void handleOpenModelPath(ModelPath modelPath) {
		if (!addingContent) { //This is needed to avoid the problem that were clearing the panel while still populating it.
			addingContent = true;
			getDefaultContentView().setContent(modelPath);
			
			Scheduler.get().scheduleDeferred(() -> addingContent = false);
		}
	}
	
	private void exchangeCurrentContentView(GmContentView contentView/*, List<QuerySelection> querySelectionList*/) {
		if (currentContentView == contentView) {
			//if (currentContentView instanceof QuerySelectionHandler)
				//((QuerySelectionHandler) currentContentView).configureQuerySelectionList(querySelectionList);
			return;
		}
		
		boolean configureActionBar = false;
		if (currentContentView != null) {
			//if (currentContentView instanceof GmViewport)
				//((GmViewport) currentContentView).removeGmViewportListener(getViewportListener());
			currentContentView.setContent(null);
			currentContentView.removeSelectionListener(getSelectionListener());
			if (currentContentView instanceof GmCheckSupport)
				((GmCheckSupport) currentContentView).removeCheckListener(getCheckListener());
			if (currentContentView instanceof GmInteractionSupport)
				((GmInteractionSupport) currentContentView).removeInteractionListener(getInteractionListener());
			if (currentContentView instanceof Widget)
				wrapperPanel.remove((Widget) currentContentView);
			configureActionBar = true;
		}
		
		currentContentView = contentView;
		
		if (currentContentView != null) {
			currentContentView.configureUseCase(useCase);
			currentContentView.configureGmSession(gmSession);
			//if (currentContentView instanceof GmViewport)
				//((GmViewport) currentContentView).addGmViewportListener(getViewportListener());
			//if (currentContentView instanceof QuerySelectionHandler)
				//((QuerySelectionHandler) currentContentView).configureQuerySelectionList(querySelectionList);
			currentContentView.addSelectionListener(getSelectionListener());
			if (currentContentView instanceof GmCheckSupport)
				((GmCheckSupport) currentContentView).addCheckListener(getCheckListener());
			if (currentContentView instanceof GmInteractionSupport)
				((GmInteractionSupport) currentContentView).addInteractionListener(getInteractionListener());
			if (currentContentView instanceof Widget)
				wrapperPanel.setCenterWidget((Widget) currentContentView);
			if (currentContentView instanceof GmListView)
				((GmListView) currentContentView).configureTypeForCheck(null);
		}
		
		if (configureActionBar) {
			GmViewActionBar gmViewActionBar = gmViewActionBarSupplier.get();
			if (contentView instanceof GmViewActionProvider)
				gmViewActionBar.prepareActionsForView((GmViewActionProvider) contentView);
			else
				gmViewActionBar.prepareActionsForView(null);
		}
		
		forceLayout();
	}
	
	private GmSelectionListener getSelectionListener() {
		if (selectionListener == null) {
			selectionListener = gmSelectionSupport -> {
				if (selectionListeners != null) {
					List<GmSelectionListener> listenersCopy = new ArrayList<>(selectionListeners);
					listenersCopy.forEach(l -> l.onSelectionChanged(gmSelectionSupport));
				}
			};
		}
		
		return selectionListener;
	}
	
	@Override
	public void addGmContentViewListener(GmContentViewListener listener) {
		if (listener != null) {
			if (contentViewListeners == null)
				contentViewListeners = new LinkedHashSet<>();
			contentViewListeners.add(listener);
		}
	}
	
	@Override
	public void removeGmContentViewListener(GmContentViewListener listener) {
		if (contentViewListeners != null) {
			contentViewListeners.remove(listener);
			if (contentViewListeners.isEmpty())
				contentViewListeners = null;
		}
	}
	
	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		if (sl != null) {
			if (selectionListeners == null)
				selectionListeners = new LinkedHashSet<>();
			
			if (!selectionListeners.contains(sl)) {
				selectionListeners.add(sl);
				serviceRequestPanel.getPropertyPanel().addSelectionListener(sl);
			}
		}
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		if (selectionListeners != null) {
			selectionListeners.remove(sl);
			if (selectionListeners.isEmpty())
				selectionListeners = null;
		}
	}
	
	@Override
	public ModelPath getFirstSelectedItem() {
		return currentContentView.getFirstSelectedItem();
	}
	
	@Override
	public GmContentView getView() {
		return currentContentView;
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		return currentContentView.getCurrentSelection();
	}

	@Override
	public boolean isSelected(Object element) {
		return currentContentView.isSelected(element);
	}

	@Override
	public void select(int index, boolean keepExisting) {
		currentContentView.select(index, keepExisting);
	}
	
	@Override
	public void addCheckListener(GmCheckListener cl) {
		if (cl != null) {
			if (checkListeners == null)
				checkListeners = new LinkedHashSet<>();
			checkListeners.add(cl);
		}
	}
	
	@Override
	public void removeCheckListener(GmCheckListener cl) {
		if (checkListeners != null) {
			checkListeners.remove(cl);
			if (checkListeners.isEmpty())
				checkListeners = null;
		}
	}
	
	@Override
	public List<ModelPath> getCurrentCheckedItems() {
		if (currentContentView instanceof GmCheckSupport)
			return ((GmCheckSupport) currentContentView).getCurrentCheckedItems();
		
		return null;
	}
	
	@Override
	public ModelPath getFirstCheckedItem() {
		if (currentContentView instanceof GmCheckSupport)
			return ((GmCheckSupport) currentContentView).getFirstCheckedItem();
		
		return null;
	}
	
	@Override
	public boolean isChecked(Object element) {
		if (currentContentView instanceof GmCheckSupport)
			return ((GmCheckSupport) currentContentView).isChecked(element);
		
		return false;
	}
	
	@Override
	public boolean uncheckAll() {
		if (currentContentView instanceof GmCheckSupport)
			return ((GmCheckSupport) currentContentView).uncheckAll();
		
		return false;
	}
	
	@Override
	public void addInteractionListener(GmInteractionListener il) {
		if (il != null) {
			if (interactionListeners == null)
				interactionListeners = new LinkedHashSet<>();
			interactionListeners.add(il);
		}
	}

	@Override
	public void removeInteractionListener(GmInteractionListener il) {
		if (interactionListeners != null) {
			interactionListeners.remove(il);
			if (interactionListeners.isEmpty())
				interactionListeners = null;
		}
	}
	
	@Override
	public ModelPath getContentPath() {
		return currentContentView.getContentPath();
	}
	
	@Override
	public PersistenceGmSession getGmSession() {
		return this.gmSession;
	}
	
	@Override
	public String getUseCase() {
		return useCase;
	}
	
	@Override
	public ActionProviderConfiguration getActions() {
		return currentContentView instanceof GmViewActionProvider ? ((GmViewActionProvider) currentContentView).getActions() : null;
	}
	
	@Override
	public boolean isFilterExternalActions() {
		return currentContentView instanceof GmViewActionProvider ? ((GmViewActionProvider) currentContentView).isFilterExternalActions() : false; 
	}
	
	@Override
	public void setActionManager(GmContentViewActionManager actionManager) {
		// NOP
	}
	
	private GmCheckListener getCheckListener() {
		if (checkListener == null) {
			checkListener = gmSelectionSupport -> {
				if (checkListeners != null)
					checkListeners.forEach(l -> l.onCheckChanged(gmSelectionSupport));
			};
		}
		
		return checkListener;
	}
	
	private GmInteractionListener getInteractionListener() {
		if (interactionListener == null) {
			interactionListener = new GmInteractionListener() {
				@Override
				public void onDblClick(GmMouseInteractionEvent event) {
					if (interactionListeners != null)
						interactionListeners.forEach(l -> l.onDblClick(event));
				}
				
				@Override
				public void onClick(GmMouseInteractionEvent event) {
					if (interactionListeners != null)
						interactionListeners.forEach(l -> l.onClick(event));
				}
				
				@Override
				public boolean onBeforeExpand(GmMouseInteractionEvent event) {
					boolean canceled = false; 
					if (interactionListeners != null) {
						for (GmInteractionListener listener : interactionListeners) {
							boolean cancel = listener.onBeforeExpand(event);
							if (!canceled && cancel)
								canceled = true;
						}
					}
					return canceled;
				}
			};
		}
		
		return interactionListener;
	}
	
	private void fireEntityContentSet() {
		if (contentViewListeners != null)
			contentViewListeners.forEach(l -> l.onContentSet(ServiceRequestConstellation.this));
	}
	
	@Override
	public void disposeBean() throws Exception {
		serviceRequestPanel.disposeBean();
		
		if (defaultContentView instanceof DisposableBean)
			((DisposableBean) defaultContentView).disposeBean();
		
		ServiceRequestConstellationScope.scopeManager.closeAndPopScope();
	}

}
