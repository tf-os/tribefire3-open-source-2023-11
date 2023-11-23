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
package com.braintribe.gwt.metadataeditor.client.experts;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmCustomTypeInfo;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.meta.override.GmCustomTypeOverride;
import com.braintribe.model.meta.override.GmEntityTypeOverride;
import com.braintribe.model.meta.override.GmEnumConstantOverride;
import com.braintribe.model.meta.override.GmEnumTypeOverride;
import com.braintribe.model.meta.override.GmPropertyOverride;

public class DeclaredOverviewExpert extends AbstractBaseEditorExpert {

	private List<GenericEntity> alreadyAddedList = new ArrayList<>();
	
	@Override
	public void provide(GenericEntity value, GmMetaModel editingModel, GenericEntity editingEntity, CallbackMetaData callback) {
		try {
			if (value == null) {
				callback.onSuccess(new ArrayList<MetaData>());
				return;
			}
			
			alreadyAddedList.clear();
			
			List<MetaDataEditorExpertResultType> list = getMetaDataResultTypeList(value, editingModel, editingEntity);
			List<MetaData> listMetaData = new ArrayList<MetaData>();
			for (MetaDataEditorExpertResultType resultType : list)
				listMetaData.add(resultType.getMetaData());
			callback.onSuccess(listMetaData);
		} catch (Exception e) {
			callback.onFailure(e);
		}
	}

	@Override
	public void provide(GenericEntity value, GmMetaModel editingModel, GenericEntity editingEntity, CallbackExpertResultType callback) {
		try {
			if (value == null) {
				callback.onSuccess(new ArrayList<MetaDataEditorExpertResultType>());
				return;
			}
			
			alreadyAddedList.clear();
			
			List<MetaDataEditorExpertResultType> list = getMetaDataResultTypeList(value, editingModel, editingEntity);
			callback.onSuccess(list);
		} catch (Exception e) {
			callback.onFailure(e);
		}
	}
	
	
	private List<MetaDataEditorExpertResultType> getMetaDataResultTypeList(GenericEntity value, GmMetaModel editingModel, GenericEntity editingEntity) {
		//get MetaData
		List<MetaDataEditorExpertResultType> listResultType = new ArrayList<>();

		if (value instanceof GmPropertyInfo) {
			//GmProperty and GmPropertyOverride version
			addFromPropertyInfo(value, editingModel, editingEntity, listResultType);					
		} else if (value instanceof GmEnumConstantInfo) {
			addFromEnumConstantInfo(value, editingModel, editingEntity, listResultType); 					
		} else if (value instanceof GmCustomTypeInfo) {
			//GmEntityType and GmEnumType and GmEntityTypeOverride and GmEnumTypeOverride versions
			addFromCustomTypeInfo(value, editingModel, listResultType);
		} else if (value instanceof GmMetaModel) {
			addFromGmMetaModel((GmMetaModel) value, listResultType);
		}
		return listResultType;
	}

	private void addFromPropertyInfo(GenericEntity value, GmMetaModel editingModel, GenericEntity editingEntity, List<MetaDataEditorExpertResultType> listResultType) {
		addMetaDataResultType(value, ((GmPropertyInfo) value).getMetaData(), listResultType);
		if (editingEntity == null) {
			return;
		}
		
		if (!(editingEntity instanceof GmEntityTypeInfo))
			return;
		
		GmEntityTypeInfo editingEntityTypeInfo = (GmEntityTypeInfo) editingEntity;
		if (editingModel != null && !editingModel.getTypeOverrides().isEmpty() && !editingEntityTypeInfo.getDeclaringModel().equals(editingModel)) {
			for (GmCustomTypeOverride typeOverride : editingModel.getTypeOverrides()) {
				if ((typeOverride instanceof GmEntityTypeOverride) && (((GmEntityTypeOverride) typeOverride).getEntityType().equals(editingEntityTypeInfo)) && !((GmEntityTypeOverride) typeOverride).getPropertyOverrides().isEmpty())
					for (GmPropertyOverride propertyOverride : ((GmEntityTypeOverride) typeOverride).getPropertyOverrides())
						if (propertyOverride.getProperty().equals(value))
							addMetaDataResultType(propertyOverride, propertyOverride.getMetaData(), listResultType);
							//listMetaData.addAll(propertyOverride.getMetaData());	
			}
		} else if (!editingEntityTypeInfo.getPropertyOverrides().isEmpty()) {
			for (GmPropertyOverride propertyOverride : editingEntityTypeInfo.getPropertyOverrides())
				if (propertyOverride.getProperty().equals(value))
					addMetaDataResultType(propertyOverride, propertyOverride.getMetaData(), listResultType);
					//listMetaData.addAll(propertyOverride.getMetaData());						
		}
		return;
	}

	private void addFromEnumConstantInfo(GenericEntity value, GmMetaModel editingModel,	GenericEntity editingEntity, List<MetaDataEditorExpertResultType> listResultType) {
		//GmEnumConstant andGmEnumConstantOverride version
		addMetaDataResultType(value, ((GmEnumConstantInfo) value).getMetaData(), listResultType);
		
		if (editingEntity == null) {
			return;
		}

		if (!(editingEntity instanceof GmEnumTypeInfo))
			return;				
		
		GmEnumTypeInfo editingEnumTypeInfo = (GmEnumTypeInfo) editingEntity;
		if (editingModel != null && !editingModel.getTypeOverrides().isEmpty() && !editingEnumTypeInfo.getDeclaringModel().equals(editingModel)) {
			for (GmCustomTypeOverride typeOverride : editingModel.getTypeOverrides()) {
				if (typeOverride instanceof GmEnumTypeOverride && (((GmEnumTypeOverride) typeOverride).getEnumType().equals(editingEnumTypeInfo)) && !((GmEnumTypeOverride) typeOverride).getConstantOverrides().isEmpty())
					for (GmEnumConstantOverride enumConstantOverride : ((GmEnumTypeOverride) editingEntity).getConstantOverrides())
						if (enumConstantOverride.getEnumConstant().equals(value))
							addMetaDataResultType(enumConstantOverride, enumConstantOverride.getMetaData(), listResultType);
							//listMetaData.addAll(enumConstantOverride.getMetaData());	
			}
		}
		return;
	}

	private void addFromCustomTypeInfo(GenericEntity value, GmMetaModel editingModel, List<MetaDataEditorExpertResultType> listResultType) {
		if (alreadyAddedList.contains(value)) 
			return;		

		alreadyAddedList.add(value);
		
		addMetaDataResultType(value,((GmCustomTypeInfo) value).getMetaData(), listResultType);
		
		if (editingModel != null && !editingModel.getTypeOverrides().isEmpty())
			for (GmCustomTypeOverride typeOverride : editingModel.getTypeOverrides()) {						
				if (typeOverride instanceof GmEntityTypeOverride && ((GmEntityTypeOverride) typeOverride).getEntityType().equals(value))
					addMetaDataResultType(typeOverride, typeOverride.getMetaData(), listResultType);
					//listMetaData.addAll(typeOverride.getMetaData());	
				else if (typeOverride instanceof GmEnumTypeOverride && ((GmEnumTypeOverride) typeOverride).getEnumType().equals(value))
					addMetaDataResultType(typeOverride, typeOverride.getMetaData(), listResultType);
					//listMetaData.addAll(typeOverride.getMetaData());	
			}
		
		if (value instanceof GmEntityType)
			for (GmEntityType superTypeEntity : ((GmEntityType) value).getSuperTypes()) {
				addFromCustomTypeInfo(superTypeEntity, editingModel, listResultType);
			}			
	}

	private void addMetaDataResultType(GenericEntity value, Set<MetaData> listMetaData, List<MetaDataEditorExpertResultType> listResultType) {
		for (MetaData metaData : listMetaData) {
			MetaDataEditorExpertResultType resultType = new MetaDataEditorExpertResultType();
			resultType.setMetaData(metaData);
			resultType.setOwner(value);
			listResultType.add(resultType);
		}
	}

	private void addFromGmMetaModel(GmMetaModel value, List<MetaDataEditorExpertResultType> listResultType) {
		if (value == null)
			return;
		
		if (alreadyAddedList.contains(value)) 
           return;
		
		alreadyAddedList.add(value);
		
		addMetaDataResultType(value, value.getMetaData(), listResultType);
				
		for (GmMetaModel dependencyModel : value.getDependencies()) {
			addFromGmMetaModel(dependencyModel, listResultType);			
		}						
	}
	
	@Override
	public void provide(GenericEntity value, CallbackEntityType callback) {
		// not used in Declared mode		
	}

	@Override
	public void provide(GenericEntity value, CallbackMetaData callback) {
		provide(value, null, null, callback);		
	}

	@Override
	public void provide(GenericEntity value, CallbackExpertResultType callback) {
		provide(value, null, null, callback);		
	}


}
