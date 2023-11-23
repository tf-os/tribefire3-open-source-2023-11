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
package com.braintribe.gwt.genericmodelgxtsupport.client;

import com.braintribe.gwt.gmview.client.IconProvider;
import com.braintribe.model.extensiondeployment.meta.DynamicSelectList;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.data.constraint.Pattern;
import com.braintribe.model.meta.data.prompt.VirtualEnum;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * Context for preparing a property field.
 * 
 * The initialValue is the value to be set initially in the field, and can be null.
 * The regex is the regular expression to be applied to the field, and can be also null (This only applies for TextField).
 * The useAlternativeField is set to true for using an alternative field in case of simple type modelType.
 * 
 * @author michel.docouto
 *
 */
public class PropertyFieldContext {
	
	private boolean mandatory;
	private boolean password;
	private GenericModelType modelType;
	private Object initialValue;
	private Pattern regex; // PGA TODO rename field to pattern (and getter too)
	private boolean useAlternativeField;
	private String useCase;
	private PersistenceGmSession gmSession;
	private VirtualEnum virtualEnum;
	private int minLenght;
	private int maxLenght;
	private GenericEntity parentEntity;
	private EntityType<?> parentEntityType;
	private String propertyName;
	private Object minValue;
	private Object maxValue;
	private DynamicSelectList dynamicSelectList;
	private boolean instantiable = true;
	private boolean referenceable = true;
	private IconProvider iconProvider;
	private boolean readOnly;
	private boolean handlingCollection;
	
	public boolean isMandatory() {
		return mandatory;
	}
	
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	
	public boolean getPassword() {
		return password;
	}

	public void setPassword(boolean password) {
		this.password = password;
	}

	public GenericModelType getModelType() {
		return modelType;
	}

	public void setModelType(GenericModelType modelType) {
		this.modelType = modelType;
	}

	public Object getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(Object initialValue) {
		this.initialValue = initialValue;
	}

	public Pattern getRegex() {
		return regex;
	}

	public void setRegex(Pattern regex) {
		this.regex = regex;
	}
	
	public boolean getUseAlternativeField() {
		return useAlternativeField;
	}
	
	public void setUseAlternativeField(boolean useAlternativeField) {
		this.useAlternativeField = useAlternativeField;
	}

	public String getUseCase() {
		return useCase;
	}

	public void setUseCase(String useCase) {
		this.useCase = useCase;
	}

	public PersistenceGmSession getGmSession() {
		return gmSession;
	}

	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	public VirtualEnum getVirtualEnum() {
		return virtualEnum;
	}
	
	public void setVirtualEnum(VirtualEnum virtualEnum) {
		this.virtualEnum = virtualEnum;
	}
	
	public int getMinLenght() {
		return minLenght;
	}
	
	public void setMinLenght(int minLenght) {
		this.minLenght = minLenght;
	}
	
	public int getMaxLenght() {
		return maxLenght;
	}
	
	public void setMaxLenght(int maxLenght) {
		this.maxLenght = maxLenght;
	}
	
	public GenericEntity getParentEntity() {
		return parentEntity;
	}
	
	public void setParentEntity(GenericEntity parentEntity) {
		this.parentEntity = parentEntity;
	}
	
	public EntityType<?> getParentEntityType() {
		return parentEntityType;
	}
	
	public void setParentEntityType(EntityType<?> parentEntityType) {
		this.parentEntityType = parentEntityType;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
	public Object getMinValue() {
		return minValue;
	}
	
	public void setMinValue(Object minValue) {
		this.minValue = minValue;
	}
	
	public Object getMaxValue() {
		return maxValue;
	}
	
	public void setMaxValue(Object maxValue) {
		this.maxValue = maxValue;
	}
	
	public DynamicSelectList getDynamicSelectList() {
		return dynamicSelectList;
	}
	
	public void setDynamicSelectList(DynamicSelectList dynamicSelectList) {
		this.dynamicSelectList = dynamicSelectList;
	}
	
	public void setInstantiable(boolean instantiable) {
		this.instantiable = instantiable;
	}
	
	public boolean isInstantiable() {
		return instantiable;
	}
	
	public void setReferenceable(boolean referenceable) {
		this.referenceable = referenceable;
	}
	
	public boolean isReferenceable() {
		return referenceable;
	}

	public IconProvider getIconProvider() {
		return iconProvider;
	}

	public void setIconProvider(IconProvider iconProvider) {
		this.iconProvider = iconProvider;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	public void setHandlingCollection(boolean handlingCollection) {
		this.handlingCollection = handlingCollection;
	}
	
	public boolean isHandlingCollection() {
		return handlingCollection;
	}
}
