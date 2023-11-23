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

import com.braintribe.gwt.modeller.client.resources.ModellerModuleResources;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.modellerfilter.EntityTypeFilter;
import com.braintribe.model.modellerfilter.EnumTypeFilter;
import com.braintribe.model.modellerfilter.NegationRelationshipFilter;
import com.braintribe.model.modellerfilter.RelationshipFilter;
import com.braintribe.model.modellerfilter.view.ModellerView;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;

class TypeEntry extends FlowPanel{
	
		private final GmModellerTypesOverviewPanel gmModellerTypesOverviewPanel;
		GmType type;
		RelationshipFilter relationshipFilter;
		RadioButton isFocusedType;
		FlowPanel typeName;
		Button add;
		Button visibility;
		Button delete;
		
		public TypeEntry(GmModellerTypesOverviewPanel gmModellerTypesOverviewPanel, GmType type) {
			this.gmModellerTypesOverviewPanel = gmModellerTypesOverviewPanel;
			this.type = type;
			relationshipFilter = getFilter();
			addStyleName("typeEntry");
			add(getIsFocusedType());
			add(getTypeNameElement());
			add(getAdd());
			add(getVisibility());
			if(type.getDeclaringModel() == this.gmModellerTypesOverviewPanel.gmMetaModel)
			add(getDelete());
		}
		
		private RelationshipFilter getFilter() {
			ModellerView view = this.gmModellerTypesOverviewPanel.modellerView;
			
			for(RelationshipFilter filter : view.getExcludesFilterContext().getOperands()) {
				if(filter instanceof NegationRelationshipFilter) {
					NegationRelationshipFilter nrf = (NegationRelationshipFilter) filter;					
					if(nrf.getOperand() instanceof EntityTypeFilter) {
						EntityTypeFilter etf = (EntityTypeFilter)nrf.getOperand();
						if(etf.getEntityType() == type) {
							return nrf;
						}
					}
				}
			}
			
			return null;
		}
		
		public RadioButton getIsFocusedType() {
			if(isFocusedType == null) {
				isFocusedType = new RadioButton(type.getTypeSignature());
				isFocusedType.setStyleName("gmModellerRadioButton");
				isFocusedType.setTitle("Set default focused type of current view");
				//isFocusedType.addStyleName("gmModellerDefaultCheckBox");
				isFocusedType.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						if(/*event.getValue() && */ TypeEntry.this.gmModellerTypesOverviewPanel.modellerView != null) {
							TypeEntry.this.gmModellerTypesOverviewPanel.modellerView.setFocusedType(event.getValue() ? type : null);							
						}
						TypeEntry.this.gmModellerTypesOverviewPanel.adaptOverview(TypeEntry.this.gmModellerTypesOverviewPanel.filter.getValue());
					}
				});
				isFocusedType.setValue(this.gmModellerTypesOverviewPanel.modellerView != null && this.gmModellerTypesOverviewPanel.modellerView.getFocusedType() == type);
			}
			return isFocusedType;
		}
		
		public FlowPanel getTypeNameElement() {
			if(typeName == null) {
				typeName = new FlowPanel();
				typeName.addStyleName("typeEntry-name");
				String name = this.gmModellerTypesOverviewPanel.getTypeName(type);
				typeName.setTitle(type.getTypeSignature());
				typeName.getElement().setInnerText(name);
				
				typeName.addDomHandler(new ClickHandler() {					
					@Override
					public void onClick(ClickEvent event) {
						TypeEntry.this.gmModellerTypesOverviewPanel.modeller.focus(type.getTypeSignature(), true);
					}
				}, ClickEvent.getType());
			}
			return typeName;
		}
		
		public Button getAdd() {
			if(add == null) {
				String img = "<img src='"+ModellerModuleResources.INSTANCE.addToCircle().getSafeUri().asString()+"'>";
				add = new Button(img,new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						TypeEntry.this.gmModellerTypesOverviewPanel.modeller.addType(type.getTypeSignature());
					}
				});
				add.addStyleName("modellerButton");
			}
			return add;
		}
		
		public Button getVisibility() {
			if(visibility == null) {
				String img = "<img src='"+ModellerModuleResources.INSTANCE.visible().getSafeUri().asString()+"'>";
				visibility = new Button(img,new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
//						if(TypeEntry.this.gmModellerTypesOverviewPanel.excludes.contains(type))
//							TypeEntry.this.gmModellerTypesOverviewPanel.excludes.remove(type);
//						else
//							TypeEntry.this.gmModellerTypesOverviewPanel.excludes.add(type);						
//						visibility.getElement().getStyle().setOpacity(TypeEntry.this.gmModellerTypesOverviewPanel.excludes.contains(type) ? 0.1 : 1);
						
						if(type instanceof GmEntityType || type instanceof GmEnumType) {
							PersistenceGmSession session = gmModellerTypesOverviewPanel.session;
							ModellerView view = gmModellerTypesOverviewPanel.modellerView;
							NestedTransaction nt = session.getTransaction().beginNestedTransaction();
							
							if(relationshipFilter == null) {
								NegationRelationshipFilter n = session.create(NegationRelationshipFilter.T);
								RelationshipFilter tf = null;
								if(type instanceof GmEntityType) {
									tf = session.create(EntityTypeFilter.T);
									((EntityTypeFilter) tf).setEntityType((GmEntityType) type);
								}else if(type instanceof GmEnumType) {
									tf = session.create(EnumTypeFilter.T);
									((EnumTypeFilter) tf).setEnumType((GmEnumType) type);
								}
								n.setOperand(tf);
								view.getExcludesFilterContext().getOperands().add(n);
								relationshipFilter = n;
							}else {
								view.getExcludesFilterContext().getOperands().remove(relationshipFilter);
								session.deleteEntity(relationshipFilter);
								relationshipFilter = null;
							}
							
							nt.commit();
						}
						
						visibility.getElement().getStyle().setOpacity(relationshipFilter != null ? 0.1 : 1);
						if(relationshipFilter == null)
							TypeEntry.this.gmModellerTypesOverviewPanel.modeller.removeType(type.getTypeSignature());
						else
							TypeEntry.this.gmModellerTypesOverviewPanel.modeller.rerender();						
					}
				});
				visibility.addStyleName("modellerButton");
				visibility.getElement().getStyle().setOpacity(relationshipFilter != null ? 0.1 : 1);
			}
			return visibility;
		}
		
		public Button getDelete() {
			if(delete == null) {
				String img = "<img src='"+ModellerModuleResources.INSTANCE.delete().getSafeUri().asString()+"'>";
				delete = new Button(img,new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						TypeEntry.this.gmModellerTypesOverviewPanel.modeller.removeType(type);
					}
				});
				delete.addStyleName("modellerButton");
			}
			return delete;
		}

		public void adaptTypeSig() {
			typeName.getElement().setInnerText(this.gmModellerTypesOverviewPanel.getTypeName(type));
		}
		
		public void adapt() {
			isFocusedType.setValue(this.gmModellerTypesOverviewPanel.modellerView != null && this.gmModellerTypesOverviewPanel.modellerView.getFocusedType() == type);
			visibility.getElement().getStyle().setOpacity(relationshipFilter != null ? 0.1 : 1);
//			visibility.getElement().getStyle().setOpacity(TypeEntry.this.gmModellerTypesOverviewPanel.excludes.contains(type) ? 0.1 : 1);
		}
		
	}