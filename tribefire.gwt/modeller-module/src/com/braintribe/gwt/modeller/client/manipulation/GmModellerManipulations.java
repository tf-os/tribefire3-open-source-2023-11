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
package com.braintribe.gwt.modeller.client.manipulation;

import java.util.Set;

import com.braintribe.gwt.modeller.client.GmModeller;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.ManifestationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.override.GmEntityTypeOverride;
import com.braintribe.model.meta.override.GmEnumTypeOverride;
import com.braintribe.model.modellerfilter.view.ExcludesFilterContext;
import com.braintribe.model.modellerfilter.view.IncludesFilterContext;
import com.braintribe.model.modellerfilter.view.ModellerSettings;
import com.braintribe.model.modellerfilter.view.ModellerView;
import com.braintribe.model.modellerfilter.view.RelationshipKindFilterContext;
import com.braintribe.model.processing.modellergraph.ModelGraphConfigurationsNew;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;

public class GmModellerManipulations implements ManipulationListener{
	
	private final static String TYPES = "types";
	private final static String TYPE_OVERRIDES = "typeOverrides";
	private final static String DEPENDENCIES = "dependencies";
	
	//private final static String REINIT_METAMODEL_TOOLS = "reinit meta model tools";
	//private final static String RENDER_TYPE = "(re)render type";
	//private final static String RENDER_DETAIL = "render detail";
	
	private GmModeller modeller;
	private ModelGraphConfigurationsNew configurations;
	
	public void setModeller(GmModeller modeller) {
		this.modeller = modeller;
	}
	
	public void setConfigurations(ModelGraphConfigurationsNew configurations) {
		this.configurations = configurations;
	}
		
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		StringBuilder sb = new StringBuilder("manipulation-" + manipulation.manipulationType().name() + "-");
		ManipulationContext mc = new ManipulationContext(manipulation);
		sb.append(mc.entity.type().getTypeName() + "-" + mc.propertyName);
		
		if(mc.entity.type().isAssignableFrom(ModellerView.T)) {
			//NOP
		}
		else if(mc.entity.type().isAssignableFrom(GmMetaModel.T)) {
			if(mc.propertyName.equals(DEPENDENCIES) || mc.propertyName.equals(TYPES) || mc.propertyName.equals(TYPE_OVERRIDES)) {
				
				if(mc.add) {
					AddManipulation am = (AddManipulation)manipulation;
					am.getItemsToAdd().forEach((k,v) -> {
						modeller.register((GenericEntity) v);
						if(v instanceof GmType)
							modeller.addType(((GmType) v).getTypeSignature());
					});
					
				}else if(mc.remove) {
					RemoveManipulation rm = (RemoveManipulation)manipulation;
					rm.getItemsToRemove().forEach((k,v) -> {
						modeller.unregister((GenericEntity) v);
						if(v instanceof GmType)
							modeller.removeType(((GmType) v).getTypeSignature());
					});
				}
				
				initMetaModelTools();
				resetTypes();
				modeller.rerender();
			}
		}
		else if(mc.entity.type().isAssignableFrom(GmEntityType.T) || mc.entity.type().isAssignableFrom(GmEntityTypeOverride.T)) {
			initMetaModelTools();
			
			if(mc.propertyName.equals("typeSignature")) {
				if(mc.oldValue != null){
					if(mc.oldValue.equals(configurations.currentFocusedType))
						configurations.currentFocusedType = mc.newValue != null ? mc.newValue.toString() : "?";
						
//					Set<String> addedTypes = configurations.addedTypes;
					Set<String> addedTypes = configurations.modellerView.getIncludesFilterContext().getAddedTypes();
					if(addedTypes.contains(mc.oldValue)) {
						addedTypes.remove(mc.oldValue);
						if(mc.newValue != null)
							addedTypes.add(mc.newValue.toString());
					}
				}
			}
			
			GmType typeToAdapt = null;
			if(mc.entity instanceof GmEntityType)
				typeToAdapt = (GmEntityType) mc.entity;
			else if(mc.entity instanceof GmEntityTypeOverride)
				typeToAdapt = ((GmEntityTypeOverride)mc.entity).getEntityType();
			
			if(typeToAdapt != null)
				modeller.adapType(typeToAdapt);
			
			Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {				
				@Override
				public boolean execute() {
					modeller.rerender();
					return false;
				}
			}, 250);			
		}
		else if(mc.entity.type().isAssignableFrom(GmEnumType.T) || mc.entity.type().isAssignableFrom(GmEnumTypeOverride.T)) {
			initMetaModelTools();
		}
		else if(isFilterRelated(mc.entity.entityType())) {
			modeller.rerender();
		}
		
		System.err.println(sb.toString());
	}
	
	private boolean isFilterRelated(EntityType<GenericEntity> type) {
		return type.isAssignableFrom(RelationshipKindFilterContext.T) ||
				type.isAssignableFrom(ExcludesFilterContext.T) ||
				type.isAssignableFrom(IncludesFilterContext.T) ||
				type.isAssignableFrom(ModellerSettings.T); 
	}
	
	private void initMetaModelTools() {
		modeller.initMetaModelTools();
	}
	
	private void resetTypes() {
		modeller.resetTypes();
	}
	
	class ManipulationContext {
		GenericEntity entity;
		String propertyName;
		Object oldValue;
		Object newValue;
		boolean add = false;
		boolean remove = false;
		
		public ManipulationContext(Manipulation m) {
			if(m instanceof PropertyManipulation) {
				LocalEntityProperty lep = (LocalEntityProperty)((PropertyManipulation) m).getOwner();				
				entity = lep.getEntity();
				propertyName = lep.getPropertyName();
				
				add = m instanceof AddManipulation;
				remove = m instanceof RemoveManipulation;
				
				if(m instanceof ChangeValueManipulation) {
					ChangeValueManipulation cvm = (ChangeValueManipulation)m;
					newValue = cvm.getNewValue();					
					oldValue = ((ChangeValueManipulation)cvm.getInverseManipulation()).getNewValue();
				}
			}
			if(m instanceof DeleteManipulation) {
				DeleteManipulation dm = (DeleteManipulation)m;
				entity = dm.getEntity();
				propertyName = "none";
			}
			if(m instanceof ManifestationManipulation) {
				ManifestationManipulation mm = (ManifestationManipulation)m;
				entity = mm.getEntity();
				propertyName = "none";
			}
		}
	}
}