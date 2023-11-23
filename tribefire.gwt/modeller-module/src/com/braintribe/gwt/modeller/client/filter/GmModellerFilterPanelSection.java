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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessDialog.QuickAccessResult;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessTriggerField;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessTriggerField.QuickAccessTriggerFieldListener;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gxt.gxtresources.blanktextfield.client.BlankTextAppearance;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.modellerfilter.JunctionRelationshipFilter;
import com.braintribe.model.modellerfilter.NegationRelationshipFilter;
import com.braintribe.model.modellerfilter.RelationshipFilter;
import com.braintribe.model.modellerfilter.view.ExtendedJunctionRelationshipFilter;
import com.braintribe.model.processing.modellergraph.filter.GenericFilterStringifier;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class GmModellerFilterPanelSection extends FlowPanel implements QuickAccessTriggerFieldListener, ManipulationListener{
	
	private final GenericFilterStringifier genericFilterStringifier;
	private PersistenceGmSession session;
	private FilterPanelSectionContext context;
	
	private Label nameLabel;
	
	private QuickAccessTriggerField addSectionTextBox;
	private FlowPanel entryWrapperPanel;
	List<PropertyContext> propertyContexts;
	
	boolean printNegation = true;
	
	public GmModellerFilterPanelSection(PersistenceGmSession session, FilterPanelSectionContext context) {
		this(session, context, true);
	}
	
	public GmModellerFilterPanelSection(PersistenceGmSession session, FilterPanelSectionContext context, boolean printNegation) {
		this.printNegation = printNegation;
		this.genericFilterStringifier = new GenericFilterStringifier(printNegation);
		this.context = context;
		this.session = session;
		add(getNameLabel(context.name));
		if(context.useAdd) {
			add(getAddSectionTextBox());
			FlowPanel fp = new FlowPanel();
			fp.addStyleName("gmModelerFilterPanelSeperator");
			add(fp);
		}
		add(getEntryWrapperPanel());		
		adapt();
		addStyleName("gmModelerFilterPanelSection");
		
		if(context.parentEntity instanceof ExtendedJunctionRelationshipFilter) {
			this.session.listeners().entity(context.parentEntity).property("operands").add(this);
			this.session.listeners().entity(context.parentEntity).property("inactiveOperands").add(this);
		}		
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		adapt();
	}
	
	private void adapt() {
		getEntryWrapperPanel().clear();
		
		context.propertyContexts.forEach((propertyContext) -> {
			getEntryWrapperPanel().add(new GmModellerFilterPanelEntry(session, context.parentEntity, propertyContext));
		});
		
		GenericEntity parentEntity = context.parentEntity;
		
		if(parentEntity instanceof ExtendedJunctionRelationshipFilter) {
			ExtendedJunctionRelationshipFilter jrf = (ExtendedJunctionRelationshipFilter) parentEntity;
			
			propertyContexts = new ArrayList<PropertyContext>();
			
			jrf.getOperands().forEach(filter -> {				
				propertyContexts.add(getPropertyContext(filter, "operands", true));	
			});
			
			jrf.getInactiveOperands().forEach(filter -> {				
				propertyContexts.add(getPropertyContext(filter, "inactiveOperands", false));
			});
			
			propertyContexts.sort((o1, o2) -> o1.desc.compareTo(o2.desc));
			
			propertyContexts.forEach(propertyContext -> {
				getEntryWrapperPanel().add(new GmModellerFilterPanelEntry(session, context.parentEntity, propertyContext));
			});			
			
		}		
		
	}
	
	@SuppressWarnings("unused")
	private PropertyContext getPropertyContext(RelationshipFilter filter, String propertyName, boolean selected) {
		PropertyContext propertyContext = new PropertyContext();
		
		propertyContext.filter = filter;
		propertyContext.isOperand = true;
		propertyContext.deletable = true;
		propertyContext.desc = genericFilterStringifier.stringify(filter);
		propertyContext.propertyName = "operands"; //TODO: why is this fixed?
		
		return propertyContext;
	}
	
	public Label getNameLabel(String name) {
		if(nameLabel == null) {
			nameLabel = new Label(name);
			nameLabel.setTitle(name);
			nameLabel.addStyleName("gmModelerFilterPanelEntryTitel");
			
			nameLabel.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					getEntryWrapperPanel().setVisible(!getEntryWrapperPanel().isVisible());
				}
			});
		}
		return nameLabel;
	}
	
	public FlowPanel getEntryWrapperPanel() {
		if(entryWrapperPanel == null) {
			entryWrapperPanel = new FlowPanel();
			entryWrapperPanel.addStyleName("gmModelerFilterPanelEntryWrapper");
		}
		return entryWrapperPanel;
	}
	
	public QuickAccessTriggerField getAddSectionTextBox() {
		if (addSectionTextBox == null) {
			addSectionTextBox = new QuickAccessTriggerField(new BlankTextAppearance());
			addSectionTextBox.addStyleName("gmModelerFilterInput");
			addSectionTextBox.setEmptyText("Add filter...");
			addSectionTextBox.setBorders(false);
			addSectionTextBox.addQuickAccessTriggerFieldListener(this);
			addSectionTextBox.setQuickAccessPanel(getQuickAccessPanelProvider());
		}	
		return addSectionTextBox;
	}
	
	boolean notUsed = true;
	
	@Override
	public void onQuickAccessResult(QuickAccessResult result) {
		if(result != null) {
			RelationshipFilterWeavingContext weavinContext = (RelationshipFilterWeavingContext) result.getObject();
			notUsed = true;	
			RelationshipFilter relationshipFilter = getFilter(weavinContext/*, false*/);
			String key = genericFilterStringifier.stringify(relationshipFilter);
			
			propertyContexts.forEach(propertyContext -> {
				if(propertyContext.desc.equals(key)) {
					notUsed = false;
					return;
				}
			});
			
			if(notUsed) {
				NestedTransaction nt = session.getTransaction().beginNestedTransaction();
				relationshipFilter = getFilter(weavinContext/*, true*/);
				
				GenericEntity parentEntity = context.parentEntity;
				
				if(parentEntity instanceof JunctionRelationshipFilter) {
					JunctionRelationshipFilter jrf = (JunctionRelationshipFilter) parentEntity;
					jrf.getOperands().add(relationshipFilter);
				}
				nt.commit();
			}
		}
	}
	
	private RelationshipFilter getFilter(RelationshipFilterWeavingContext weavinContext/*, boolean useSession*/) {
		EntityType<RelationshipFilter> type = GMF.getTypeReflection().getEntityType(weavinContext.getFilterType());
		RelationshipFilter filter = session.create(type);
		filter.entityType().getProperty(weavinContext.getPropertyName()).set(filter, weavinContext.getValue());
		if(weavinContext.getUseNegation())
			filter = negateFilter(filter);
		if(context.negation)
			filter = negateFilter(filter);
		return filter;
	}
	
	private NegationRelationshipFilter negateFilter(RelationshipFilter filter) {
		NegationRelationshipFilter nf = session.create(NegationRelationshipFilter.T);
		nf.setOperand(filter);
		return nf;
	}
	
	private Supplier<SpotlightPanel> getQuickAccessPanelProvider() {
		return () -> {
			SpotlightPanel quickAccessPanel = context.spotlightPanelProvider.get();
			quickAccessPanel.setMinCharsForFilter(3);
			quickAccessPanel.setUseApplyButton(false);
			quickAccessPanel.setLoadExistingValues(true);
			//quickAccessPanel.setSimpleTypesValuesProvider(context.relationshipFilterValuesProvider);
			quickAccessPanel.setEntitiesFutureProvider(context.relationshipFilterValuesProvider);
			quickAccessPanel.setLoadTypes(false);
//				quickAccessPanel.setIgnoreMetadata(true);
			return quickAccessPanel;
		};
	}
	
}
