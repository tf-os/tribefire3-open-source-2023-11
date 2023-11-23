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
package com.braintribe.gwt.modeller.client.filter;

import java.util.function.Predicate;
import java.util.function.Supplier;

import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.modeller.client.GmModellerPanel;
import com.braintribe.gwt.modeller.client.typesoverview.GmModellerTypesOverviewPanel;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.modellerfilter.view.ModellerView;
import com.braintribe.model.processing.modellergraph.ModelGraphConfigurationsNew;
import com.braintribe.model.processing.modellergraph.filter.relationship.Relationship;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;

public class GmModellerFilterPanel extends FlowPanel{
	
//	private GmModellerPanel gmModellerPanel;
//	private GmModellerTypesOverviewPanel typesOverviewPanel;
	private PersistenceGmSession session;
//	private ModelGraphConfigurationsNew modelGraphConfigurations;
//	private GmMetaModel gmMetaModel;
	private DefaultFiltering defaultFiltering = new DefaultFiltering();
	private ModellerView currentModellerView;
//	private List<GmModellerFilterPanelSection> sections = new ArrayList<>();
	Supplier<SpotlightPanel> spotlightPanelProvider;
	
	FilterPanelSectionContext includes;
	FilterPanelSectionContext excludes;
	FilterPanelSectionContext relationship;
	FilterPanelSectionContext settingz;
	
	private TextBox viewName;
	private FlowPanel sectionsWrapperPanel;
	RelationshipFilterValuesProvider relationshipFilterValuesProvider = new RelationshipFilterValuesProvider();
	
	public GmModellerFilterPanel() {
		includes = DefaultFiltering.prepareIncludesFilterContext(null);
		excludes = DefaultFiltering.prepareExcludesFilterContext(null);
		relationship = DefaultFiltering.prepareRelationshipFilterContext(null);
		settingz = DefaultFiltering.prepareSettingsContext(null);
		
		add(getViewName());
		add(getSectionsWrapperPanel());	
		addStyleName("gmModelerFilterPanel");
	}
	
	private void init() {
		getSectionsWrapperPanel().clear();
		
		excludes.spotlightPanelProvider = spotlightPanelProvider;
		excludes.relationshipFilterValuesProvider = relationshipFilterValuesProvider;
		GmModellerFilterPanelSection exclude = new GmModellerFilterPanelSection(session, excludes, false);
		getSectionsWrapperPanel().add(exclude);
		
		includes.spotlightPanelProvider = spotlightPanelProvider;
		includes.relationshipFilterValuesProvider = relationshipFilterValuesProvider;
		GmModellerFilterPanelSection include = new GmModellerFilterPanelSection(session, includes);
		getSectionsWrapperPanel().add(include);
		
		relationship.spotlightPanelProvider = spotlightPanelProvider;
		relationship.relationshipFilterValuesProvider = relationshipFilterValuesProvider;
		GmModellerFilterPanelSection relationshipSection = new GmModellerFilterPanelSection(session, relationship);
		getSectionsWrapperPanel().add(relationshipSection);
		
		getSectionsWrapperPanel().add(new GmModellerFilterPanelSection(session, settingz));
	}
	
	@SuppressWarnings("unused")
	public void setTypesOverviewPanel(GmModellerTypesOverviewPanel typesOverviewPanel) {
//		this.typesOverviewPanel = typesOverviewPanel;
	}
	
	@SuppressWarnings("unused")
	public void setGmModellerPanel(GmModellerPanel gmModellerPanel) {
//		this.gmModellerPanel = gmModellerPanel;
	}

	public void setModelGraphConfigurations(ModelGraphConfigurationsNew modelGraphConfigurations) {
		defaultFiltering.setModelGraphConfigurations(modelGraphConfigurations);
	}

	public void setGmMetaModel(GmMetaModel gmMetaModel) {
		if(gmMetaModel != null)
			relationshipFilterValuesProvider.setMetaModel(gmMetaModel);	
	}

	public void setModellerView(ModellerView currentModellerView) {
		this.currentModellerView = currentModellerView;
		
		if(currentModellerView != null) {
		
			getViewName().setText(currentModellerView.getName());
			
			includes.parentEntity = currentModellerView.getIncludesFilterContext();
			excludes.parentEntity = currentModellerView.getExcludesFilterContext();
			relationship.parentEntity = currentModellerView.getRelationshipKindFilterContext();
			settingz.parentEntity = currentModellerView.getSettings();
		}else {
			getViewName().setText("");
		}
		
		init();
	}
	
	public TextBox getViewName() {
		if(viewName == null) {
			viewName = new TextBox();
			viewName.getElement().setAttribute("placeholder", "viewName");
			viewName.addStyleName("gmModelerFilterViewName");
			viewName.addChangeHandler(new ChangeHandler() {
				
				@Override
				public void onChange(ChangeEvent event) {
					String newName = viewName.getValue();
					if(newName != null) {
						currentModellerView.setName(newName);
					}
				}
			});
			
			viewName.addKeyUpHandler(new KeyUpHandler() {
				
				@Override
				public void onKeyUp(KeyUpEvent event) {
					if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
						viewName.setFocus(false);
				}
			});
			viewName.setEnabled(false);
		}
		return viewName;
	}
	
	public FlowPanel getSectionsWrapperPanel() {
		if(sectionsWrapperPanel == null) {
			sectionsWrapperPanel = new FlowPanel();
		}
		return sectionsWrapperPanel;
	}

	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}

	public void setQuickAccessPanelProvider(Supplier<SpotlightPanel> spotlightPanelProvider) {
		this.spotlightPanelProvider = spotlightPanelProvider;
	}

	public Predicate<Relationship> getFilter() {
		return defaultFiltering.getFilter(currentModellerView, null /* typesOverviewPanel.getExcludes() */);
	}

}
