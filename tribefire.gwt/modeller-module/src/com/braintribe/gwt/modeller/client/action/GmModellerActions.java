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
package com.braintribe.gwt.modeller.client.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.modeller.client.GmModeller;
import com.braintribe.gwt.modeller.client.history.GmModellerHistory;
import com.braintribe.gwt.modeller.client.history.GmModellerHistoryListener;
import com.braintribe.gwt.modeller.client.resources.ModellerModuleResources;
import com.braintribe.gwt.smartmapper.client.SmartMapper;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.modellergraph.GmModellerMode;
import com.braintribe.model.processing.modellergraph.ModelGraphConfigurationsNew;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;

public class GmModellerActions implements GmModellerHistoryListener{

	private boolean readOnly = false;
	
	private GmModeller modeller;
	private SmartMapper smartMapper;
	private ModelGraphConfigurationsNew configurations;
	private GmModellerHistory history;
	
	Map<Integer, ModelAction> registry = new HashMap<Integer, ModelAction>();
	ModelAction addType;
	ModelAction goTo;
	ModelAction next;
	ModelAction previous;
	ModelAction delete;
	ModelAction backToModeller;
	
	public GmModellerActions() {
		registry.put(KeyCodes.KEY_NUM_PLUS, addType());
		registry.put(KeyCodes.KEY_G, goTo());
		registry.put(KeyCodes.KEY_LEFT, previous());
		registry.put(KeyCodes.KEY_RIGHT, next());
		
		registry.put(KeyCodes.KEY_DELETE, delete());
	}
	
	public void setModeller(GmModeller modeller) {
		this.modeller = modeller;
	}
	
	public void setSmartMapper(SmartMapper smartMapper) {
		this.smartMapper = smartMapper;
	}

	public void setHistory(GmModellerHistory history) {
		this.history = history;
	}
	
	public void setConfigurations(ModelGraphConfigurationsNew configurations) {
		this.configurations = configurations;
	}
	
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	@Override
	public void onHistoryChanged(GmModellerHistory history) {
		previous().setHidden(!history.hasPrevious() || configurations.modellerMode == GmModellerMode.mapping);
		next().setHidden(!history.hasNext() || configurations.modellerMode == GmModellerMode.mapping);
	}
	
	public List<Pair<ActionTypeAndName, ModelAction>> getActions() {
		List<Pair<ActionTypeAndName, ModelAction>> actions = new ArrayList<>();
//		registry.forEach((integer, action) -> {
		actions.add(new Pair<>(new ActionTypeAndName(addType().getName()), addType()));
		actions.add(new Pair<>(new ActionTypeAndName(goTo().getName()), goTo()));
		actions.add(new Pair<>(new ActionTypeAndName(delete().getName()), delete()));		
		actions.add(new Pair<>(new ActionTypeAndName("Back"), previous()));
		actions.add(new Pair<>(new ActionTypeAndName(next().getName()), next()));
		actions.add(new Pair<>(new ActionTypeAndName(backToModeller().getName()), backToModeller()));
		
		if(smartMapper != null) {
			for(Pair<String, ModelAction> smartMapperActionPairs : smartMapper.getSmapperActions()) {
				ModelAction smartMapperAction = smartMapperActionPairs.getSecond();
				ModelAction wrap = new ModelAction() {
					
					@Override
					public void perform(TriggerInfo triggerInfo) {
						smartMapperAction.perform(triggerInfo);
					}
					
					@Override
					protected void updateVisibility() {
						setHidden(smartMapperAction.getHidden() || configurations.modellerMode != GmModellerMode.mapping);
					}
				};
				wrap.setName(smartMapperAction.getName());
				wrap.setIcon(smartMapperAction.getIcon());
				wrap.setHoverIcon(smartMapperAction.getHoverIcon());
				wrap.put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
				
				smartMapperAction.addPropertyListener((source, property) -> {
					wrap.setHidden(smartMapperAction.getHidden() || configurations.modellerMode != GmModellerMode.mapping);
					modeller.fireGmSelectionListeners();
				});
				
				actions.add(new Pair<>(new ActionTypeAndName(smartMapperActionPairs.getFirst()), wrap));
			}			
		}
		
//		});
		return actions;
	}
	
	public boolean hasAction(KeyDownEvent evt) {
		return registry.containsKey(evt.getNativeKeyCode());
	}
	
	public void perform(KeyDownEvent evt) {
		ModelAction action = registry.get(evt.getNativeKeyCode());
		if(action != null) {			
			evt.preventDefault();
			evt.stopPropagation();
			action.perform(null);
		}
	}
	
	public ModelAction addType() {
		if(addType == null) {
			addType = new ModelAction() {
				
				@Override
				public void perform(TriggerInfo triggerInfo) {
					modeller.addType("?");
				}
				
				@Override
				protected void updateVisibility() {
					if(configurations.modellerMode == GmModellerMode.mapping)
						setHidden(true);
					else
						setHidden(readOnly);
				}
			};
			addType.setIcon(ModellerModuleResources.INSTANCE.add());
			addType.setHoverIcon(ModellerModuleResources.INSTANCE.add());
			addType.setName("Add");
			addType.setHidden(false);
			addType.put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
		}
		return addType;
	}
	
	public ModelAction goTo() {
		if(goTo == null) {
			goTo = new ModelAction() {
				
				@Override
				public void perform(TriggerInfo triggerInfo) {
					modeller.focus("?", false);
				}
				
				@Override
				protected void updateVisibility() {
					if(configurations.modellerMode == GmModellerMode.mapping)
						setHidden(true);
					else
						setHidden(false);
				}
			};
			goTo.setIcon(ModellerModuleResources.INSTANCE.goTo());
			goTo.setHoverIcon(ModellerModuleResources.INSTANCE.goTo());
			goTo.setName("Go To");
			goTo.setHidden(false);
			goTo.put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
		}
		return goTo;
	}
	
	public ModelAction next() {
		if(next == null) {
			next = new ModelAction() {
				
				@Override
				public void perform(TriggerInfo triggerInfo) {
					history.next();
				}
				
				@Override
				protected void updateVisibility() {
					if(configurations.modellerMode == GmModellerMode.mapping)
						setHidden(true);
					else
						setHidden(!history.hasNext());
				}
			};
			next.setIcon(ModellerModuleResources.INSTANCE.next());
			next.setHoverIcon(ModellerModuleResources.INSTANCE.next());
			next.setName("Next");
			next.setHidden(true);
			next.put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
		}
		return next;
	}
	
	public ModelAction previous() {
		if(previous == null) {
			previous = new ModelAction() {
				
				@Override
				public void perform(TriggerInfo triggerInfo) {
					history.previous(false);
				}
				
				@Override
				protected void updateVisibility() {
					if(configurations.modellerMode == GmModellerMode.mapping)
						setHidden(true);
					else
						setHidden(!history.hasPrevious());
				}
			};
			previous.setIcon(ModellerModuleResources.INSTANCE.previous());
			previous.setHoverIcon(ModellerModuleResources.INSTANCE.previous());
			previous.setName("Previous");
			previous.setHidden(true);
			previous.put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
		}
		return previous;
	}
	
	public ModelAction delete() {
		if(delete == null) {
			delete = new ModelAction() {
				
				@Override
				public void perform(TriggerInfo triggerInfo) {
					ModelPath mp = modeller.getCurrentSelection().get(0);
					GmType type = (GmType) mp.last().getValue();
					modeller.removeType(type);
				}
				
				@Override
				protected void updateVisibility() {
					if(configurations.modellerMode == GmModellerMode.mapping)
						setHidden(true);
					else {
						if(!readOnly) {
							boolean isTypeSelected = false;
							if(modeller.getCurrentSelection() != null) {
								ModelPath mp = modeller.getCurrentSelection().get(0);
								Object candidate = mp != null ? mp.last().getValue() : null;
								if(candidate != null && candidate instanceof GmType) {
									GmType type = (GmType) candidate;
									isTypeSelected = type.getDeclaringModel() == modeller.getModel();	
								}else
									setHidden(true);
							}
							setHidden(!isTypeSelected);
						}else
							setHidden(true);
					}
				}
			};
			delete.setIcon(ModellerModuleResources.INSTANCE.remove());
			delete.setHoverIcon(ModellerModuleResources.INSTANCE.remove());
			delete.setName("Delete");
			delete.setHidden(true);		
			delete.put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
		}
		return delete;
	}
	
	public ModelAction backToModeller() {
		if(backToModeller == null) {
			backToModeller = new ModelAction() {
				
				@Override
				public void perform(TriggerInfo triggerInfo) {
					modeller.showModeller();
				}
				
				@Override
				protected void updateVisibility() {
					if(configurations.modellerMode == GmModellerMode.mapping)
						setHidden(false);
					else
						setHidden(true);
				}
			};
			backToModeller.setIcon(ModellerModuleResources.INSTANCE.previous());
			backToModeller.setHoverIcon(ModellerModuleResources.INSTANCE.previous());
			backToModeller.setName("Modeller");
			backToModeller.setHidden(true);
			backToModeller.put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
		}
		return backToModeller;
	}
	
}
