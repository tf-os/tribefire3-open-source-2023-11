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
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.gwt.gme.constellation.client.QueryResultsHandlerView;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gme.templateevaluation.client.expert.TemplateQueryActionHandler.TemplateQueryOpener;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewContext;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmViewChangeListener;
import com.braintribe.gwt.gmview.client.GmViewport;
import com.braintribe.gwt.gmview.client.ModelPathNavigationListener;
import com.braintribe.gwt.gmview.client.ReloadableGmView;
import com.braintribe.gwt.gmview.client.ViewSituationResolver;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedBorderLayoutContainer;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.accessapi.QueryAndSelect;
import com.braintribe.model.accessapi.QueryEntities;
import com.braintribe.model.accessapi.QueryRequest;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.pagination.Paginated;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationContext;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.workbench.TemplateServiceRequestBasedAction;
import com.google.gwt.core.client.Scheduler;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;

/**
 * Constellation which wires up a {@link GmContentView} and a {@link ServiceRequestExecutionPanel} together.
 * @author michel.docouto
 *
 */
public class ServiceRequestExecutionConstellation extends QueryResultsHandlerView implements GmEntityView, InitializableBean,
		ModelPathNavigationListener, ReloadableGmView, TemplateQueryOpenerView, ServiceRequestAutoPagingView {
	
	private Supplier<ServiceRequestExecutionPanel> serviceRequestPanelSupplier;
	private ServiceRequestExecutionPanel serviceRequestPanel;
	private boolean addingContent;
	private boolean hasMoreDataToLoad = false;
	private ServiceRequest serviceRequest;
	private GenericModelType viewType;
	
	public ServiceRequestExecutionConstellation() {
		setBorders(false);
	}
	
	/**
	 * Configures the required supplier for the {@link ServiceRequestExecutionPanel}.
	 */
	@Required
	public void setServiceRequestPanelSupplier(Supplier<ServiceRequestExecutionPanel> serviceRequestPanelSupplier) {
		this.serviceRequestPanelSupplier = serviceRequestPanelSupplier;
	}
	
	@Override
	public void intializeBean() throws Exception {
		wrapperPanel = new ExtendedBorderLayoutContainer();
		wrapperPanel.setBorders(false);
		
		BorderLayoutData northData = new BorderLayoutData(60);
		northData.setMaxSize(500);
		northData.setMinSize(60);
		northData.setSplit(true);
		BorderLayoutContainer wrapperNorthPanel = new ExtendedBorderLayoutContainer();
		wrapperNorthPanel.addStyleName(ConstellationResources.INSTANCE.css().queryConstellationNorth());
		wrapperNorthPanel.setBorders(false);
		
		wrapperNorthPanel.setCenterWidget(getServiceRequestPanel());
		wrapperPanel.setNorthWidget(wrapperNorthPanel, northData);
	}
	
	/**
	 * Configures the required {@link PersistenceGmSession}.
	 */
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
		
		exchangeCurrentContentView(getDefaultContentView(), null);
		setCenterWidget(wrapperPanel);
	}
	
	@Override
	public void configureTemplateQueryOpener(TemplateQueryOpener templateQueryOpener) {
		serviceRequestPanel.configureTemplateQueryOpener(templateQueryOpener);
	}
	
	@Override
	public void configureAutoPaging(boolean autoPaging, boolean pagingEditable) {
		serviceRequestPanel.configureAutoPaging(autoPaging, pagingEditable);
	}
	
	@Override
	public void setContent(ModelPath modelPath) {
		serviceRequest = modelPath.last().getValue();
		serviceRequestPanel.configureRequestData(serviceRequest);
		fireEntityContentSet();
	}
	
	public void setTemplateEvaluationContext(TemplateEvaluationContext templateEvaluationContext, TemplateServiceRequestBasedAction action) {
		serviceRequest = null;
		serviceRequestPanel.configureTemplateEvaluationContext(templateEvaluationContext, action);
		fireEntityContentSet();
	}
	
	@Override
	public void onOpenModelPath(ModelPath modelPath) {
		handleOpenModelPath(modelPath);
	}
	
	@Override
	public void onAddModelPath(ModelPath modelPath) {
		handleOpenModelPath(modelPath, true);
	}
	
	@Override
	public void onOpenModelPath(ModelPath modelPath, TabInformation tabInformation) {
		handleOpenModelPath(modelPath);
	}
	
	@Override
	public void reloadGmView() {
		serviceRequestPanel.evaluateOrExecuteServiceRequest();
		reloadPending = false;
	}
	
	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		super.addSelectionListener(sl);
		if (sl != null)
			serviceRequestPanel.getPropertyPanel().addSelectionListener(sl);
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		super.removeSelectionListener(sl);
		serviceRequestPanel.getPropertyPanel().removeSelectionListener(sl);
	}
	
	@Override
	protected GmViewChangeListener getViewChangeListener() {
		return null;
	}
	
	private void handleOpenModelPath(ModelPath modelPath) {
		handleOpenModelPath(modelPath, false);
	}
	
	private void handleOpenModelPath(ModelPath modelPath, boolean addingMore) {
		if (disposed)
			return; // Ignoring result from a previous execution
		
		Object data = mergeData(modelPath.last().getValue());
		List<Object> list = null;
		
		if (!(data instanceof Paginated))
			hasMoreDataToLoad = false;
		else {
			Paginated paginated = (Paginated) data;
			hasMoreDataToLoad = paginated.getHasMore();
			EntityType<?> paginatedEntityType = GMF.getTypeReflection().getType(paginated);
			Property listProperty = paginatedEntityType.getDeclaredProperties().stream()
					.filter(property -> property.getType().isCollection()
							&& ((CollectionType) property.getType()).getCollectionKind().equals(CollectionKind.list))
					.findAny().orElse(null);
			
			if (listProperty == null)
				throw new RuntimeException("The returned Paginated entity '" + paginated.toSelectiveInformation() + "' must have a declared list property");
			
			list = listProperty.get(paginated);
			GenericModelType listType = listProperty.getType();
			
			if (viewType != null)
				listType = viewType;

			modelPath = new ModelPath();
			modelPath.add(new RootPathElement(listType, list));
		}
		
		//prepareGmContentContext(queryProviderContext, initialQuery);
		
		if (!(serviceRequest instanceof QueryRequest)) {
			ViewSituationResolver<GmContentViewContext> viewSituationResolver = viewSituationResolverSupplier.get();
			List<GmContentViewContext> possibleContentViews = viewSituationResolver.getPossibleContentViews(modelPath.last());
			provideAndExchangeView(possibleContentViews.isEmpty() ? null : possibleContentViews.get(0), null);
		}
		
		if (serviceRequestPanel.isFirstExecution()) {
			//Setting null here means that we have no parent element.
			currentContentView.setContent(null);
		}
		
		if (!addingContent || addingMore) { //This is needed to avoid the problem where the panel was cleared while still populating it
			addingContent = true;
			if (addingMore && currentContentView instanceof GmListView) {
				((GmListView) currentContentView).addContent(modelPath);
				//if (currentContentView instanceof GmSelectionCount)
					//((GmSelectionCount) currentContentView).setMaxSelectCount(maxSelectionCount);
			} else
				currentContentView.setContent(modelPath);
			
			Scheduler.get().scheduleDeferred(() -> addingContent = false);
		}
		
		if (serviceRequestPanel.isFirstExecution())
			selectFirstResult();
		
		//if (currentContentView instanceof GmViewport)
			//handleViewPortWindowChanged((GmViewport) currentContentView, false);
	}
	
	protected Object mergeData(Object data) {
		if (!serviceRequestPanel.isHandlingQuery() || !(data instanceof EntityQueryResult))
			return data;
		
		EntityQueryResult result = (EntityQueryResult) data;
		List<GenericEntity> entities = new ArrayList<>(result.getEntities());
		
		entities = serviceRequestPanel.getDataSession().merge().adoptUnexposed(true).suspendHistory(true).doFor(entities);
		result.setEntities(entities);
		
		return result;
	}
	
	private ServiceRequestExecutionPanel getServiceRequestPanel() {
		if (serviceRequestPanel == null) {
			serviceRequestPanel = serviceRequestPanelSupplier.get();
			serviceRequestPanel.addServiceRequestInitialExecutionListener(serviceRequest -> {
				this.serviceRequest = serviceRequest;
				if (serviceRequest instanceof QueryEntities) {
					EntityQuery entityQuery = ((QueryEntities) serviceRequest).getQuery();
					viewType = changeViewForEntityQuery(entityQuery);
				} else if (serviceRequest instanceof QueryAndSelect) {
					SelectQuery selectQuery = ((QueryAndSelect) serviceRequest).getQuery();
					viewType = changeViewForSelectQuery(selectQuery);
				} else
					viewType = null;
			});
		}
		
		return serviceRequestPanel;
	}
	
	@Override
	protected void handleViewPortWindowChanged(GmViewport source, boolean focusEditor) {
		//if (performingSearch)
			//return;
		
		//performingSearch = true;
		Scheduler.get().scheduleDeferred(() -> {
			if (!hasMoreDataToLoad || !source.isWindowOverlappingFillingSensorArea()) {
				//performingSearch = false;
				return;
			}
			
			serviceRequestPanel.evaluateOrExecuteServiceRequest(false);
		});
	}
	
	@Override
	public void disposeBean() throws Exception {
		super.disposeBean();
		serviceRequestPanel.disposeBean();
		ServiceRequestConstellationScope.scopeManager.closeAndPopScope();
	}

}
