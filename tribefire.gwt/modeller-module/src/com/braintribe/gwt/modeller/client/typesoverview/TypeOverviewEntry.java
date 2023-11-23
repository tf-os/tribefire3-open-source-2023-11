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
package com.braintribe.gwt.modeller.client.typesoverview;

import java.util.TreeSet;
import java.util.stream.Collectors;

import com.braintribe.gwt.modeller.client.resources.ModellerModuleResources;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.modellerfilter.ModelFilter;
import com.braintribe.model.modellerfilter.NegationRelationshipFilter;
import com.braintribe.model.modellerfilter.RelationshipFilter;
import com.braintribe.model.modellerfilter.view.ModellerView;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

class TypeOverviewEntry extends FlowPanel{
	
		private final GmModellerTypesOverviewPanel gmModellerTypesOverviewPanel;
		GmMetaModel model;
		RelationshipFilter relationshipFilter;
		FlowPanel dependency;
		FlowPanel typesWrapper;
		//Set<TypeEntry> types;
		String filter;
		boolean hasTypes = false;
		
		Button expand;
		Label name;
		Button visibility;
		Button delete;
		
		public TypeOverviewEntry(GmModellerTypesOverviewPanel gmModellerTypesOverviewPanel, GmMetaModel model, String filter) {
			this.gmModellerTypesOverviewPanel = gmModellerTypesOverviewPanel;
			this.model = model;
			this.filter = filter;
			this.relationshipFilter = getFilter();
			addStyleName("typeOverviewEntry");
			add(getExpand());
			add(getDependency(model));
			add(getTypesWrapper());
		}
		
		private RelationshipFilter getFilter() {
			ModellerView view = this.gmModellerTypesOverviewPanel.modellerView;
			
			for(RelationshipFilter filter : view.getExcludesFilterContext().getOperands()) {
				if(filter instanceof NegationRelationshipFilter) {
					NegationRelationshipFilter nrf = (NegationRelationshipFilter) filter;					
					if(nrf.getOperand() instanceof ModelFilter) {
						ModelFilter mf = (ModelFilter)nrf.getOperand();
						if(mf.getModel() == model) {
							return nrf;
						}
					}
				}
			}
			
			return null;
		}
		
		public boolean hasTypes() {
			//return types != null && types.size() > 0;
			return hasTypes;
		}
		
		public FlowPanel getDependency(GmMetaModel model) {
			if(dependency == null) {
				dependency = new FlowPanel();
				dependency.addStyleName("typeOverviewEntry-dependency");	
				
				dependency.add(getExpand());
				dependency.add(getName());
				dependency.add(getVisibility());
//				if(model != gmMetaModel && !readOnly)
				dependency.add(getDelete());
				getDelete().setVisible(model != this.gmModellerTypesOverviewPanel.gmMetaModel && !this.gmModellerTypesOverviewPanel.readOnly);
			}
			return dependency;
		}
		
		public Label getName() {
			if(name == null) {
				name = new Label(this.gmModellerTypesOverviewPanel.getModelName(model));
				name.addDomHandler(new ClickHandler() {					
					@Override
					public void onClick(ClickEvent event) {
						getTypesWrapper().setVisible(!getTypesWrapper().isVisible());
					}
				}, ClickEvent.getType());
			}
			return name;
		}
		
		public Button getExpand() {
			if(expand == null) {
				String img = "<img src='"+ModellerModuleResources.INSTANCE.expanded().getSafeUri().asString()+"'>";
				expand = new Button(img, new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						// TODO Auto-generated method stub
						
					}
				});
				expand.addStyleName("modellerButton");
			}
			return expand;
		}
		
		public Button getVisibility() {
			if(visibility == null) {
				String img = "<img src='"+ModellerModuleResources.INSTANCE.visibility().getSafeUri().asString()+"'>";
				visibility = new Button(img,new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
//						if(TypeOverviewEntry.this.gmModellerTypesOverviewPanel.excludes.contains(model))
//							TypeOverviewEntry.this.gmModellerTypesOverviewPanel.excludes.remove(model);
//						else
//							TypeOverviewEntry.this.gmModellerTypesOverviewPanel.excludes.add(model);						
//						visibility.getElement().getStyle().setOpacity(TypeOverviewEntry.this.gmModellerTypesOverviewPanel.excludes.contains(model) ? 0.1 : 1);
						
						PersistenceGmSession session = gmModellerTypesOverviewPanel.session;
						ModellerView view = gmModellerTypesOverviewPanel.modellerView;
						NestedTransaction nt = session.getTransaction().beginNestedTransaction();
						
						if(relationshipFilter == null) {
							NegationRelationshipFilter n = session.create(NegationRelationshipFilter.T);
							ModelFilter mf = session.create(ModelFilter.T);
							mf.setModel(model);
							n.setOperand(mf);
							view.getExcludesFilterContext().getOperands().add(n);
							relationshipFilter = n;
						}else {
							view.getExcludesFilterContext().getOperands().remove(relationshipFilter);
							session.deleteEntity(relationshipFilter);
							relationshipFilter = null;
						}
						
						nt.commit();
						
						visibility.getElement().getStyle().setOpacity(relationshipFilter != null ? 0.1 : 1);
						TypeOverviewEntry.this.gmModellerTypesOverviewPanel.modeller.rerender();
					}
				});
				visibility.getElement().getStyle().setOpacity(relationshipFilter != null ? 0.1 : 1);
				visibility.addStyleName("modellerButton");
			}
			return visibility;
		}
		
		public Button getDelete() {
			if(delete == null) {
				String img = "<img src='"+ModellerModuleResources.INSTANCE.delete().getSafeUri().asString()+"'>";
				delete = new Button(img, new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						TypeOverviewEntry.this.gmModellerTypesOverviewPanel.modeller.removeDependeny(model);
					}
				});
				delete.addStyleName("modellerButton");
			}
			return delete;
		}
		
		public FlowPanel getTypesWrapper() {
			if(typesWrapper == null) {
				typesWrapper = new FlowPanel();
				typesWrapper.addStyleName("typeOverviewEntry-types");
				TreeSet<GmType> set = new TreeSet<GmType>(this.gmModellerTypesOverviewPanel.new GmTypeComparator());
				if(filter == null || filter.equals(""))
					set.addAll(model.getTypes());
				else {
					String modelName = model.getName().contains(":") ? model.getName().split(":")[1] : model.getName();
					if(filter.equalsIgnoreCase(modelName) || modelName.toLowerCase().contains(filter.toLowerCase())) {
						set.addAll(model.getTypes());
					}else {
						set.addAll(model.getTypes().stream().filter((type) -> {
							if(type.isGmEntity() || type.isGmEnum()) {
								String typeSig = this.gmModellerTypesOverviewPanel.getTypeName(type);
								return typeSig.equalsIgnoreCase(filter) || typeSig.toLowerCase().startsWith(filter.toLowerCase()) ||
										typeSig.toLowerCase().contains(filter.toLowerCase()) || typeSig.toLowerCase().endsWith(filter.toLowerCase());
							}
							else
								return false;
						}).collect(Collectors.toSet()));
					}
				}
				for(GmType type : set) {
					if(type.isGmEntity() || type.isGmEnum()) {
						TypeEntry te = this.gmModellerTypesOverviewPanel.typeEntries.get(type);
						if(te == null) {
							te = new TypeEntry(this.gmModellerTypesOverviewPanel, type);
							this.gmModellerTypesOverviewPanel.typeEntries.put(type, te);
						}else
							te.adapt();
						typesWrapper.add(te);
					}
				}
				hasTypes = !set.isEmpty();
			}			
			return typesWrapper;
		}
		
	}