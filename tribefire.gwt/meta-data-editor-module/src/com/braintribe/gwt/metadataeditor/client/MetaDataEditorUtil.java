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
package com.braintribe.gwt.metadataeditor.client;

import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.codec.registry.client.CodecRegistry;
import com.braintribe.gwt.gme.assemblypanel.client.resources.AssemblyPanelResources;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelCss;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelResources;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.metadataeditor.client.resources.MetaDataEditorResources;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.Predicate;
import com.braintribe.model.meta.data.PredicateErasure;
import com.braintribe.model.meta.data.display.DisplayInfo;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;

public class MetaDataEditorUtil {

	public static List<Property> preparePropertyList(EntityType<?> entityType, EntityType<?> baseEntityType, boolean useOnlyClassSpecial) {
		List<String> baseTypes = new ArrayList<>();
		if (useOnlyClassSpecial && baseEntityType != null)
			for (Property property : baseEntityType.getProperties())
				baseTypes.add(property.getName());
		
		List<Property> result = new ArrayList<Property>();
		for (Property property : entityType.getProperties()) {
			if (!useOnlyClassSpecial) {
				//add all property
				result.add(property);	
			} else {
				if (baseTypes.indexOf(property.getName()) < 0) {
					result.add(property);
				} else if (property.getName().equals("name") || property.getName().equals("description")) {
					//special for DisplayInfo - get also Name from basic Types
					for (EntityType<?> entitySuperType : entityType.getSuperTypes()) {
						if (entitySuperType.getTypeSignature().equals((DisplayInfo.class).getName()))																
							result.add(property);
					}
				}						
			}
		}
		return result;
	}
		
	public static List<Object> preparePropertyAndPredicateList(EntityType<?> entityType, EntityType<?> baseEntityType, boolean useOnlyClassSpecial) {
		List<String> baseTypes = new ArrayList<>();
		if (useOnlyClassSpecial && baseEntityType != null)
			for (Property property : baseEntityType.getProperties())
				baseTypes.add(property.getName());
		
		List<Object> result = new ArrayList<>();
				
		if (Predicate.T.isAssignableFrom(entityType)) {
			result.add(entityType);
		} else {		
			for (Property property : entityType.getProperties()) {
				if (!useOnlyClassSpecial) {
					//add all property
					result.add(property);	
				} else {
					if (baseTypes.indexOf(property.getName()) < 0) {
						result.add(property);
					} else if (property.getName().equals("name") || property.getName().equals("description")) {
						//special for DisplayInfo - get also Name from basic Types
						for (EntityType<?> entitySuperType : entityType.getSuperTypes()) {
							if (entitySuperType.getTypeSignature().equals((DisplayInfo.class).getName()))																
								result.add(property);
						}
					}						
				}
			}
		}
		return result;
	}	

	public static String prepareStringValue(Object propertyValue, GenericModelType valueType,
			CodecRegistry<String> codecRegistry, Boolean readOnly, Boolean useGray, String useCase) {	
	    String stringValue = null;				
		if (valueType == null)
			valueType = GMF.getTypeReflection().getType(propertyValue);
		if (propertyValue != null) {			
			if (codecRegistry != null) {
				Codec<Object, String> codec = codecRegistry.getCodec(valueType.getJavaType());
				if (codec != null) {
					try {
						stringValue = codec.encode(propertyValue);
					} catch (CodecException e) {
						//logger.error("Error while getting value renderer value.", e);
						e.printStackTrace();
					}
				}
			}			
									
			if (stringValue == null) {					
				if ((valueType instanceof EntityType || valueType instanceof EnumType) && propertyValue instanceof GenericEntity) {
					//String selectiveInformation = SelectiveInformationResolver.resolve((EntityType<?>) valueType, (GenericEntity) propertyValue,  modelContextBuilder.entity((GenericEntity) propertyValue));
					//RVE - must be used this version, because this get own resolver - for showing value we need original Resolver from where the MetaData are
					String selectiveInformation = SelectiveInformationResolver.resolve((EntityType<?>) valueType, (GenericEntity) propertyValue, getMetaData((GenericEntity) propertyValue), useCase);
					if (selectiveInformation != null && !selectiveInformation.trim().equals("")) {
						stringValue =  selectiveInformation;
					}
					
				} /*else if (valueType instanceof EnumType) {
				
				//if (valueType instanceof EntityType || valueType instanceof EnumType) {
					String valString = propertyValue.toString();
					DisplayInfo displayInfo = getDisplayInfo(valueType, modelContextBuilder);
					if (displayInfo != null && displayInfo.getName() != null) {
						valString = I18nTools.getLocalized(displayInfo.getName());
					}
					stringValue = valString;
				} */
			}
				
		}

		if (valueType.getJavaType() == Boolean.class) {
			stringValue = prepareBooleanValue((Boolean) propertyValue, readOnly, useGray);			
		}
		
		return stringValue != null ? stringValue : (propertyValue != null ? propertyValue.toString() : "");
		//return SafeHtmlUtils.htmlEscape(stringValue != null ? stringValue : (propertyValue != null ? propertyValue.toString() : ""));
	}	
	
	public static String prepareBooleanValue(Boolean value, Boolean readOnly, Boolean useGray) {
		String booleanClass;			
		PropertyPanelCss css = PropertyPanelResources.INSTANCE.css();
		//if (readOnly || !propertyModel.isEditable()) {
		if (readOnly) {
			if (value == null)
				booleanClass = css.checkNullReadOnlyValue();
			else
				booleanClass = value ? css.checkedReadOnlyValue() : css.uncheckedReadOnlyValue();
		} else {
			if (value == null)
				booleanClass = css.checkNullValue();
			else
				booleanClass = value ? css.checkedValue() : css.uncheckedValue();
		}
		
		String stringValue = "<div class='" + booleanClass;
		if (value != null) {
			stringValue += " " + (value ? "CHECKED" : "UNCHECKED");
		}
		stringValue += "'/>";
		
		if (useGray) {
			stringValue += prepareBooleanValue(null, readOnly, false);
		}
		
		return stringValue;
	}		
	
	//in case of PredicateErasure is returned Predicate superType
	public static EntityType<?> getPredicateEntityType(EntityType<?> predicateType) {
		if (PredicateErasure.T.isAssignableFrom(predicateType)) {
			for (EntityType<?> superType : predicateType.getSuperTypes()) {
			   if (Predicate.T.isAssignableFrom(superType) && MetaData.T.isAssignableFrom(superType) && !PredicateErasure.T.isAssignableFrom(superType))
				   return superType;			   
			}			
		}
		
		return predicateType;
	}
	
	public static String appendStringGray(String text) {
    	if (text == null)
    		return null;
    	
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<span class=\"").append(AssemblyPanelResources.INSTANCE.css().propertyNameStyle()).append(" ").append(GMEUtil.PROPERTY_NAME_CSS).append("\">");
		stringBuilder.append(text);
	    stringBuilder.append("</span>");
    	
    	return stringBuilder.toString(); 
    }

	public static String appendStringCell(String text) {
    	if (text == null)
    		return null;
    	
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<div class=\"").append(MetaDataEditorResources.INSTANCE.constellationCss().cellBasic()).append(" ").append(GMEUtil.PROPERTY_NAME_CSS).append("\">");
		stringBuilder.append(text);
	    stringBuilder.append("</div>");
    	
    	return stringBuilder.toString(); 
    }    

	public static String appendStringCellMain(String text) {
    	if (text == null)
    		return null;
    	
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<span class=\"").append(MetaDataEditorResources.INSTANCE.constellationCss().cellMain()).append(" ").append(GMEUtil.PROPERTY_NAME_CSS).append("\">");
		stringBuilder.append(text);
	    stringBuilder.append("</span>");
    	
    	return stringBuilder.toString(); 
    }    

	public static String appendStringCellIcon(String text) {
    	if (text == null)
    		return null;
    	
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<span class=\"").append(MetaDataEditorResources.INSTANCE.constellationCss().cellIcon()).append(" ").append(GMEUtil.PROPERTY_NAME_CSS).append("\">");
		stringBuilder.append(text);
	    stringBuilder.append("</span>");
    	
    	return stringBuilder.toString(); 
    }    	
	
	public static Boolean canEditMetaData(ModelPath modelPath, MetaData metaData, Boolean isPropertyMetaData) {		
		if (modelPath == null || metaData == null)
			return false;
		
		Boolean canEdit = true;  //Do not allow Remove metaData which are declared at Dependency, in this case allow delete metaData only from override
		GmMetaModel editingMetaModel = null;
		GmEntityTypeInfo editingEntityType = null;
		GmEnumTypeInfo editingEnumType = null;
		GmPropertyInfo editingProperty = null;
		GmEnumConstantInfo editingEnumConstant = null;
		for (ModelPathElement pathElement : modelPath) {
			if (pathElement.getValue() instanceof GmMetaModel)
				editingMetaModel = pathElement.getValue();
			if (pathElement.getValue() instanceof GmEntityTypeInfo)
				editingEntityType = pathElement.getValue();
			if (pathElement.getValue() instanceof GmEnumTypeInfo)
				editingEnumType = pathElement.getValue();
			if (pathElement.getValue() instanceof GmPropertyInfo)
				editingProperty = pathElement.getValue();
			if (pathElement.getValue() instanceof GmEnumConstantInfo)
				editingEnumConstant = pathElement.getValue();
		}
	
		if (editingEnumConstant != null) {
			if (editingEnumType == null)	
				editingEnumType = editingEnumConstant.declaringTypeInfo();
			if (editingEnumType != editingEnumConstant.declaringTypeInfo() || editingMetaModel != editingEnumType.getDeclaringModel()) {
				canEdit = !editingEnumConstant.getMetaData().contains(metaData); 	
			}									
		} else if (editingProperty != null) {
			if (editingEntityType == null)
				editingEntityType = editingProperty.declaringTypeInfo();
			if (editingEntityType != editingProperty.declaringTypeInfo() || editingMetaModel != editingEntityType.getDeclaringModel()) {
				canEdit = !editingProperty.getMetaData().contains(metaData); 	
			}																															
		} else if (editingEnumType != null) {
			if (editingMetaModel != editingEnumType.declaringModel()) {
				canEdit = !editingEnumType.getMetaData().contains(metaData);
			}
		} else if (editingEntityType != null) {
			if (editingMetaModel != editingEntityType.declaringModel()) {
				if (isPropertyMetaData)
					canEdit = !editingEntityType.getPropertyMetaData().contains(metaData);
				else	
					canEdit = !editingEntityType.getMetaData().contains(metaData);
			}
		} 				
		return canEdit;
	}
}
